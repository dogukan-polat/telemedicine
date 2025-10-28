package com.dogukanpolat.telemedicine.dto.availability;

import com.dogukanpolat.telemedicine.model.enums.DayOfWeek;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

public record AvailabilityRequestDto(
        @NotNull(message = "Doctor ID is required")
        UUID doctorId,

        @NotNull(message = "Day of week is required")
        DayOfWeek dayOfWeek,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime
) {
}
