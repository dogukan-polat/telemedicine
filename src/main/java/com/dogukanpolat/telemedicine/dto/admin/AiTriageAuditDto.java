package com.dogukanpolat.telemedicine.dto.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AiTriageAuditDto(
        UUID id,
        UUID patientId,
        String patientName,
        String userInput,
        String aiOutput,
        String urgencyLevel,
        OffsetDateTime createdAt
) {
}
