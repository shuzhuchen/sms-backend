package net.javaguides.sms_backend.kafka.producer;

import net.javaguides.sms_backend.kafka.KafkaTopicConfig;
import net.javaguides.sms_backend.kafka.event.SmsEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsEventProducerTest {

    @Mock
    private KafkaTemplate<String, SmsEvent> kafkaTemplate;

    @Test
    void publishSendsEventWithEntityIdAsKey() {
        SmsEvent event = new SmsEvent("event-1", "CREATED", 42L, "Student created", Instant.now());
        CompletableFuture<SendResult<String, SmsEvent>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(KafkaTopicConfig.SMS_EVENTS_TOPIC, "42", event)).thenReturn(future);

        SmsEventProducer producer = new SmsEventProducer(kafkaTemplate);

        producer.publish(event);

        verify(kafkaTemplate).send(KafkaTopicConfig.SMS_EVENTS_TOPIC, "42", event);
    }
}
