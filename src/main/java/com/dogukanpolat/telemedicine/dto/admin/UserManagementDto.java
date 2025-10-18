package com.dogukanpolat.telemedicine.dto.admin;

import java.time.LocalDate;
import java.util.UUID;

public record UserManagementDto(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String role,
        Boolean isActive,
        LocalDate createdAt
) {
}
