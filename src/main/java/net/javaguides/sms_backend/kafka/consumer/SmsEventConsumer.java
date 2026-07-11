package net.javaguides.sms_backend.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import net.javaguides.sms_backend.kafka.KafkaTopicConfig;
import net.javaguides.sms_backend.kafka.event.SmsEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsEventConsumer {

    @KafkaListener(
            topics = KafkaTopicConfig.SMS_EVENTS_TOPIC,
            groupId = "sms-notification-service",
            concurrency = "3",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(@Payload SmsEvent event, Acknowledgment ack) {
        log.info("Received SMS event eventId={} eventType={} entityId={}",
                event.eventId(), event.eventType(), event.entityId());
        ack.acknowledge();
    }
}
