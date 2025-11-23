package com.dogukanpolat.telemedicine.dto.medicalrecord;

import com.dogukanpolat.telemedicine.model.enums.MedicalRecordType;

import java.time.OffsetDateTime;

public record MedicalRecordUpdateDto(
        String title,
        String description,
        MedicalRecordType recordType,
        OffsetDateTime recordDate,
        String notes
) {
}
