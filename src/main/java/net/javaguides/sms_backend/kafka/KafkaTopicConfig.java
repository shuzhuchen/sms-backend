package net.javaguides.sms_backend.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String SMS_EVENTS_TOPIC = "sms-events";
    public static final String SMS_EVENTS_DLT_TOPIC = "sms-events.DLT";

    @Bean
    public NewTopic smsEventsTopic() {
        return TopicBuilder.name(SMS_EVENTS_TOPIC)
                .partitions(3)
                .replicas(3)
                .config("min.insync.replicas", "2")
                .build();
    }

    @Bean
    public NewTopic smsEventsDltTopic() {
        return TopicBuilder.name(SMS_EVENTS_DLT_TOPIC)
                .partitions(3)
                .replicas(3)
                .config("min.insync.replicas", "2")
                .build();
    }
}
