package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.admin.AdminStatsResponseDto;
import com.dogukanpolat.telemedicine.dto.admin.AiTriageAuditDto;
import com.dogukanpolat.telemedicine.dto.admin.UserManagementDto;
import com.dogukanpolat.telemedicine.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administrative endpoints for system management (Admin role required)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final AdminService adminService;

    @Operation(
            summary = "Get all users",
            description = "Retrieve a list of all registered users in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserManagementDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required"
            )
    })
    @GetMapping("/users")
    public ResponseEntity<List<UserManagementDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @Operation(
            summary = "Get all patients",
            description = "Retrieve a list of all registered patients in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Patients retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserManagementDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required"
            )
    })
    @GetMapping("/patients")
    public ResponseEntity<List<UserManagementDto>> getAllPatients() {
        return ResponseEntity.ok(adminService.getAllPatients());
    }

    @Operation(
            summary = "Get all doctors",
            description = "Retrieve a list of all registered doctors in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Doctors retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserManagementDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required"
            )
    })
    @GetMapping("/doctors")
    public ResponseEntity<List<UserManagementDto>> getAllDoctors() {
        return ResponseEntity.ok(adminService.getAllDoctors());
    }

    @Operation(
            summary = "Get system statistics",
            description = """
                    Retrieve comprehensive system statistics including:
                    - Total users count
                    - Total doctors count
                    - Total patients count
                    - Total appointments count
                    - Active users count
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statistics retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdminStatsResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required"
            )
    })
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponseDto> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    @Operation(
            summary = "Get AI triage audit",
            description = "Retrieve a list of all AI triage audits."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Triage audits retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AiTriageAuditDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required"
            )
    })
    @GetMapping("/triages")
    public ResponseEntity<List<AiTriageAuditDto>> getAiTriageAudit() {
        return ResponseEntity.ok(adminService.getAiTriageAudit());
    }

    @Operation(
            summary = "Get triage audits by patient ID",
            description = "Retrieve a list of all AI triage audits for a specific patient.",
            parameters = @Parameter(description = "Patient ID", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Patient triage audits retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AiTriageAuditDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required"
            )
    })
    @GetMapping("/triage-audits/patient/{patientId}")
    public ResponseEntity<List<AiTriageAuditDto>> getPatientTriageAudits(@PathVariable UUID patientId) {
        return ResponseEntity.ok(adminService.getTriageAuditsByPatient(patientId));
    }

    @Operation(
            summary = "Get triage audits by urgency level",
            description = "Filter triage audits by urgency level (LOW, MEDIUM, HIGH, EMERGENCY)",
            parameters = @Parameter(description = "Urgency level", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Filtered triage audits retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AiTriageAuditDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required"
            )
    })
    @GetMapping("/triage-audits/urgency/{urgency}")
    public ResponseEntity<List<AiTriageAuditDto>> getTriageAuditsByUrgency(@PathVariable String urgency) {
        return ResponseEntity.ok(adminService.getTriageAuditsByUrgency(urgency));
    }

    @Operation(
            summary = "Deactivate a user account",
            description = "Disable a user account",
            parameters = @Parameter(description = "User email", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User deactivated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserManagementDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required"
            )
    })
    @PatchMapping("/users/{email}/deactivate")
    public ResponseEntity<UserManagementDto> deactivateUser(@PathVariable String email) {
        return new ResponseEntity<>(adminService.toggleUserActivity(email, false), HttpStatus.OK);
    }

    @Operation(
            summary = "Activate a user account",
            description = "Enable a user account",
            parameters = @Parameter(description = "User email", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User activated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserManagementDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required"
            )
    })
    @PatchMapping("/users/{email}/activate")
    public ResponseEntity<UserManagementDto> activateUser(@PathVariable String email) {
        return new ResponseEntity<>(adminService.toggleUserActivity(email, true), HttpStatus.OK);
    }

    @Operation(
            summary = "Verify a doctor",
            description = "Mark a doctor as verified",
            parameters = @Parameter(description = "Medical license number", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Doctor verified successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required"
            )
    })
    @PatchMapping("/doctors/{medicalLicenseNumber}/verify")
    public ResponseEntity<Void> verifyDoctor(@PathVariable String medicalLicenseNumber) {
        adminService.verifyDoctor(medicalLicenseNumber, true);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Unverify a doctor",
            description = "Mark a doctor as unverified",
            parameters = @Parameter(description = "Medical license number", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Doctor unverified successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required"
            )
    })
    @PatchMapping("/doctors/{medicalLicenseNumber}/unverify")
    public ResponseEntity<Void> unverifyDoctor(@PathVariable String medicalLicenseNumber) {
        adminService.verifyDoctor(medicalLicenseNumber, false);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Delete a user",
            description = "Delete a user account",
            parameters = @Parameter(description = "User email", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "User deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required"
            )
    })
    @DeleteMapping("/users/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable String email) {
        adminService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }

}
