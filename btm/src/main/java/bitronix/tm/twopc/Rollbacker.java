/*
 * Copyright (C) 2006-2013 Bitronix Software (http://www.bitronix.be)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bitronix.tm.twopc;

import bitronix.tm.BitronixTransaction;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.internal.*;
import bitronix.tm.twopc.executor.Executor;
import bitronix.tm.twopc.executor.Job;
import bitronix.tm.utils.Decoder;
import jakarta.transaction.HeuristicCommitException;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.xa.XAException;
import java.util.*;

/**
 * Phase 1 &amp; 2 Rollback logic engine.
 *
 * @author Ludovic Orban
 */
public final class Rollbacker extends AbstractPhaseEngine {

    private static final Logger log = LoggerFactory.getLogger(Rollbacker.class);

    private final List<XAResourceHolderState> interestedResources = Collections.synchronizedList(new ArrayList<XAResourceHolderState>());
    // this list has to be thread-safe as the RollbackJobs can be executed in parallel (when async 2PC is configured)
    private final List<XAResourceHolderState> rolledbackResources = Collections.synchronizedList(new ArrayList<XAResourceHolderState>());

    public Rollbacker(Executor executor) {
        super(executor);
    }

    /**
     * Rollback the current XA transaction. Transaction will not timeout while changing status but rather by some
     * extra logic that will manually throw the exception after doing as much cleanup as possible.
     *
     * @param transaction         the transaction to rollback.
     * @param interestedResources resources that should be rolled back.
     * @throws HeuristicCommitException                     when all resources committed instead.
     * @throws HeuristicMixedException                      when some resources committed and some rolled back.
     * @throws bitronix.tm.internal.BitronixSystemException when an internal error occured.
     */
    public void rollback(BitronixTransaction transaction, List<XAResourceHolderState> interestedResources) throws HeuristicMixedException, HeuristicCommitException, BitronixSystemException {
        XAResourceManager resourceManager = transaction.getResourceManager();
        transaction.setStatus(Status.STATUS_ROLLING_BACK);
        this.interestedResources.clear();
        this.interestedResources.addAll(interestedResources);

        try {
            executePhase(resourceManager, true);
        } catch (PhaseException ex) {
            logFailedResources(ex);
            transaction.setStatus(Status.STATUS_UNKNOWN);
            throwException("transaction failed during rollback of " + transaction, ex, interestedResources.size());
        }

        if (log.isDebugEnabled()) {
            log.debug("rollback executed on resources " + Decoder.collectResourcesNames(rolledbackResources));
        }

        // Some resources might have failed the 2nd phase of 2PC.
        // Only resources which successfully rolled back should be registered in the journal, the other
        // ones should be picked up by the recoverer.
        // Not interested resources have to be included as well since they returned XA_RDONLY and they
        // don't participate in phase 2: the TX succeded for them.
        Set<String> rolledbackAndNotInterestedUniqueNames = new HashSet<String>();
        rolledbackAndNotInterestedUniqueNames.addAll(collectResourcesUniqueNames(rolledbackResources));
        List<XAResourceHolderState> notInterestedResources = collectNotInterestedResources(resourceManager.getAllResources(), interestedResources);
        rolledbackAndNotInterestedUniqueNames.addAll(collectResourcesUniqueNames(notInterestedResources));

        if (log.isDebugEnabled()) {
            List<XAResourceHolderState> rolledbackAndNotInterestedResources = new ArrayList<XAResourceHolderState>();
            rolledbackAndNotInterestedResources.addAll(rolledbackResources);
            rolledbackAndNotInterestedResources.addAll(notInterestedResources);

            log.debug("rollback succeeded on resources " + Decoder.collectResourcesNames(rolledbackAndNotInterestedResources));
        }

        transaction.setStatus(Status.STATUS_ROLLEDBACK, rolledbackAndNotInterestedUniqueNames);
    }

    private void throwException(String message, PhaseException phaseException, int totalResourceCount) throws HeuristicMixedException, HeuristicCommitException {
        List<Exception> exceptions = phaseException.getExceptions();
        List<XAResourceHolderState> resources = phaseException.getResourceStates();

        boolean hazard = false;
        List<XAResourceHolderState> heuristicResources = new ArrayList<XAResourceHolderState>();
        List<XAResourceHolderState> errorResources = new ArrayList<XAResourceHolderState>();

        for (int i = 0; i < exceptions.size(); i++) {
            Exception ex = exceptions.get(i);
            XAResourceHolderState resourceHolder = resources.get(i);
            if (ex instanceof XAException xaEx) {
                switch (xaEx.errorCode) {
                    case XAException.XA_HEURHAZ:
                        hazard = true;
                    case XAException.XA_HEURCOM:
                    case XAException.XA_HEURRB:
                    case XAException.XA_HEURMIX:
                        heuristicResources.add(resourceHolder);
                        break;

                    default:
                        errorResources.add(resourceHolder);
                }
            } else {
                errorResources.add(resourceHolder);
            }
        }

        if (!hazard && heuristicResources.size() == totalResourceCount) {
            throw new BitronixHeuristicCommitException(message + ":" +
                    " all resource(s) " + Decoder.collectResourcesNames(heuristicResources) +
                    " improperly unilaterally committed", phaseException);
        } else {
            throw new BitronixHeuristicMixedException(message + ":" +
                    (errorResources.size() > 0 ? " resource(s) " + Decoder.collectResourcesNames(errorResources) + " threw unexpected exception" : "") +
                    (errorResources.size() > 0 && heuristicResources.size() > 0 ? " and" : "") +
                    (heuristicResources.size() > 0 ? " resource(s) " + Decoder.collectResourcesNames(heuristicResources) + " improperly unilaterally committed" + (hazard ? " (or hazard happened)" : "") : ""), phaseException);
        }
    }

    @Override
    protected Job createJob(XAResourceHolderState resourceHolder) {
        return new RollbackJob(resourceHolder);
    }

    @Override
    protected boolean isParticipating(XAResourceHolderState xaResourceHolderState) {
        for (XAResourceHolderState resourceHolderState : interestedResources) {
            if (xaResourceHolderState == resourceHolderState) {
                return true;
            }
        }
        return false;
    }

    private final class RollbackJob extends Job {

        public RollbackJob(XAResourceHolderState resourceHolder) {
            super(resourceHolder);
        }

        @Override
        public void execute() {
            try {
                rollbackResource(getResource());
            } catch (RuntimeException ex) {
                runtimeException = ex;
            } catch (XAException ex) {
                xaException = ex;
            }
        }

        private void rollbackResource(XAResourceHolderState resourceHolder) throws XAException {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("trying to rollback resource " + resourceHolder);
                }
                resourceHolder.getXAResource().rollback(resourceHolder.getXid());
                rolledbackResources.add(resourceHolder);
                if (log.isDebugEnabled()) {
                    log.debug("rolled back resource " + resourceHolder);
                }
            } catch (XAException ex) {
                handleXAException(resourceHolder, ex);
            }
        }

        private void handleXAException(XAResourceHolderState failedResourceHolder, XAException xaException) throws XAException {
            switch (xaException.errorCode) {
                case XAException.XA_HEURRB:
                    forgetHeuristicRollback(failedResourceHolder);
                    return;

                case XAException.XA_HEURCOM:
                case XAException.XA_HEURHAZ:
                case XAException.XA_HEURMIX:
                    log.error("heuristic rollback is incompatible with the global state of this transaction - guilty: " + failedResourceHolder);
                    throw xaException;

                default:
                    String extraErrorDetails = TransactionManagerServices.getExceptionAnalyzer().extractExtraXAExceptionDetails(xaException);
                    log.warn("resource '" + failedResourceHolder.getUniqueName() + "' reported " + Decoder.decodeXAExceptionErrorCode(xaException) +
                            " when asked to rollback transaction branch. Transaction is prepared and will rollback via recovery service when resource availability allows."
                            + (extraErrorDetails == null ? "" : " Extra error=" + extraErrorDetails), xaException);
            }
        }

        private void forgetHeuristicRollback(XAResourceHolderState resourceHolder) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("handling heuristic rollback on resource " + resourceHolder.getXAResource());
                }
                resourceHolder.getXAResource().forget(resourceHolder.getXid());
                if (log.isDebugEnabled()) {
                    log.debug("forgotten heuristically rolled back resource " + resourceHolder.getXAResource());
                }
            } catch (XAException ex) {
                String extraErrorDetails = TransactionManagerServices.getExceptionAnalyzer().extractExtraXAExceptionDetails(ex);
                log.error("cannot forget " + resourceHolder.getXid() + " assigned to " + resourceHolder.getXAResource() +
                        ", error=" + Decoder.decodeXAExceptionErrorCode(ex) + (extraErrorDetails == null ? "" : ", extra error=" + extraErrorDetails), ex);
            }
        }

        @Override
        public String toString() {
            return "a RollbackJob with " + getResource();
        }
    }

}
