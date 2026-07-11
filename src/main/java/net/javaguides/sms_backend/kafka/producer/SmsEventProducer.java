package net.javaguides.sms_backend.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javaguides.sms_backend.kafka.KafkaTopicConfig;
import net.javaguides.sms_backend.kafka.event.SmsEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsEventProducer {

    private final KafkaTemplate<String, SmsEvent> kafkaTemplate;

    public void publish(SmsEvent event) {
        String key = String.valueOf(event.entityId());
        kafkaTemplate.send(KafkaTopicConfig.SMS_EVENTS_TOPIC, key, event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Failed to publish SMS event eventId={} entityId={}",
                                event.eventId(), event.entityId(), exception);
                        return;
                    }

                    log.info("Published SMS event eventId={} topic={} partition={} offset={}",
                            event.eventId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                });
    }
}
