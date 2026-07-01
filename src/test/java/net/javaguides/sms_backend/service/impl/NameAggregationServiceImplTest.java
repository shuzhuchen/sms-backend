package net.javaguides.sms_backend.service.impl;

import net.javaguides.sms_backend.dto.NameAggregationRequest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NameAggregationServiceImplTest {

    @Test
    void downstreamSuccessReturnsInputNamesThenDownstreamReturnedNames() {
        TestNameAggregationService service = new TestNameAggregationService(new NameAggregationRequest(List.of("Alice", "Bob")), "");

        NameAggregationRequest response = service.forwardToNext(List.of("Suzy"));

        assertEquals(List.of("Suzy", "Alice", "Bob"), response.getName());
        assertEquals(null, response.getFallbackReason());
    }

    @Test
    void fallbackReturnsLocalResponseWhenDownstreamFails() {
        TestNameAggregationService service = new TestNameAggregationService(null, "");

        NameAggregationRequest response = service.downgrade(List.of("Suzy"), new RuntimeException("Read timed out"));

        assertEquals(List.of("Suzy"), response.getName());
        assertEquals("fallback-downstream unavailable", response.getFallbackReason());
    }

    @Test
    void configuredServiceNameIsAppendedWithoutHardcodingName() {
        TestNameAggregationService service = new TestNameAggregationService(new NameAggregationRequest(List.of("Alice")), "Backend");

        NameAggregationRequest response = service.forwardToNext(List.of("Jude"));

        assertEquals(List.of("Jude", "Backend", "Alice"), response.getName());
    }

    private static class TestNameAggregationService extends NameAggregationServiceImpl {
        private final NameAggregationRequest downstreamResponse;

        private TestNameAggregationService(NameAggregationRequest downstreamResponse, String serviceName) {
            super("http://localhost:8081/name/aggregation", Duration.ofMillis(100), serviceName);
            this.downstreamResponse = downstreamResponse;
        }

        @Override
        protected NameAggregationRequest sendDownstream(NameAggregationRequest request) {
            if (downstreamResponse == null) {
                throw new RuntimeException("Read timed out");
            }

            return downstreamResponse;
        }
    }
}
