package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.admin.AdminStatsResponseDto;
import com.dogukanpolat.telemedicine.dto.admin.AiTriageAuditDto;
import com.dogukanpolat.telemedicine.dto.admin.UserManagementDto;
import com.dogukanpolat.telemedicine.mappers.AiTriageMapper;
import com.dogukanpolat.telemedicine.mappers.UserMapper;
import com.dogukanpolat.telemedicine.model.AiTriageAudit;
import com.dogukanpolat.telemedicine.model.Doctor;
import com.dogukanpolat.telemedicine.model.Patient;
import com.dogukanpolat.telemedicine.model.UserModel;
import com.dogukanpolat.telemedicine.model.enums.Role;
import com.dogukanpolat.telemedicine.model.enums.UrgencyLevel;
import com.dogukanpolat.telemedicine.repository.AiTriageAuditRepository;
import com.dogukanpolat.telemedicine.repository.AppointmentRepository;
import com.dogukanpolat.telemedicine.repository.DoctorRepository;
import com.dogukanpolat.telemedicine.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AiTriageAuditRepository auditRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AiTriageMapper aiTriageMapper;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private AdminService adminService;

    private UserModel testPatient;
    private UserModel testDoctor;
    private UserModel testAdmin;
    private UserManagementDto patientDto;
    private UserManagementDto doctorDto;
    private UserManagementDto adminDto;

    @BeforeEach
    void setUp() {
        testPatient = new UserModel();
        testPatient.setId(UUID.randomUUID());
        testPatient.setEmail("patient@test.com");
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setRole(Role.PATIENT);
        testPatient.setIsActive(true);
        testPatient.setCreatedAt(LocalDate.now());

        patientDto = new UserManagementDto(
                testPatient.getId(),
                testPatient.getEmail(),
                testPatient.getFirstName(),
                testPatient.getLastName(),
                "PATIENT",
                true,
                testPatient.getCreatedAt()
        );

        testDoctor = new UserModel();
        testDoctor.setId(UUID.randomUUID());
        testDoctor.setEmail("doctor@test.com");
        testDoctor.setFirstName("Jane");
        testDoctor.setLastName("Smith");
        testDoctor.setRole(Role.DOCTOR);
        testDoctor.setIsActive(true);
        testDoctor.setCreatedAt(LocalDate.now());

        doctorDto = new UserManagementDto(
                testDoctor.getId(),
                testDoctor.getEmail(),
                testDoctor.getFirstName(),
                testDoctor.getLastName(),
                "DOCTOR",
                true,
                testDoctor.getCreatedAt()
        );

        testAdmin = new UserModel();
        testAdmin.setId(UUID.randomUUID());
        testAdmin.setEmail("admin@test.com");
        testAdmin.setFirstName("Admin");
        testAdmin.setLastName("User");
        testAdmin.setRole(Role.ADMIN);
        testAdmin.setIsActive(true);
        testAdmin.setCreatedAt(LocalDate.now());

        adminDto = new UserManagementDto(
                testAdmin.getId(),
                testAdmin.getEmail(),
                testAdmin.getFirstName(),
                testAdmin.getLastName(),
                "ADMIN",
                true,
                testAdmin.getCreatedAt()
        );
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        List<UserModel> users = List.of(testPatient,testDoctor,testAdmin);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toUserManagementDto(testPatient)).thenReturn(patientDto);
        when(userMapper.toUserManagementDto(testDoctor)).thenReturn(doctorDto);
        when(userMapper.toUserManagementDto(testAdmin)).thenReturn(adminDto);

        List<UserManagementDto> result = adminService.getAllUsers();

        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(patientDto, doctorDto, adminDto);
        verify(userRepository).findAll();
        verify(userMapper, times(3)).toUserManagementDto(any(UserModel.class));
    }

    @Test
    void getAllUsers_WhenNoUsersExist_ShouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserManagementDto> result = adminService.getAllUsers();

        assertThat(result).isEmpty();
        verify(userRepository).findAll();
        verify(userMapper, never()).toUserManagementDto(any(UserModel.class));
    }

    @Test
    void getAllDoctors_ShouldReturnOnlyDoctors() {
        // Given
        List<UserModel> users = List.of(testPatient, testDoctor, testAdmin);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toUserManagementDto(testDoctor)).thenReturn(doctorDto);

        List<UserManagementDto> result = adminService.getAllDoctors();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().role()).isEqualTo("DOCTOR");
        assertThat(result.getFirst().email()).isEqualTo("doctor@test.com");
        verify(userRepository).findAll();
        verify(userMapper, times(1)).toUserManagementDto(testDoctor);
    }

    @Test
    void getAllDoctors_WhenNoDoctors_ShouldReturnEmptyList() {
        List<UserModel> users = List.of(testPatient, testAdmin);
        when(userRepository.findAll()).thenReturn(users);

        List<UserManagementDto> result = adminService.getAllDoctors();

        assertThat(result).isEmpty();
        verify(userRepository).findAll();
    }

    @Test
    void getAllPatients_ShouldReturnOnlyPatients() {
        List<UserModel> users = List.of(testPatient, testDoctor, testAdmin);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toUserManagementDto(testPatient)).thenReturn(patientDto);

        List<UserManagementDto> result = adminService.getAllPatients();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().role()).isEqualTo("PATIENT");
        assertThat(result.getFirst().email()).isEqualTo("patient@test.com");
        verify(userRepository).findAll();
        verify(userMapper, times(1)).toUserManagementDto(testPatient);
    }

    @Test
    void getAllPatients_WhenNoPatients_ShouldReturnEmptyList() {
        List<UserModel> users = List.of(testDoctor, testAdmin);
        when(userRepository.findAll()).thenReturn(users);

        List<UserManagementDto> result = adminService.getAllPatients();

        assertThat(result).isEmpty();
        verify(userRepository).findAll();
    }

    @Test
    void getSystemStats_ShouldReturnCorrectStatistics() {
        // Given
        List<UserModel> users = List.of(testPatient, testDoctor, testAdmin);
        when(userRepository.count()).thenReturn(3L);
        when(userRepository.findAll()).thenReturn(users);
        when(appointmentRepository.count()).thenReturn(10L);

        // When
        AdminStatsResponseDto result = adminService.getSystemStats();

        // Then
        assertThat(result.totalUsers()).isEqualTo(3L);
        assertThat(result.totalDoctors()).isEqualTo(1L);
        assertThat(result.totalPatients()).isEqualTo(1L);
        assertThat(result.totalAppointments()).isEqualTo(10L);
        assertThat(result.activeUsers()).isEqualTo(3L);
        verify(userRepository).count();
        verify(appointmentRepository).count();
    }

    @Test
    void getSystemStats_WithInactiveUsers_ShouldCountOnlyActiveUsers() {
        // Given
        testPatient.setIsActive(false);
        List<UserModel> users = List.of(testPatient, testDoctor, testAdmin);
        when(userRepository.count()).thenReturn(3L);
        when(userRepository.findAll()).thenReturn(users);
        when(appointmentRepository.count()).thenReturn(5L);

        // When
        AdminStatsResponseDto result = adminService.getSystemStats();

        // Then
        assertThat(result.totalUsers()).isEqualTo(3L);
        assertThat(result.activeUsers()).isEqualTo(2L);
        verify(userRepository, times(3)).findAll();
    }

    @Test
    void getSystemStats_WithNoData_ShouldReturnZeros() {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.findAll()).thenReturn(List.of());
        when(appointmentRepository.count()).thenReturn(0L);

        // When
        AdminStatsResponseDto result = adminService.getSystemStats();

        // Then
        assertThat(result.totalUsers()).isZero();
        assertThat(result.totalDoctors()).isZero();
        assertThat(result.totalPatients()).isZero();
        assertThat(result.totalAppointments()).isZero();
        assertThat(result.activeUsers()).isZero();
    }

    @Test
    void getAiTriageAudit_ShouldReturnAllAudits() {
        // Given
        AiTriageAudit audit1 = createAudit(UrgencyLevel.LOW);
        AiTriageAudit audit2 = createAudit(UrgencyLevel.HIGH);

        AiTriageAuditDto auditDto1 = createAuditDto(audit1);
        AiTriageAuditDto auditDto2 = createAuditDto(audit2);

        when(auditRepository.findAll()).thenReturn(List.of(audit1, audit2));
        when(aiTriageMapper.toAiTriageAuditDto(audit1)).thenReturn(auditDto1);
        when(aiTriageMapper.toAiTriageAuditDto(audit2)).thenReturn(auditDto2);

        // When
        List<AiTriageAuditDto> result = adminService.getAiTriageAudit();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(auditDto1, auditDto2);
        verify(auditRepository).findAll();
    }

    @Test
    void getTriageAuditsByPatient_ShouldReturnOnlyPatientAudits() {
        // Given
        UUID patientId = UUID.randomUUID();
        Patient patient = new Patient();
        patient.setId(patientId);

        AiTriageAudit audit1 = createAudit(UrgencyLevel.LOW);
        audit1.setPatient(patient);

        AiTriageAudit audit2 = createAudit(UrgencyLevel.HIGH);
        Patient otherPatient = new Patient();
        otherPatient.setId(UUID.randomUUID());
        audit2.setPatient(otherPatient);

        AiTriageAuditDto auditDto1 = createAuditDto(audit1);

        when(auditRepository.findAll()).thenReturn(List.of(audit1, audit2));
        when(aiTriageMapper.toAiTriageAuditDto(audit1)).thenReturn(auditDto1);

        // When
        List<AiTriageAuditDto> result = adminService.getTriageAuditsByPatient(patientId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(auditDto1);
        verify(auditRepository).findAll();
    }

    @Test
    void getTriageAuditsByPatient_WhenNoAuditsForPatient_ShouldReturnEmptyList() {
        // Given
        UUID patientId = UUID.randomUUID();
        when(auditRepository.findAll()).thenReturn(List.of());

        // When
        List<AiTriageAuditDto> result = adminService.getTriageAuditsByPatient(patientId);

        // Then
        assertThat(result).isEmpty();
        verify(auditRepository).findAll();
    }

    @Test
    void getTriageAuditsByUrgency_ShouldReturnMatchingAudits() {
        // Given
        AiTriageAudit audit1 = createAudit(UrgencyLevel.HIGH);
        AiTriageAudit audit2 = createAudit(UrgencyLevel.HIGH);
        AiTriageAudit audit3 = createAudit(UrgencyLevel.LOW);

        AiTriageAuditDto auditDto1 = createAuditDto(audit1);
        AiTriageAuditDto auditDto2 = createAuditDto(audit2);

        when(auditRepository.findAll()).thenReturn(List.of(audit1, audit2, audit3));
        when(aiTriageMapper.toAiTriageAuditDto(audit1)).thenReturn(auditDto1);
        when(aiTriageMapper.toAiTriageAuditDto(audit2)).thenReturn(auditDto2);

        // When
        List<AiTriageAuditDto> result = adminService.getTriageAuditsByUrgency("HIGH");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(auditDto1, auditDto2);
        verify(auditRepository).findAll();
    }

    @Test
    void getTriageAuditsByUrgency_CaseInsensitive_ShouldWork() {
        // Given
        AiTriageAudit audit = createAudit(UrgencyLevel.EMERGENCY);
        AiTriageAuditDto auditDto = createAuditDto(audit);

        when(auditRepository.findAll()).thenReturn(List.of(audit));
        when(aiTriageMapper.toAiTriageAuditDto(audit)).thenReturn(auditDto);

        // When
        List<AiTriageAuditDto> result = adminService.getTriageAuditsByUrgency("emergency");

        // Then
        assertThat(result).hasSize(1);
        verify(auditRepository).findAll();
    }

    @Test
    void getTriageAuditsByUrgency_WithInvalidUrgency_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> adminService.getTriageAuditsByUrgency("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toggleUserActivity_Deactivate_ShouldUpdateUser() {
        // Given
        String email = "patient@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testPatient));
        when(userRepository.save(testPatient)).thenReturn(testPatient);
        when(userMapper.toUserManagementDto(testPatient)).thenReturn(patientDto);

        // When
        UserManagementDto result = adminService.toggleUserActivity(email, false);

        // Then
        assertThat(testPatient.getIsActive()).isFalse();
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(testPatient);
        verify(userMapper).toUserManagementDto(testPatient);
    }

    @Test
    void toggleUserActivity_Activate_ShouldUpdateUser() {
        // Given
        String email = "patient@test.com";
        testPatient.setIsActive(false);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testPatient));
        when(userRepository.save(testPatient)).thenReturn(testPatient);
        when(userMapper.toUserManagementDto(testPatient)).thenReturn(patientDto);

        // When
        UserManagementDto result = adminService.toggleUserActivity(email, true);

        // Then
        assertThat(testPatient.getIsActive()).isTrue();
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(testPatient);
    }

    @Test
    void toggleUserActivity_WithNonExistentEmail_ShouldThrowException() {
        // Given
        String email = "nonexistent@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adminService.toggleUserActivity(email, false))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User with email " + email + " not found");

        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyDoctor_ShouldUpdateDoctorVerification() {
        // Given
        String licenseNumber = "LICENSE123";
        Doctor doctor = new Doctor();
        doctor.setId(UUID.randomUUID());
        doctor.setMedicalLicenseNumber(licenseNumber);
        doctor.setIsVerified(false);

        when(doctorRepository.findByMedicalLicenseNumber(licenseNumber))
                .thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctor);

        // When
        adminService.verifyDoctor(licenseNumber, true);

        // Then
        assertThat(doctor.getIsVerified()).isTrue();
        verify(doctorRepository).findByMedicalLicenseNumber(licenseNumber);
        verify(doctorRepository).save(doctor);
    }

    @Test
    void verifyDoctor_Unverify_ShouldUpdateDoctorVerification() {
        // Given
        String licenseNumber = "LICENSE123";
        Doctor doctor = new Doctor();
        doctor.setId(UUID.randomUUID());
        doctor.setMedicalLicenseNumber(licenseNumber);
        doctor.setIsVerified(true);

        when(doctorRepository.findByMedicalLicenseNumber(licenseNumber))
                .thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctor);

        // When
        adminService.verifyDoctor(licenseNumber, false);

        // Then
        assertThat(doctor.getIsVerified()).isFalse();
        verify(doctorRepository).save(doctor);
    }

    @Test
    void verifyDoctor_WithNonExistentLicense_ShouldThrowException() {
        // Given
        String licenseNumber = "INVALID123";
        when(doctorRepository.findByMedicalLicenseNumber(licenseNumber))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adminService.verifyDoctor(licenseNumber, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Doctor not found with medical license number " + licenseNumber);

        verify(doctorRepository).findByMedicalLicenseNumber(licenseNumber);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void deleteUser_ShouldDeleteExistingUser() {
        // Given
        String email = "patient@test.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);
        doNothing().when(userRepository).deleteByEmail(email);

        // When
        adminService.deleteUser(email);

        // Then
        verify(userRepository).existsByEmail(email);
        verify(userRepository).deleteByEmail(email);
    }

    @Test
    void deleteUser_WithNonExistentEmail_ShouldThrowException() {
        // Given
        String email = "nonexistent@test.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> adminService.deleteUser(email))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User with email " + email + " not found");

        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).deleteByEmail(anyString());
    }

    @Test
    void getAllUsers_WithMixedActiveStatus_ShouldReturnAll() {
        // Given
        testPatient.setIsActive(false);
        List<UserModel> users = List.of(testPatient, testDoctor);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toUserManagementDto(testPatient)).thenReturn(patientDto);
        when(userMapper.toUserManagementDto(testDoctor)).thenReturn(doctorDto);

        // When
        List<UserManagementDto> result = adminService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
        verify(userRepository).findAll();
    }

    @Test
    void getTriageAuditsByPatient_WithNullPatient_ShouldBeExcluded() {
        // Given
        UUID patientId = UUID.randomUUID();
        AiTriageAudit auditWithPatient = createAudit(UrgencyLevel.LOW);
        Patient patient = new Patient();
        patient.setId(patientId);
        auditWithPatient.setPatient(patient);

        AiTriageAudit auditWithoutPatient = createAudit(UrgencyLevel.HIGH);
        auditWithoutPatient.setPatient(null);

        when(auditRepository.findAll()).thenReturn(List.of(auditWithPatient, auditWithoutPatient));
        when(aiTriageMapper.toAiTriageAuditDto(auditWithPatient))
                .thenReturn(createAuditDto(auditWithPatient));

        // When
        List<AiTriageAuditDto> result = adminService.getTriageAuditsByPatient(patientId);

        // Then
        assertThat(result).hasSize(1);
        verify(aiTriageMapper, times(1)).toAiTriageAuditDto(any());
    }

    // Helper methods
    private AiTriageAudit createAudit(UrgencyLevel urgency) {
        AiTriageAudit audit = new AiTriageAudit();
        audit.setId(UUID.randomUUID());
        audit.setUserInput("Test symptoms");
        audit.setAiOutput("Test analysis");
        audit.setUrgencyLevel(urgency);
        audit.setCreatedAt(OffsetDateTime.now());
        return audit;
    }

    private AiTriageAuditDto createAuditDto(AiTriageAudit audit) {
        return new AiTriageAuditDto(
                audit.getId(),
                audit.getPatient() != null ? audit.getPatient().getId() : null,
                audit.getPatient() != null ? "Patient Name" : null,
                audit.getUserInput(),
                audit.getAiOutput(),
                audit.getUrgencyLevel().toString(),
                audit.getCreatedAt()
        );
    }

}