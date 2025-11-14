package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.user.*;
import com.dogukanpolat.telemedicine.model.UserModel;
import com.dogukanpolat.telemedicine.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for user registration (doctors, patients, and admins)")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Register a new doctor",
            description = "Register a new doctor account.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Doctor registration details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DoctorRegistrationDto.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Doctor registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DoctorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or duplicate email",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/register/doctor")
    public ResponseEntity<DoctorResponseDto> registerDoctor(@Valid @RequestBody DoctorRegistrationDto doctorRegistrationDto) {
        return new ResponseEntity<>(userService.registerDoctor(doctorRegistrationDto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Register a new patient",
            description = "Register a new patient account.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Patient registration details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PatientRegistrationDto.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Patient registered successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PatientResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or duplicate email",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/register/patient")
    public ResponseEntity<PatientResponseDto> registerPatient(@Valid @RequestBody PatientRegistrationDto patientRegistrationDto) {
        return new ResponseEntity<>(userService.registerPatient(patientRegistrationDto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Register a new admin",
            description = "Register a new admin account. This is restricted and only can be in email in configuration.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Admin registration details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRegistrationDto.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Admin registered successfully."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/register/admin")
    public ResponseEntity<Void> registerAdmin(@Valid @RequestBody UserRegistrationDto userRegistrationDto) {
        userService.registerAdmin(userRegistrationDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Update device token for push notifications")
    @PatchMapping("/{userId}/device-token")
    public ResponseEntity<Void> updateDeviceToken(
            @PathVariable UUID userId,
            @Valid @RequestBody DeviceTokenUpdateDto dto
    ) {
        userService.updateDeviceToken(dto.deviceToken(), userId);
        return ResponseEntity.ok().build();
    }
}
