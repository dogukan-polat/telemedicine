package com.dogukanpolat.telemedicine.dto.search;

import com.dogukanpolat.telemedicine.model.enums.DayOfWeek;

import java.math.BigDecimal;
import java.time.LocalTime;

public record DoctorSearchCriteria(
        String specialization,
        String location,
        BigDecimal minFee,
        BigDecimal maxFee,
        Integer minExperience,
        Boolean isVerified,
        DayOfWeek availableDay,
        LocalTime availableStartTime,
        LocalTime availableEndTime,
        String name
) {
}
