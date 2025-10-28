package com.dogukanpolat.telemedicine.dto.availability;

import com.dogukanpolat.telemedicine.model.enums.DayOfWeek;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AvailabilityResponseDto(
    UUID id,
    UUID doctorId,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    Boolean isAvailable,
    OffsetDateTime createdAt
){
}
