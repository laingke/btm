package bitronix.tm.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Ludovic Orban
 */
public class MonotonicClockTest {

    @Test
    public void testPrecision() throws Exception {
        for (int i = 0; i < 100; i++) {
            long monoTime = MonotonicClock.currentTimeMillis();
            long wallTime = System.currentTimeMillis();
            assertTrue(Math.abs(wallTime - monoTime) < 5L, "iteration #" + i + " wall time: " + wallTime + ", mono time: " + monoTime);
            Thread.sleep(10);
        }
    }

}
