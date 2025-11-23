package com.dogukanpolat.telemedicine.dto.medicalrecord;

import com.dogukanpolat.telemedicine.model.enums.MedicalRecordType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MedicalRecordUploadDto(
        @NotNull(message = "Patient ID is required")
        UUID patientId,

        UUID doctorId,

        UUID appointmentId,

        @NotBlank(message = "Title is required")
        String title,

        String description,

        @NotNull(message = "Record type is required")
        MedicalRecordType recordType,

        @NotNull(message = "File is required")
        MultipartFile file,

        OffsetDateTime recordDate,

        String notes
) {
}
