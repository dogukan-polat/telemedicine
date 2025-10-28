package com.dogukanpolat.telemedicine.dto.availability;

import com.dogukanpolat.telemedicine.model.enums.DayOfWeek;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record BulkAvailabilityRequestDto(
        @NotNull(message = "Doctor ID is required")
        UUID doctorId,

        @NotEmpty(message = "At least one availability slot is required")
        List<AvailabilitySlot> slots
) {
    public record AvailabilitySlot(
            @NotNull(message = "Day of week is required")
            DayOfWeek dayOfWeek,

            @NotNull(message = "Start time is required")
            LocalTime startTime,

            @NotNull(message = "End time is required")
            LocalTime endTime
    ) {
    }
}
