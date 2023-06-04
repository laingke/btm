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
package bitronix.tm.utils;

import bitronix.tm.internal.XAResourceHolderState;
import bitronix.tm.journal.TransactionLogHeader;
import jakarta.transaction.Status;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import java.util.Collection;
import java.util.Iterator;

/**
 * Constant to string decoder.
 *
 * @author Ludovic Orban
 */
public class Decoder {

    public static String decodeXAExceptionErrorCode(XAException ex) {
        return switch (ex.errorCode) {
            // rollback errors
            case XAException.XA_RBROLLBACK -> "XA_RBROLLBACK";
            case XAException.XA_RBCOMMFAIL -> "XA_RBCOMMFAIL";
            case XAException.XA_RBDEADLOCK -> "XA_RBDEADLOCK";
            case XAException.XA_RBTRANSIENT -> "XA_RBTRANSIENT";
            case XAException.XA_RBINTEGRITY -> "XA_RBINTEGRITY";
            case XAException.XA_RBOTHER -> "XA_RBOTHER";
            case XAException.XA_RBPROTO -> "XA_RBPROTO";
            case XAException.XA_RBTIMEOUT -> "XA_RBTIMEOUT";

            // heuristic errors
            case XAException.XA_HEURCOM -> "XA_HEURCOM";
            case XAException.XA_HEURHAZ -> "XA_HEURHAZ";
            case XAException.XA_HEURMIX -> "XA_HEURMIX";
            case XAException.XA_HEURRB -> "XA_HEURRB";

            // misc failures errors
            case XAException.XAER_RMERR -> "XAER_RMERR";
            case XAException.XAER_RMFAIL -> "XAER_RMFAIL";
            case XAException.XAER_NOTA -> "XAER_NOTA";
            case XAException.XAER_INVAL -> "XAER_INVAL";
            case XAException.XAER_PROTO -> "XAER_PROTO";
            case XAException.XAER_ASYNC -> "XAER_ASYNC";
            case XAException.XAER_DUPID -> "XAER_DUPID";
            case XAException.XAER_OUTSIDE -> "XAER_OUTSIDE";
            default -> "!invalid error code (" + ex.errorCode + ")!";
        };
    }

    public static String decodeStatus(int status) {
        return switch (status) {
            case Status.STATUS_ACTIVE -> "ACTIVE";
            case Status.STATUS_COMMITTED -> "COMMITTED";
            case Status.STATUS_COMMITTING -> "COMMITTING";
            case Status.STATUS_MARKED_ROLLBACK -> "MARKED_ROLLBACK";
            case Status.STATUS_NO_TRANSACTION -> "NO_TRANSACTION";
            case Status.STATUS_PREPARED -> "PREPARED";
            case Status.STATUS_PREPARING -> "PREPARING";
            case Status.STATUS_ROLLEDBACK -> "ROLLEDBACK";
            case Status.STATUS_ROLLING_BACK -> "ROLLING_BACK";
            case Status.STATUS_UNKNOWN -> "UNKNOWN";
            default -> "!incorrect status (" + status + ")!";
        };
    }

    public static String decodeXAResourceFlag(int flag) {
        return switch (flag) {
            case XAResource.TMENDRSCAN -> "ENDRSCAN";
            case XAResource.TMFAIL -> "FAIL";
            case XAResource.TMJOIN -> "JOIN";
            case XAResource.TMNOFLAGS -> "NOFLAGS";
            case XAResource.TMONEPHASE -> "ONEPHASE";
            case XAResource.TMRESUME -> "RESUME";
            case XAResource.TMSTARTRSCAN -> "STARTRSCAN";
            case XAResource.TMSUCCESS -> "SUCCESS";
            case XAResource.TMSUSPEND -> "SUSPEND";
            default -> "!invalid flag (" + flag + ")!";
        };
    }

    public static String decodePrepareVote(int vote) {
        return switch (vote) {
            case XAResource.XA_OK -> "XA_OK";
            case XAResource.XA_RDONLY -> "XA_RDONLY";
            default -> "!invalid return code (" + vote + ")!";
        };
    }

    public static String decodeHeaderState(byte state) {
        return switch (state) {
            case TransactionLogHeader.CLEAN_LOG_STATE -> "CLEAN_LOG_STATE";
            case TransactionLogHeader.UNCLEAN_LOG_STATE -> "UNCLEAN_LOG_STATE";
            default -> "!invalid state (" + state + ")!";
        };
    }

    /**
     * Create a String representation of a list of {@link bitronix.tm.resource.common.XAResourceHolder}s. This
     * String will contain each resource's unique name.
     *
     * @param resources a list of {@link bitronix.tm.resource.common.XAResourceHolder}s.
     * @return a String representation of the list.
     */
    public static String collectResourcesNames(Collection<XAResourceHolderState> resources) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        Iterator<XAResourceHolderState> it = resources.iterator();
        while (it.hasNext()) {
            XAResourceHolderState resourceHolderState = it.next();
            sb.append(resourceHolderState.getUniqueName());

            if (it.hasNext()) {
                sb.append(", ");
            }
        }

        sb.append("]");
        return sb.toString();
    }

}
