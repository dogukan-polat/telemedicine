package com.dogukanpolat.telemedicine.dto.appointment;

import com.dogukanpolat.telemedicine.model.Doctor;
import com.dogukanpolat.telemedicine.model.Patient;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;


public record AppointmentRequestDto(

        @NotNull(message = "Patient cannot be null.")
        Patient patient,

        @NotNull(message = "Doctor cannot be null.")
        Doctor doctor,

        @NotNull(message = "Date can't be null.")
        @FutureOrPresent(message = "Cannot be scheduled in past.")
        LocalDate scheduledDate,

        @NotNull(message = "Time cannot be null")
        LocalTime scheduledTime,

        @Max(message = "Duration cannot be longer than 2 hours.", value = 120)
        @Positive(message = "Duration cannot be zero or lower.")
        Integer durationMinutes
) {}