package com.dogukanpolat.telemedicine.dto.medicalrecord;

import com.dogukanpolat.telemedicine.model.enums.MedicalRecordType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MedicalRecordSearchCriteria(
        UUID patientId,
        UUID doctorId,
        MedicalRecordType recordType,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        String searchTerm
) {
}
