package com.dogukanpolat.telemedicine.dto.user;

public record DoctorResponseDto(
        String firstName,
        String lastName,
        String phoneNumber,
        String specialization,
        Integer yearsOfExperience
) {
}
