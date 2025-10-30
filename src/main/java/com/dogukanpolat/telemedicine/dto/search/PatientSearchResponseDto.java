package com.dogukanpolat.telemedicine.dto.search;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PatientSearchResponseDto(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String bloodType,
        List<String> allergies,
        String emergencyContactName,
        String emergencyContactPhone,
        Boolean isActive,
        LocalDate createdAt
) {
}
