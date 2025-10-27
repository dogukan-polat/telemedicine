package com.dogukanpolat.telemedicine.dto.appointment;

import com.dogukanpolat.telemedicine.model.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentResponseDto(
        String patientFirstName,
        String patientLastName,
        String doctorFirstName,
        String doctorLastName,
        LocalDate scheduledDate,
        LocalTime scheduledTime,
        Integer durationInMinutes,
        AppointmentStatus status
) {
}
