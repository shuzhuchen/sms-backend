package net.javaguides.sms_backend.kafka.event;

import java.time.Instant;

public record SmsEvent(
        String eventId,
        String eventType,
        Long entityId,
        String message,
        Instant occurredAt
) {
}
