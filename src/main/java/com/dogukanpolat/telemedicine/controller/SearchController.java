package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.appointment.AppointmentResponseDto;
import com.dogukanpolat.telemedicine.dto.search.*;
import com.dogukanpolat.telemedicine.model.enums.AppointmentStatus;
import com.dogukanpolat.telemedicine.model.enums.DayOfWeek;
import com.dogukanpolat.telemedicine.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "Search & Filters", description = "Endpoints for advanced searching and filtering")
@SecurityRequirement(name = "bearerAuth")
public class SearchController {
    private final SearchService searchService;

    @Operation(
            summary = "Search doctors",
            description = """
                    Search for doctors using various criteria:
                    - Specialization (partial match)
                    - Fee range (min/max)
                    - Years of experience (minimum)
                    - Verification status
                    - Availability (day and time)
                    - Name (first or last name, partial match)
                    
                    All parameters are optional. Results can be filtered by multiple criteria simultaneously.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Doctors found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = DoctorSearchResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or expired JWT"
            )
    })
    @Parameters(value = {
            @Parameter(name = "specialization", description = "Doctor's specialization"),
            @Parameter(name = "minFee", description = "Doctor's minimum fee"),
            @Parameter(name = "maxFee", description = "Doctor's maximum fee"),
            @Parameter(name = "minExperience", description = "Doctor's minimum years of experience"),
            @Parameter(name = "isVerified", description = "Doctor's verification status"),
            @Parameter(name = "availableDay", description = "Doctor's availability day"),
            @Parameter(name = "availableStartTime", description = "Doctor's availability start time"),
            @Parameter(name = "availableEndTime", description = "Doctor's availability end time"),
            @Parameter(name = "name", description = "Doctor's name(First or last name. Searchs separate.)")
    })
    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorSearchResponseDto>> searchDoctors(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) BigDecimal minFee,
            @RequestParam(required = false) BigDecimal maxFee,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(required = false) DayOfWeek availableDay,
            @RequestParam(required = false) LocalTime availableStartTime,
            @RequestParam(required = false) LocalTime availableEndTime,
            @RequestParam(required = false) String name
            ) {
        DoctorSearchCriteria criteria = new DoctorSearchCriteria(
                specialization,
                minFee,
                maxFee,
                minExperience,
                isVerified,
                availableDay,
                availableStartTime,
                availableEndTime,
                name
        );

        return ResponseEntity.ok(searchService.searchDoctors(criteria));
    }



    @Operation(
            summary = "Search patients (Admin only)",
            description = """
                    Advanced patient search for administrators:
                    - Name (first or last name, partial match)
                    - Email (partial match)
                    - Blood type (exact match)
                    - Active status
                    - Phone number (partial match)
                    
                    All parameters are optional.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Patients found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PatientSearchResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or expired JWT"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required"
            )
    })
    @Parameters(value = {
            @Parameter(name = "name", description = "Patient's name(First or last name. Searchs separate.)"),
            @Parameter(name = "email", description = "Patient's email"),
            @Parameter(name = "bloodType", description = "Patient's blood type"),
            @Parameter(name = "isActive", description = "Patient's active status"),
            @Parameter(name = "phoneNumber", description = "Patient's phone number")
    })
    @GetMapping("/patients")
    public ResponseEntity<List<PatientSearchResponseDto>> searchPatients(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String bloodType,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String phoneNumber
    ) {

        PatientSearchCriteria criteria = new PatientSearchCriteria(
                name, email, bloodType, isActive, phoneNumber);

        return ResponseEntity.ok(searchService.searchPatients(criteria));
    }

    @Operation(
            summary = "Filter appointments",
            description = """
                    Filter appointments using various criteria:
                    - Patient ID
                    - Doctor ID
                    - Status (SCHEDULED, CONFIRMED, COMPLETED, CANCELLED)
                    - Date range (start and end dates)
                    
                    All parameters are optional. Results are ordered by scheduled date (descending).
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Appointments filtered successfully",
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
    @Parameters(value = {
            @Parameter(name = "patientId", description = "Patient ID"),
            @Parameter(name = "doctorId", description = "Doctor ID"),
            @Parameter(name = "status", description = "Appointment status"),
            @Parameter(name = "startDate", description = "Start date (yyyy-MM-dd)"),
            @Parameter(name = "endDate", description = "End date (yyyy-MM-dd)")
    })
    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> filterAppointments(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID doctorId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
            ) {
        AppointmentFilterCriteria criteria = new AppointmentFilterCriteria(
                patientId, doctorId, status, startDate, endDate
        );

        return ResponseEntity.ok(searchService.filterAppointments(criteria));
    }
}
