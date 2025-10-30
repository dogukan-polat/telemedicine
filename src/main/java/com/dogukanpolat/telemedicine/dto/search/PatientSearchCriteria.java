package com.dogukanpolat.telemedicine.dto.search;

public record PatientSearchCriteria(
        String name,
        String email,
        String bloodType,
        Boolean isActive,
        String phoneNumber
) {
}
