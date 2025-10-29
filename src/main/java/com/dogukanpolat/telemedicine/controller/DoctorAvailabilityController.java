package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.availability.AvailabilityRequestDto;
import com.dogukanpolat.telemedicine.dto.availability.AvailabilityResponseDto;
import com.dogukanpolat.telemedicine.dto.availability.AvailabilityUpdateDto;
import com.dogukanpolat.telemedicine.dto.availability.BulkAvailabilityRequestDto;
import com.dogukanpolat.telemedicine.model.enums.DayOfWeek;
import com.dogukanpolat.telemedicine.service.DoctorAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/availability")
@RequiredArgsConstructor
@Tag(name = "Doctor Availability", description = "Endpoints for managing doctor availability schedules")
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService doctorAvailabilityService;

    @Operation(
            summary = "Get doctor's availability",
            description = "Get all availability slots for a specific doctor.",
            parameters = @Parameter(description = "Doctor ID", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Availability slots retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AvailabilityResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or expired JWT"
            )
    })
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AvailabilityResponseDto>> getDoctorAvailability(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(doctorAvailabilityService.getDoctorAvailability(doctorId));
    }

    @Operation(
            summary = "Get doctor's availability by day",
            description = "Retrieve availability slots for a specific doctor on a specific day of the week",
            parameters = {
                    @Parameter(description = "Doctor ID", required = true),
                    @Parameter(description = "Day of week (MONDAY, TUESDAY, etc.)", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Availability slots retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AvailabilityResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or expired JWT"
            )
    })
    @GetMapping("/doctor/{doctorId}/day/{dayOfWeek}")
    public ResponseEntity<List<AvailabilityResponseDto>> getDoctorAvailabilityByDay(@PathVariable UUID doctorId, @PathVariable DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(doctorAvailabilityService.getDoctorAvailabilityByDay(doctorId, dayOfWeek));
    }

    @Operation(
            summary = "Update availability slot",
            description = "Update an existing availability slot. Only doctors can update their own availability.",
            parameters = @Parameter(description = "Doctor ID", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Active availability slots retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AvailabilityResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or expired JWT"
            )
    })
    @GetMapping("/doctor/{doctorId}/active")
    public ResponseEntity<List<AvailabilityResponseDto>> getActiveAvailability(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(doctorAvailabilityService.getActiveAvailability(doctorId));
    }

    @Operation(
            summary = "Create availability slot",
            description = "Create a new availability time slot for a doctor. Only doctors can create their own availability.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Availability request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AvailabilityRequestDto.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Availability slot created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AvailabilityResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - overlapping slots or invalid time range"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only doctors can create availability"
            )
    })
    @PostMapping
    public ResponseEntity<AvailabilityResponseDto> createAvailability(@Valid @RequestBody AvailabilityRequestDto request){
        return new ResponseEntity<>(doctorAvailabilityService.createAvailability(request), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Create multiple availability slots",
            description = "Create multiple availability time slots for a doctor at once. Only doctors can create their own availability.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Bulk Availability request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BulkAvailabilityRequestDto.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Availability slots created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AvailabilityResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - overlapping slots or invalid time range"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only doctors can create availability"
            )
    })
    @PostMapping("/bulk")
    public ResponseEntity<List<AvailabilityResponseDto>> createBulkAvailability(
            @Valid @RequestBody BulkAvailabilityRequestDto request) {
        return new ResponseEntity<>(doctorAvailabilityService.createBulkAvailability(request), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Update availability slot",
            description = "Update an existing availability slot. Only doctors can update their own availability.",
            parameters = @Parameter(description = "Availability ID", required = true),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Availability update request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AvailabilityUpdateDto.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Availability updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AvailabilityResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - overlapping slots or invalid time range"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only doctors can update availability"
            )
    })
    @PatchMapping("/{availabilityId}")
    public ResponseEntity<AvailabilityResponseDto> updateAvailability(
            @PathVariable UUID availabilityId,
            @Valid @RequestBody AvailabilityUpdateDto updateDto
            ) {
        return ResponseEntity.ok(doctorAvailabilityService.updateAvailability(availabilityId, updateDto));
    }

    @Operation(
            summary = "Delete all doctor availability",
            description = "Delete all availability slots for a doctor. Only doctors can delete their own availability.",
            parameters = @Parameter(description = "Availability ID", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "All availability slots deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only doctors can delete their own availability"
            )
    })
    @DeleteMapping("/{availabilityId}")
    public ResponseEntity<Void> deleteAvailability(
            @PathVariable UUID availabilityId) {
        doctorAvailabilityService.deleteAvailability(availabilityId);
        return ResponseEntity.noContent().build();
    }

}
