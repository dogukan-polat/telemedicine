package com.dogukanpolat.telemedicine.dto.notification;

import com.dogukanpolat.telemedicine.model.enums.NotificationChannel;
import com.dogukanpolat.telemedicine.model.enums.NotificationStatus;
import com.dogukanpolat.telemedicine.model.enums.NotificationType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationLogResponseDto(
        UUID id,
        NotificationType notificationType,
        NotificationChannel channel,
        NotificationStatus status,
        String recipient,
        String subject,
        String errorMessage,
        OffsetDateTime sentAt,
        OffsetDateTime createdAt,
        UUID appointmentId
) {
}
