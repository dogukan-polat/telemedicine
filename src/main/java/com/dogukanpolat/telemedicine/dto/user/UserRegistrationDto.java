package com.dogukanpolat.telemedicine.dto.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UserRegistrationDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email")
        String email,

        @Valid PasswordDto password,

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,

        @Pattern(regexp = "^\\+?[1-9][0-9]\\d{1,14}", message = "Emergency contact phone must be a valid phone number")
        String phoneNumber,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth
) {
}
