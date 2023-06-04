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
package bitronix.tm.timer;

import bitronix.tm.recovery.Recoverer;
import bitronix.tm.utils.MonotonicClock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 *
 * @author Ludovic Orban
 */
public class TaskSchedulerTest {

    private TaskScheduler ts;

    @BeforeEach
    protected void setUp() throws Exception {
        ts = new TaskScheduler();
        ts.start();
    }

    @AfterEach
    protected void tearDown() throws Exception {
        assertEquals(0, ts.countTasksQueued());
        ts.shutdown();
    }

    @Test
    public void testRecoveryTask() throws Exception {
        Recoverer recoverer = new Recoverer();
        ts.scheduleRecovery(recoverer, LocalDateTime.now());
        assertEquals(1, ts.countTasksQueued());
        Thread.sleep(1100);
        assertEquals(1, ts.countTasksQueued());

        ts.cancelRecovery(recoverer);
        assertEquals(0, ts.countTasksQueued());
        Thread.sleep(1100);
        assertEquals(0, ts.countTasksQueued());
    }

    @Test
    public void testTaskOrdering() throws Exception {
        List<SimpleTask> result = Collections.synchronizedList(new ArrayList<SimpleTask>());
        ts.addTask(new SimpleTask(Instant.ofEpochMilli(MonotonicClock.currentTimeMillis() + 100)
                .atZone(ZoneId.systemDefault()).toLocalDateTime(), ts, 0, result));
        ts.addTask(new SimpleTask(Instant.ofEpochMilli(MonotonicClock.currentTimeMillis() + 200)
                .atZone(ZoneId.systemDefault()).toLocalDateTime(), ts, 1, result));
        ts.addTask(new SimpleTask(Instant.ofEpochMilli(MonotonicClock.currentTimeMillis() + 300)
                .atZone(ZoneId.systemDefault()).toLocalDateTime(), ts, 2, result));

        ts.join(1000);

        assertEquals(0, result.get(0).getObject());
        assertEquals(1, result.get(1).getObject());
        assertEquals(2, result.get(2).getObject());
    }

    @Test
    public void testIdenticalScheduleTimestamp() throws Exception {
        List<SimpleTask> result = Collections.synchronizedList(new ArrayList<SimpleTask>());

        long firstTimestamp = MonotonicClock.currentTimeMillis();
        long secondTimestamp = MonotonicClock.currentTimeMillis() + 200;

        ts.addTask(new SimpleTask(Instant.ofEpochMilli(firstTimestamp).atZone(ZoneId.systemDefault()).toLocalDateTime(), ts, 0, result));

        ts.addTask(new SimpleTask(Instant.ofEpochMilli(secondTimestamp).atZone(ZoneId.systemDefault()).toLocalDateTime(), ts, 1, result));
        ts.addTask(new SimpleTask(Instant.ofEpochMilli(secondTimestamp).atZone(ZoneId.systemDefault()).toLocalDateTime(), ts, 2, result));

        assertEquals(3, ts.countTasksQueued(), "Three tasks were created.  All 3 (even identical timestamps) should be queued");

        ts.join(1000);
    }

    private static class SimpleTask extends Task {

        private final Object obj;
        private final List<SimpleTask> result;

        protected SimpleTask(LocalDateTime executionTime, TaskScheduler scheduler, Object obj, List<SimpleTask> result) {
            super(executionTime, scheduler);
            this.obj = obj;
            this.result = result;
        }

        @Override
        public Object getObject() {
            return obj;
        }

        @Override
        public void execute() throws TaskException {
            result.add(this);
        }
    }

}
