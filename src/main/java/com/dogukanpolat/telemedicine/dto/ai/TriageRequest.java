package com.dogukanpolat.telemedicine.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TriageRequest(

        @NotNull(message = "Patient ID required.")
        UUID patientId,

        @NotBlank(message = "Symptom description required.")
        String symptomDescription
) {
}
