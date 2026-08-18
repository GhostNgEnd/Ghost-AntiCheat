package ac.ghost.anticheat.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LatencyUtilTest {

    @Test
    void responsesReleaseExactlyOneMarkerInSendOrder() {
        final LatencyUtil latency = new LatencyUtil(null);
        final AtomicInteger order = new AtomicInteger();

        latency.queue(37L, true);
        latency.queue(() -> assertEquals(0, order.getAndIncrement()));
        latency.queue(0L, false);
        latency.queue(() -> assertEquals(1, order.getAndIncrement()));

        assertTrue(latency.hasInFlight());
        assertTrue(latency.onOrderedResponse());
        assertTrue(latency.hasInFlight());
        assertEquals(1, order.get());

        assertFalse(latency.onOrderedResponse());
        assertFalse(latency.hasInFlight());
        assertEquals(2, order.get());
    }

    @Test
    void unmatchedWireTimestampCannotBlockOrderedResponse() {
        final LatencyUtil latency = new LatencyUtil(null);
        final AtomicInteger released = new AtomicInteger();

        latency.queue(91L, true);
        latency.queue(released::incrementAndGet);

        
        
        assertTrue(latency.onOrderedResponse());
        assertEquals(1, released.get());
        assertFalse(latency.hasInFlight());
    }
}
