package com.dogukanpolat.telemedicine.dto.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

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

        String phoneNumber,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth
) {
}
