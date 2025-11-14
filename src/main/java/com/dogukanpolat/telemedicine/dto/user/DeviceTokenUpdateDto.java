package com.dogukanpolat.telemedicine.dto.user;

import jakarta.validation.constraints.NotNull;

public record DeviceTokenUpdateDto(
        @NotNull(message = "Device token is required")
        String deviceToken
) {
}
