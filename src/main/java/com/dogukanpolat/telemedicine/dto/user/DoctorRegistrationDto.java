package com.dogukanpolat.telemedicine.dto.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record DoctorRegistrationDto(
        @Valid UserRegistrationDto user,

        @NotBlank(message = "Medical license number is required")
        @Size(max = 100, message = "Medical license number must be at most 100 characters")
        String medicalLicenseNumber,

        @NotBlank(message = "Specialization is required")
        @Size(max = 100, message = "Specialization must be at most 100 characters")
        String specialization,

        @Min(value = 0, message = "Experience must be at least 0 years")
        @Max(value = 50, message = "Experience must be at most 50 years")
        Integer yearsOfExperience,

        @Size(max = 1000)
        String biography,

        @NotNull(message = "Consultation fee is required")
        @DecimalMin(value = "0.0", message = "Fee must be at least 0")
        BigDecimal consultationFee
) {
}
