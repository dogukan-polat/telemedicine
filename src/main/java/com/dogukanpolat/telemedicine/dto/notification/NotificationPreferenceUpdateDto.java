package com.dogukanpolat.telemedicine.dto.notification;

import com.dogukanpolat.telemedicine.model.enums.NotificationChannel;
import com.dogukanpolat.telemedicine.model.enums.NotificationType;
import jakarta.validation.constraints.NotNull;

public record NotificationPreferenceUpdateDto(
        @NotNull(message = "Notification type is required")
        NotificationType notificationType,

        @NotNull(message = "Notification channel is required")
        NotificationChannel channel,

        @NotNull(message = "Enabled status is required")
        Boolean isEnabled
) {
}
