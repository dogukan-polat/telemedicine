package com.dogukanpolat.telemedicine.dto.search;

import java.math.BigDecimal;
import java.util.UUID;

public record DoctorSearchResponseDto(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String specialization,
        Integer yearsOfExperience,
        BigDecimal consultationFee,
        Boolean isVerified
) {
}
