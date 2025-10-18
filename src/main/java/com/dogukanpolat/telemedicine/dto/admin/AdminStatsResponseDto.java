package com.dogukanpolat.telemedicine.dto.admin;

public record AdminStatsResponseDto(
        Long totalUsers,
        Long totalDoctors,
        Long totalPatients,
        Long totalAppointments,
        Long activeUsers
) {
}
