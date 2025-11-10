package com.dogukanpolat.telemedicine.dto.notification;

import com.dogukanpolat.telemedicine.model.enums.NotificationChannel;
import com.dogukanpolat.telemedicine.model.enums.NotificationType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationPreferenceResponseDto(
        UUID id,
        NotificationType notificationType,
        NotificationChannel channel,
        Boolean isEnabled,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
