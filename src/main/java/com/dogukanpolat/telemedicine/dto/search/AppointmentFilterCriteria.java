package com.dogukanpolat.telemedicine.dto.search;

import com.dogukanpolat.telemedicine.model.enums.AppointmentStatus;

import java.time.LocalDate;
import java.util.UUID;

public record AppointmentFilterCriteria(
        UUID patientId,
        UUID doctorId,
        AppointmentStatus status,
        LocalDate startDate,
        LocalDate endDate
) {
}
