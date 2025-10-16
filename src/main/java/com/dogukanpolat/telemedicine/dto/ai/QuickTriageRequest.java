package com.dogukanpolat.telemedicine.dto.ai;

import jakarta.validation.constraints.NotBlank;

public record QuickTriageRequest(
        @NotBlank(message = "Symptoms description required.")
        String symptomDescription
) {
}
