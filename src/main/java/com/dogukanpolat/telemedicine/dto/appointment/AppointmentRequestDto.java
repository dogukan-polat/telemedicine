package com.dogukanpolat.telemedicine.dto.appointment;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;


public record AppointmentRequestDto(

        @NotNull(message = "Patient ID cannot be null.")
        UUID patientId,

        @NotNull(message = "Doctor ID cannot be null.")
        UUID doctorId,

        @NotNull(message = "Date can't be null.")
        @FutureOrPresent(message = "Cannot be scheduled in past.")
        LocalDate scheduledDate,

        @NotNull(message = "Time cannot be null")
        LocalTime scheduledTime,

        @Max(message = "Duration cannot be longer than 2 hours.", value = 120)
        @Positive(message = "Duration cannot be zero or lower.")
        Integer durationMinutes
) {}