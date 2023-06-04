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
package bitronix.tm.integration.jetty9;

import bitronix.tm.TransactionManagerServices;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

import java.util.logging.Logger;

public class BTMLifeCycle extends AbstractLifeCycle {
    private static final Logger log = Logger.getLogger(BTMLifeCycle.class.getName());

    @Override
    protected void doStart() throws Exception {
        log.info("Starting Bitronix Transaction Manager");
        TransactionManagerServices.getTransactionManager();
    }

    @Override
    protected void doStop() throws Exception {
        log.info("Shutting down Bitronix Transaction Manager");
        TransactionManagerServices.getTransactionManager().shutdown();
    }

}
