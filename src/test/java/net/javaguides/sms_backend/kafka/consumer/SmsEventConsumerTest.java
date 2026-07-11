package net.javaguides.sms_backend.kafka.consumer;

import net.javaguides.sms_backend.kafka.event.SmsEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SmsEventConsumerTest {

    @Test
    void consumeAcknowledgesAfterProcessing() {
        SmsEventConsumer consumer = new SmsEventConsumer();
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        consumer.consume(new SmsEvent("event-1", "CREATED", 42L, "Student created", Instant.now()), acknowledgment);

        verify(acknowledgment).acknowledge();
    }
}
