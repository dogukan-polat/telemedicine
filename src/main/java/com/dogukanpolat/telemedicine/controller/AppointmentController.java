package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.appointment.AppointmentRequestDto;
import com.dogukanpolat.telemedicine.dto.appointment.AppointmentResponseDto;
import com.dogukanpolat.telemedicine.model.enums.AppointmentStatus;
import com.dogukanpolat.telemedicine.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Endpoints for managing appointments")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {
    private final AppointmentService appointmentService;

    @Operation(
            summary = "Get appointments by patient ID",
            description = "Get appointments for a specific patient. Requires authentication.",
            parameters = @Parameter(name = "id", description = "Patient ID", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of appointments for the patient retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AppointmentResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or expired JWT"
            )
    })
    @GetMapping("/patient/{id}")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointmentsByPatientId(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatientId(id));
    }

    @Operation(
            summary = "Get appointments by doctor ID",
            description = "Get appointments for a specific doctor. Requires authentication.",
            parameters = @Parameter(name = "id", description = "Doctor ID", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of appointments for the doctor retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AppointmentResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or expired JWT"
            )
    })
    @GetMapping("/doctor/{id}")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointmentsByDoctorId(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctorId(id));
    }

    @Operation(
            summary = "Create an appointment",
            description = """
    Schedule a new appointment for patient and doctor. Only doctors can create appointments.
    Email notifications are sent automatically.""",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Appointment details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AppointmentRequestDto.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Appointment created successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AppointmentResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only doctors can create appointments."
            )
    })
    @PostMapping
    public ResponseEntity<AppointmentResponseDto> createAppointment(@RequestBody @Valid AppointmentRequestDto appointmentRequest) {
        return ResponseEntity.ok(appointmentService.createAppointment(appointmentRequest));
    }

    @Operation(
            summary = "Cancel an appointment",
            description = "Change appointment status to CANCELLED.",
            parameters = @Parameter(name = "id", description = "Appointment ID", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Appointment cancelled successfully",
                    content = @Content(schema = @Schema(implementation = AppointmentResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or expired JWT"
            )
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDto> cancelAppointment(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.changeAppointmentStatus(id, AppointmentStatus.CANCELLED));
    }

    @Operation(
            summary = "Confirm an appointment",
            description = "Change appointment status to CONFIRMED.",
            parameters = @Parameter(name = "id", description = "Appointment ID", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Appointment confirmed successfully",
                    content = @Content(schema = @Schema(implementation = AppointmentResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or expired JWT"
            )
    })
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponseDto> confirmAppointment(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.changeAppointmentStatus(id, AppointmentStatus.CONFIRMED));
    }

    @Operation(
            summary = "Complete an appointment",
            description = "Change appointment status to COMPLETED.",
            parameters = @Parameter(name = "id", description = "Appointment ID", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Appointment completed successfully",
                    content = @Content(schema = @Schema(implementation = AppointmentResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or expired JWT"
            )
    })
    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponseDto> completeAppointment(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.changeAppointmentStatus(id, AppointmentStatus.COMPLETED));
    }

    @Operation(
            summary = "Delete an appointment",
            description = "Delete an appointment by ID.",
            parameters = @Parameter(name = "id", description = "Appointment ID", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Appointment deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only admins can delete appointments."
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable UUID id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
