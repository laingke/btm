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
package bitronix.tm.resource.common;

import bitronix.tm.internal.XAResourceHolderState;
import bitronix.tm.utils.Uid;
import bitronix.tm.utils.UidGenerator;
import org.junit.jupiter.api.Test;

import javax.transaction.xa.XAResource;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Ludovic Orban
 */
public class AbstractXAResourceHolderTest {

    @Test
    public void testStatesForGtridIterationOrder() throws Exception {
        final ResourceBean resourceBean = new ResourceBean() {
        };

        AbstractXAResourceHolder<DummyResourceHolder> xaResourceHolder = new AbstractXAResourceHolder<DummyResourceHolder>() {
            @Override
            public XAResource getXAResource() {
                return null;
            }

            @Override
            public ResourceBean getResourceBean() {
                return resourceBean;
            }

            @Override
            public List<? extends XAResourceHolder<? extends XAResourceHolder>> getXAResourceHolders() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Object getConnectionHandle() throws Exception {
                return null;
            }

            @Override
            public void close() throws Exception {
            }

            @Override
            public LocalDateTime getLastReleaseDate() {
                return null;
            }
        };

        Uid gtrid = UidGenerator.generateUid();

        XAResourceHolderState state1 = new XAResourceHolderState(xaResourceHolder, resourceBean);
        XAResourceHolderState state2 = new XAResourceHolderState(xaResourceHolder, resourceBean);
        XAResourceHolderState state3 = new XAResourceHolderState(xaResourceHolder, resourceBean);

        xaResourceHolder.putXAResourceHolderState(UidGenerator.generateXid(gtrid), state1);
        xaResourceHolder.putXAResourceHolderState(UidGenerator.generateXid(gtrid), state2);
        xaResourceHolder.putXAResourceHolderState(UidGenerator.generateXid(gtrid), state3);


        Map<Uid, XAResourceHolderState> statesForGtrid = xaResourceHolder.getXAResourceHolderStatesForGtrid(gtrid);
        Iterator<XAResourceHolderState> statesForGtridIt = statesForGtrid.values().iterator();

        assertTrue(statesForGtridIt.hasNext());
        assertSame(state1, statesForGtridIt.next());
        assertTrue(statesForGtridIt.hasNext());
        assertSame(state2, statesForGtridIt.next());
        assertTrue(statesForGtridIt.hasNext());
        assertSame(state3, statesForGtridIt.next());
        assertFalse(statesForGtridIt.hasNext());
    }
}
