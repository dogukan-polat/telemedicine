package com.dogukanpolat.telemedicine.dto.user;

public record PatientResponseDto(
        String firstName,
        String lastName,
        String phoneNumber,
        String bloodType
) {
}
