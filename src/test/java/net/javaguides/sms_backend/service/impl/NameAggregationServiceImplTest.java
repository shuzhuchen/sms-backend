package net.javaguides.sms_backend.service.impl;

import net.javaguides.sms_backend.dto.NameAggregationRequest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NameAggregationServiceImplTest {

    @Test
    void downstreamSuccessReturnsLocalResponseWhenDownstreamAcceptsRequest() {
        TestNameAggregationService service = new TestNameAggregationService(false, "");

        NameAggregationRequest response = service.forwardToNext(List.of("Suzy"));

        assertEquals(List.of("Suzy"), response.getName());
        assertEquals(null, response.getFallbackReason());
    }

    @Test
    void fallbackReturnsLocalResponseWhenDownstreamFails() {
        TestNameAggregationService service = new TestNameAggregationService(true, "");

        NameAggregationRequest response = service.downgrade(List.of("Suzy"), new RuntimeException("Read timed out"));

        assertEquals(List.of("Suzy"), response.getName());
        assertEquals("fallback-downstream unavailable", response.getFallbackReason());
    }

    @Test
    void configuredServiceNameIsAppendedWithoutHardcodingName() {
        TestNameAggregationService service = new TestNameAggregationService(false, "Backend");

        NameAggregationRequest response = service.forwardToNext(List.of("Jude"));

        assertEquals(List.of("Jude", "Backend"), response.getName());
    }

    private static class TestNameAggregationService extends NameAggregationServiceImpl {
        private final boolean fail;

        private TestNameAggregationService(boolean fail, String serviceName) {
            super("http://localhost:8081/name/aggregation", Duration.ofMillis(100), serviceName);
            this.fail = fail;
        }

        @Override
        protected void sendDownstream(NameAggregationRequest request) {
            if (fail) {
                throw new RuntimeException("Read timed out");
            }
        }
    }
}
