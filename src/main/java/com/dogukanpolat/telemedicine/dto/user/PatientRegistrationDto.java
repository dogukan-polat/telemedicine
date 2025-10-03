package com.dogukanpolat.telemedicine.dto.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PatientRegistrationDto(
        @Valid UserRegistrationDto userRegistrationDto,

        @Size(max = 200, message = "Emergency contact name must be at most 200 characters")
        String emergencyContactName,

        @Pattern(regexp = "^\\+?[1-9][0-9]\\d{1,14}", message = "Emergency contact phone must be a valid phone number")
        String emergencyContactPhone,

        @Pattern(regexp = "^(A|B|AB|O)[+-]", message = "Blood type must be A, B, AB, O, + or -")
        String bloodType,

        List<String> allergies
) {
}
