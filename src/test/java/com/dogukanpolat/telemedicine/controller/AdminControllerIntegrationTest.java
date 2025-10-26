package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.admin.AdminStatsResponseDto;
import com.dogukanpolat.telemedicine.dto.admin.AiTriageAuditDto;
import com.dogukanpolat.telemedicine.dto.admin.UserManagementDto;
import com.dogukanpolat.telemedicine.security.JwtAuthenticationFilter;
import com.dogukanpolat.telemedicine.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        ))
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    private UserManagementDto patientDto;
    private UserManagementDto doctorDto;
    private UserManagementDto adminDto;
    private AdminStatsResponseDto statsDto;
    private AiTriageAuditDto auditDto;

    @BeforeEach
    void setUp() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        patientDto = new UserManagementDto(
                patientId,
                "patient@test.com",
                "John",
                "Doe",
                "PATIENT",
                true,
                LocalDate.now()
        );

        doctorDto = new UserManagementDto(
                doctorId,
                "doctor@test.com",
                "Jane",
                "Smith",
                "DOCTOR",
                true,
                LocalDate.now()
        );

        adminDto = new UserManagementDto(
                adminId,
                "admin@test.com",
                "Admin",
                "User",
                "ADMIN",
                true,
                LocalDate.now()
        );

        statsDto = new AdminStatsResponseDto(
                3L,
                1L,
                1L,
                10L,
                3L
        );

        auditDto = new AiTriageAuditDto(
                UUID.randomUUID(),
                patientId,
                "John Doe",
                "Chest pain and shortness of breath",
                "Emergency level symptoms detected",
                "EMERGENCY",
                OffsetDateTime.now()
        );
    }

    @Test
    void getAllUsers_ShouldReturn200WithUserList() throws Exception {
        // Given
        List<UserManagementDto> users = List.of(patientDto, doctorDto, adminDto);
        when(adminService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].email").value("patient@test.com"))
                .andExpect(jsonPath("$[0].role").value("PATIENT"))
                .andExpect(jsonPath("$[1].email").value("doctor@test.com"))
                .andExpect(jsonPath("$[1].role").value("DOCTOR"))
                .andExpect(jsonPath("$[2].email").value("admin@test.com"))
                .andExpect(jsonPath("$[2].role").value("ADMIN"));

        verify(adminService).getAllUsers();
    }

    @Test
    void getAllUsers_WhenNoUsers_ShouldReturn200WithEmptyList() throws Exception {
        // Given
        when(adminService.getAllUsers()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(adminService).getAllUsers();
    }

    @Test
    void getAllPatients_ShouldReturn200WithPatientList() throws Exception {
        // Given
        List<UserManagementDto> patients = List.of(patientDto);
        when(adminService.getAllPatients()).thenReturn(patients);

        // When & Then
        mockMvc.perform(get("/admin/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("patient@test.com"))
                .andExpect(jsonPath("$[0].role").value("PATIENT"))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[0].isActive").value(true));

        verify(adminService).getAllPatients();
    }

    @Test
    void getAllPatients_WhenNoPatients_ShouldReturn200WithEmptyList() throws Exception {
        // Given
        when(adminService.getAllPatients()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/admin/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(adminService).getAllPatients();
    }

    @Test
    void getAllDoctors_ShouldReturn200WithDoctorList() throws Exception {
        // Given
        List<UserManagementDto> doctors = List.of(doctorDto);
        when(adminService.getAllDoctors()).thenReturn(doctors);

        // When & Then
        mockMvc.perform(get("/admin/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("doctor@test.com"))
                .andExpect(jsonPath("$[0].role").value("DOCTOR"))
                .andExpect(jsonPath("$[0].firstName").value("Jane"))
                .andExpect(jsonPath("$[0].lastName").value("Smith"));

        verify(adminService).getAllDoctors();
    }

    @Test
    void getAllDoctors_WhenNoDoctors_ShouldReturn200WithEmptyList() throws Exception {
        // Given
        when(adminService.getAllDoctors()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/admin/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(adminService).getAllDoctors();
    }

    @Test
    void getSystemStats_ShouldReturn200WithStatistics() throws Exception {
        // Given
        when(adminService.getSystemStats()).thenReturn(statsDto);

        // When & Then
        mockMvc.perform(get("/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(3))
                .andExpect(jsonPath("$.totalDoctors").value(1))
                .andExpect(jsonPath("$.totalPatients").value(1))
                .andExpect(jsonPath("$.totalAppointments").value(10))
                .andExpect(jsonPath("$.activeUsers").value(3));

        verify(adminService).getSystemStats();
    }

    @Test
    void getSystemStats_WithZeroData_ShouldReturn200() throws Exception {
        // Given
        AdminStatsResponseDto emptyStats = new AdminStatsResponseDto(0L, 0L, 0L, 0L, 0L);
        when(adminService.getSystemStats()).thenReturn(emptyStats);

        // When & Then
        mockMvc.perform(get("/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(0))
                .andExpect(jsonPath("$.totalDoctors").value(0))
                .andExpect(jsonPath("$.totalPatients").value(0))
                .andExpect(jsonPath("$.totalAppointments").value(0))
                .andExpect(jsonPath("$.activeUsers").value(0));
    }

    @Test
    void getAiTriageAudit_ShouldReturn200WithAuditList() throws Exception {
        // Given
        List<AiTriageAuditDto> audits = List.of(auditDto);
        when(adminService.getAiTriageAudit()).thenReturn(audits);

        // When & Then
        mockMvc.perform(get("/admin/triages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].patientName").value("John Doe"))
                .andExpect(jsonPath("$[0].userInput").value("Chest pain and shortness of breath"))
                .andExpect(jsonPath("$[0].urgencyLevel").value("EMERGENCY"));

        verify(adminService).getAiTriageAudit();
    }

    @Test
    void getAiTriageAudit_WhenNoAudits_ShouldReturn200WithEmptyList() throws Exception {
        // Given
        when(adminService.getAiTriageAudit()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/admin/triages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(adminService).getAiTriageAudit();
    }

    @Test
    void getPatientTriageAudits_ShouldReturn200WithFilteredAudits() throws Exception {
        // Given
        UUID patientId = UUID.randomUUID();
        List<AiTriageAuditDto> audits = List.of(auditDto);
        when(adminService.getTriageAuditsByPatient(patientId)).thenReturn(audits);

        // When & Then
        mockMvc.perform(get("/admin/triage-audits/patient/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].patientName").value("John Doe"))
                .andExpect(jsonPath("$[0].urgencyLevel").value("EMERGENCY"));

        verify(adminService).getTriageAuditsByPatient(patientId);
    }

    @Test
    void getPatientTriageAudits_WhenNoAuditsForPatient_ShouldReturn200WithEmptyList() throws Exception {
        // Given
        UUID patientId = UUID.randomUUID();
        when(adminService.getTriageAuditsByPatient(patientId)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/admin/triage-audits/patient/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(adminService).getTriageAuditsByPatient(patientId);
    }

    @Test
    void getTriageAuditsByUrgency_ShouldReturn200WithFilteredAudits() throws Exception {
        // Given
        String urgency = "EMERGENCY";
        List<AiTriageAuditDto> audits = List.of(auditDto);
        when(adminService.getTriageAuditsByUrgency(urgency)).thenReturn(audits);

        // When & Then
        mockMvc.perform(get("/admin/triage-audits/urgency/{urgency}", urgency))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].urgencyLevel").value("EMERGENCY"));

        verify(adminService).getTriageAuditsByUrgency(urgency);
    }

    @Test
    void getTriageAuditsByUrgency_WithLowercaseUrgency_ShouldWork() throws Exception {
        // Given
        String urgency = "emergency";
        List<AiTriageAuditDto> audits = List.of(auditDto);
        when(adminService.getTriageAuditsByUrgency(urgency)).thenReturn(audits);

        // When & Then
        mockMvc.perform(get("/admin/triage-audits/urgency/{urgency}", urgency))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(adminService).getTriageAuditsByUrgency(urgency);
    }

    @Test
    void getTriageAuditsByUrgency_WithInvalidUrgency_ShouldReturn500() throws Exception {
        // Given
        String invalidUrgency = "INVALID";
        when(adminService.getTriageAuditsByUrgency(invalidUrgency))
                .thenThrow(new IllegalArgumentException("Invalid urgency level"));

        // When & Then
        mockMvc.perform(get("/admin/triage-audits/urgency/{urgency}", invalidUrgency))
                .andExpect(status().isInternalServerError());

        verify(adminService).getTriageAuditsByUrgency(invalidUrgency);
    }

    @Test
    void deactivateUser_ShouldReturn200WithUpdatedUser() throws Exception {
        // Given
        String email = "patient@test.com";
        UserManagementDto deactivatedUser = new UserManagementDto(
                patientDto.id(),
                patientDto.email(),
                patientDto.firstName(),
                patientDto.lastName(),
                patientDto.role(),
                false,
                patientDto.createdAt()
        );
        when(adminService.toggleUserActivity(email, false)).thenReturn(deactivatedUser);

        // When & Then
        mockMvc.perform(patch("/admin/users/{email}/deactivate", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.isActive").value(false));

        verify(adminService).toggleUserActivity(email, false);
    }

    @Test
    void deactivateUser_WithNonExistentEmail_ShouldReturn500() throws Exception {
        // Given
        String email = "nonexistent@test.com";
        when(adminService.toggleUserActivity(email, false))
                .thenThrow(new RuntimeException("User with email " + email + " not found"));

        // When & Then
        mockMvc.perform(patch("/admin/users/{email}/deactivate", email))
                .andExpect(status().isInternalServerError());

        verify(adminService).toggleUserActivity(email, false);
    }

    @Test
    void activateUser_ShouldReturn200WithUpdatedUser() throws Exception {
        // Given
        String email = "patient@test.com";
        when(adminService.toggleUserActivity(email, true)).thenReturn(patientDto);

        // When & Then
        mockMvc.perform(patch("/admin/users/{email}/activate", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(adminService).toggleUserActivity(email, true);
    }

    @Test
    void activateUser_WithNonExistentEmail_ShouldReturn500() throws Exception {
        // Given
        String email = "nonexistent@test.com";
        when(adminService.toggleUserActivity(email, true))
                .thenThrow(new RuntimeException("User with email " + email + " not found"));

        // When & Then
        mockMvc.perform(patch("/admin/users/{email}/activate", email))
                .andExpect(status().isInternalServerError());

        verify(adminService).toggleUserActivity(email, true);
    }

    @Test
    void verifyDoctor_ShouldReturn200() throws Exception {
        // Given
        String licenseNumber = "LICENSE123";
        doNothing().when(adminService).verifyDoctor(licenseNumber, true);

        // When & Then
        mockMvc.perform(patch("/admin/doctors/{medicalLicenseNumber}/verify", licenseNumber))
                .andExpect(status().isOk());

        verify(adminService).verifyDoctor(licenseNumber, true);
    }

    @Test
    void verifyDoctor_WithNonExistentLicense_ShouldReturn500() throws Exception {
        // Given
        String licenseNumber = "INVALID123";
        doThrow(new IllegalArgumentException("Doctor not found with medical license number " + licenseNumber))
                .when(adminService).verifyDoctor(licenseNumber, true);

        // When & Then
        mockMvc.perform(patch("/admin/doctors/{medicalLicenseNumber}/verify", licenseNumber))
                .andExpect(status().isInternalServerError());

        verify(adminService).verifyDoctor(licenseNumber, true);
    }

    @Test
    void unverifyDoctor_ShouldReturn200() throws Exception {
        // Given
        String licenseNumber = "LICENSE123";
        doNothing().when(adminService).verifyDoctor(licenseNumber, false);

        // When & Then
        mockMvc.perform(patch("/admin/doctors/{medicalLicenseNumber}/unverify", licenseNumber))
                .andExpect(status().isOk());

        verify(adminService).verifyDoctor(licenseNumber, false);
    }

    @Test
    void unverifyDoctor_WithNonExistentLicense_ShouldReturn500() throws Exception {
        // Given
        String licenseNumber = "INVALID123";
        doThrow(new IllegalArgumentException("Doctor not found with medical license number " + licenseNumber))
                .when(adminService).verifyDoctor(licenseNumber, false);

        // When & Then
        mockMvc.perform(patch("/admin/doctors/{medicalLicenseNumber}/unverify", licenseNumber))
                .andExpect(status().isInternalServerError());

        verify(adminService).verifyDoctor(licenseNumber, false);
    }

    @Test
    void deleteUser_ShouldReturn204() throws Exception {
        // Given
        String email = "patient@test.com";
        doNothing().when(adminService).deleteUser(email);

        // When & Then
        mockMvc.perform(delete("/admin/users/{email}", email))
                .andExpect(status().isNoContent());

        verify(adminService).deleteUser(email);
    }

    @Test
    void deleteUser_WithNonExistentEmail_ShouldReturn500() throws Exception {
        // Given
        String email = "nonexistent@test.com";
        doThrow(new RuntimeException("User with email " + email + " not found"))
                .when(adminService).deleteUser(email);

        // When & Then
        mockMvc.perform(delete("/admin/users/{email}", email))
                .andExpect(status().isInternalServerError());

        verify(adminService).deleteUser(email);
    }

    @Test
    void getAllUsers_ShouldReturnUsersWithAllFields() throws Exception {
        // Given
        List<UserManagementDto> users = List.of(patientDto);
        when(adminService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].email").value("patient@test.com"))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[0].role").value("PATIENT"))
                .andExpect(jsonPath("$[0].isActive").value(true))
                .andExpect(jsonPath("$[0].createdAt").exists());
    }

    @Test
    void getSystemStats_ShouldHandleLargeNumbers() throws Exception {
        // Given
        AdminStatsResponseDto largeStats = new AdminStatsResponseDto(
                10000L,
                5000L,
                4000L,
                50000L,
                9500L
        );
        when(adminService.getSystemStats()).thenReturn(largeStats);

        // When & Then
        mockMvc.perform(get("/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10000))
                .andExpect(jsonPath("$.totalDoctors").value(5000))
                .andExpect(jsonPath("$.totalPatients").value(4000))
                .andExpect(jsonPath("$.totalAppointments").value(50000))
                .andExpect(jsonPath("$.activeUsers").value(9500));
    }

    @Test
    void getTriageAuditsByUrgency_ForAllUrgencyLevels_ShouldWork() throws Exception {
        // Test all valid urgency levels
        String[] urgencyLevels = {"LOW", "MEDIUM", "HIGH", "EMERGENCY"};

        for (String urgency : urgencyLevels) {
            when(adminService.getTriageAuditsByUrgency(urgency)).thenReturn(List.of(auditDto));

            mockMvc.perform(get("/admin/triage-audits/urgency/{urgency}", urgency))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(adminService).getTriageAuditsByUrgency(urgency);
        }
    }

    @Test
    void getAiTriageAudit_ShouldReturnAuditsWithAllFields() throws Exception {
        // Given
        List<AiTriageAuditDto> audits = List.of(auditDto);
        when(adminService.getAiTriageAudit()).thenReturn(audits);

        // When & Then
        mockMvc.perform(get("/admin/triages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].patientId").exists())
                .andExpect(jsonPath("$[0].patientName").exists())
                .andExpect(jsonPath("$[0].userInput").exists())
                .andExpect(jsonPath("$[0].aiOutput").exists())
                .andExpect(jsonPath("$[0].urgencyLevel").exists())
                .andExpect(jsonPath("$[0].createdAt").exists());
    }

    @Test
    void activateUser_MultipleTimes_ShouldWork() throws Exception {
        // Given
        String email = "patient@test.com";
        when(adminService.toggleUserActivity(email, true)).thenReturn(patientDto);

        // When & Then - Call multiple times
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(patch("/admin/users/{email}/activate", email))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(true));
        }

        verify(adminService, times(3)).toggleUserActivity(email, true);
    }

    @Test
    void deactivateUser_MultipleTimes_ShouldWork() throws Exception {
        // Given
        String email = "patient@test.com";
        UserManagementDto deactivatedUser = new UserManagementDto(
                patientDto.id(),
                patientDto.email(),
                patientDto.firstName(),
                patientDto.lastName(),
                patientDto.role(),
                false,
                patientDto.createdAt()
        );
        when(adminService.toggleUserActivity(email, false)).thenReturn(deactivatedUser);

        // When & Then - Call multiple times
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(patch("/admin/users/{email}/deactivate", email))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(false));
        }

        verify(adminService, times(3)).toggleUserActivity(email, false);
    }

    @Test
    void getAllEndpoints_ShouldCallServiceOnce() throws Exception {
        // Given
        when(adminService.getAllUsers()).thenReturn(List.of());
        when(adminService.getAllPatients()).thenReturn(List.of());
        when(adminService.getAllDoctors()).thenReturn(List.of());

        // When
        mockMvc.perform(get("/admin/users")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/patients")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/doctors")).andExpect(status().isOk());

        // Then
        verify(adminService, times(1)).getAllUsers();
        verify(adminService, times(1)).getAllPatients();
        verify(adminService, times(1)).getAllDoctors();
    }

    @Test
    void verifyDoctor_ThenUnverify_ShouldWorkSequentially() throws Exception {
        // Given
        String licenseNumber = "LICENSE123";
        doNothing().when(adminService).verifyDoctor(eq(licenseNumber), anyBoolean());

        // When & Then - Verify
        mockMvc.perform(patch("/admin/doctors/{medicalLicenseNumber}/verify", licenseNumber))
                .andExpect(status().isOk());

        // When & Then - Unverify
        mockMvc.perform(patch("/admin/doctors/{medicalLicenseNumber}/unverify", licenseNumber))
                .andExpect(status().isOk());

        verify(adminService).verifyDoctor(licenseNumber, true);
        verify(adminService).verifyDoctor(licenseNumber, false);
    }
}