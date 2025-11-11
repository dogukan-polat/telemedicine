package com.dogukanpolat.telemedicine.dto.notification;

import java.util.UUID;

public record PushNotificationDto(
        String deviceToken,
        UUID userId,
        String title,
        String body
) {
}
