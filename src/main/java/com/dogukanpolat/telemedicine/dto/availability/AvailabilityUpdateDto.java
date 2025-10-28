package com.dogukanpolat.telemedicine.dto.availability;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record AvailabilityUpdateDto(
        LocalTime startTime,
        LocalTime endTime,
        @NotNull(message = "Availability status is required")
        Boolean isAvailable
) {
}
