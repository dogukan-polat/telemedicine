package com.dogukanpolat.telemedicine.dto.medicalrecord;


import com.dogukanpolat.telemedicine.model.enums.MedicalRecordType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MedicalRecordResponseDto(
        UUID id,
        UUID patientId,
        String patientName,
        UUID doctorId,
        String doctorName,
        UUID appointmentId,
        String title,
        String description,
        MedicalRecordType recordType,
        String fileName,
        Long fileSize,
        String mimeType,
        Boolean isEncrypted,
        Integer version,
        UUID previousVersionId,
        OffsetDateTime recordDate,
        OffsetDateTime createdAt,
        UUID uploadedBy,
        String uploadedByName,
        String notes
) {
}
