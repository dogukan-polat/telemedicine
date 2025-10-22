package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.user.*;
import com.dogukanpolat.telemedicine.model.Doctor;
import com.dogukanpolat.telemedicine.model.Patient;
import com.dogukanpolat.telemedicine.model.UserModel;
import com.dogukanpolat.telemedicine.model.enums.Role;
import com.dogukanpolat.telemedicine.repository.DoctorRepository;
import com.dogukanpolat.telemedicine.repository.PatientRepository;
import com.dogukanpolat.telemedicine.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private DoctorRegistrationDto validDoctorRegistrationDto;
    private PatientRegistrationDto validPatientRegistrationDto;

    @BeforeEach
    void setUp() {
        PasswordDto passwordDto = new PasswordDto("password123", "password123");

        UserRegistrationDto validUserRegistrationDto = new UserRegistrationDto(
                "test@example.com",
                passwordDto,
                "John",
                "Doe",
                "+1234567890"
        );

        validDoctorRegistrationDto = new DoctorRegistrationDto(
                validUserRegistrationDto,
                "LICENSE123",
                "Cardiology",
                10,
                "Experienced cardiologist specializing in heart diseases",
                new BigDecimal("150.00")
        );

        validPatientRegistrationDto = new PatientRegistrationDto(
                validUserRegistrationDto,
                "Jane Smith",
                "+9876543210",
                "A+",
                List.of("Penicillin", "Peanuts")
        );
    }

    @AfterEach
    void tearDown() {
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerDoctor_WithValidData_ShouldReturnCreated() throws Exception {
        // When & Then
        mockMvc.perform(post("/users/register/doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDoctorRegistrationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"))
                .andExpect(jsonPath("$.specialization").value("Cardiology"))
                .andExpect(jsonPath("$.yearsOfExperience").value(10))
                .andReturn();

        // Verify database state
        List<UserModel> users = userRepository.findAll();
        assertThat(users).hasSize(1);
        assertThat(users.getFirst().getEmail()).isEqualTo("test@example.com");
        assertThat(users.getFirst().getRole()).isEqualTo(Role.DOCTOR);
        assertThat(passwordEncoder.matches("password123", users.getFirst().getPassword())).isTrue();

        List<Doctor> doctors = doctorRepository.findAll();
        assertThat(doctors).hasSize(1);
        assertThat(doctors.getFirst().getMedicalLicenseNumber()).isEqualTo("LICENSE123");
        assertThat(doctors.getFirst().getSpecialization()).isEqualTo("Cardiology");
        assertThat(doctors.getFirst().getYearsOfExperience()).isEqualTo(10);
        assertThat(doctors.getFirst().getConsultationFee()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void registerDoctor_WithDuplicateEmail_ShouldReturnBadRequest() throws Exception {
        // Given - Register first doctor
        mockMvc.perform(post("/users/register/doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDoctorRegistrationDto)))
                .andExpect(status().isCreated());

        // When & Then - Try to register with same email
        mockMvc.perform(post("/users/register/doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDoctorRegistrationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Duplicate User"))
                .andExpect(jsonPath("$.message").value("User with email test@example.com already exists"));

        // Verify only one user exists
        assertThat(userRepository.findAll()).hasSize(1);
        assertThat(doctorRepository.findAll()).hasSize(1);
    }

    @Test
    void registerDoctor_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        PasswordDto passwordDto = new PasswordDto("password123", "password123");
        UserRegistrationDto invalidUserDto = new UserRegistrationDto(
                "invalid-email",
                passwordDto,
                "John",
                "Doe",
                "+1234567890"
        );
        DoctorRegistrationDto invalidDoctorDto = new DoctorRegistrationDto(
                invalidUserDto,
                "LICENSE123",
                "Cardiology",
                10,
                "Biography",
                new BigDecimal("150.00")
        );

        // When & Then
        mockMvc.perform(post("/users/register/doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDoctorDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['user.email']").value("Must be a valid email"));

        // Verify no user was created
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void registerDoctor_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Given
        PasswordDto passwordDto = new PasswordDto("password123", "password123");
        UserRegistrationDto userDto = new UserRegistrationDto(
                "test@example.com",
                passwordDto,
                "John",
                "Doe",
                "+1234567890"
        );
        DoctorRegistrationDto invalidDoctorDto = new DoctorRegistrationDto(
                userDto,
                "",  // Empty license number
                "",  // Empty specialization
                10,
                "Biography",
                new BigDecimal("150.00")
        );

        // When & Then
        mockMvc.perform(post("/users/register/doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDoctorDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.medicalLicenseNumber").exists())
                .andExpect(jsonPath("$.specialization").exists());

        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void registerDoctor_WithInvalidExperience_ShouldReturnBadRequest() throws Exception {
        // Given
        PasswordDto passwordDto = new PasswordDto("password123", "password123");
        UserRegistrationDto userDto = new UserRegistrationDto(
                "test@example.com",
                passwordDto,
                "John",
                "Doe",
                "+1234567890"
        );
        DoctorRegistrationDto invalidDoctorDto = new DoctorRegistrationDto(
                userDto,
                "LICENSE123",
                "Cardiology",
                51,  // Exceeds max of 50
                "Biography",
                new BigDecimal("150.00")
        );

        // When & Then
        mockMvc.perform(post("/users/register/doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDoctorDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.yearsOfExperience").value("Experience must be at most 50 years"));
    }

    @Test
    void registerPatient_WithValidData_ShouldReturnCreated() throws Exception {
        // When & Then
        mockMvc.perform(post("/users/register/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPatientRegistrationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"))
                .andExpect(jsonPath("$.bloodType").value("A+"));

        // Verify database state
        List<UserModel> users = userRepository.findAll();
        assertThat(users).hasSize(1);
        assertThat(users.getFirst().getEmail()).isEqualTo("test@example.com");
        assertThat(users.getFirst().getRole()).isEqualTo(Role.PATIENT);
        assertThat(passwordEncoder.matches("password123", users.getFirst().getPassword())).isTrue();

        List<Patient> patients = patientRepository.findAll();
        assertThat(patients).hasSize(1);
        assertThat(patients.getFirst().getBloodType()).isEqualTo("A+");
        assertThat(patients.getFirst().getEmergencyContactName()).isEqualTo("Jane Smith");
        assertThat(patients.getFirst().getEmergencyContactPhone()).isEqualTo("+9876543210");
        assertThat(patients.getFirst().getAllergies()).containsExactlyInAnyOrder("Penicillin", "Peanuts");
    }

    @Test
    void registerPatient_WithMinimalData_ShouldReturnCreated() throws Exception {
        // Given - Patient with only required user fields
        PasswordDto passwordDto = new PasswordDto("password123", "password123");
        UserRegistrationDto userDto = new UserRegistrationDto(
                "patient@example.com",
                passwordDto,
                "Jane",
                "Smith",
                "+1234567890"
        );
        PatientRegistrationDto minimalPatientDto = new PatientRegistrationDto(
                userDto,
                null,  // No emergency contact
                null,  // No emergency phone
                null,  // No blood type
                null   // No allergies
        );

        // When & Then
        mockMvc.perform(post("/users/register/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minimalPatientDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));

        // Verify database state
        List<Patient> patients = patientRepository.findAll();
        assertThat(patients).hasSize(1);
        assertThat(patients.getFirst().getBloodType()).isNull();
        assertThat(patients.getFirst().getEmergencyContactName()).isNull();
    }

    @Test
    void registerPatient_WithDuplicateEmail_ShouldReturnBadRequest() throws Exception {
        // Given - Register first patient
        mockMvc.perform(post("/users/register/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPatientRegistrationDto)))
                .andExpect(status().isCreated());

        // When & Then - Try to register with same email
        mockMvc.perform(post("/users/register/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPatientRegistrationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Duplicate User"))
                .andExpect(jsonPath("$.message").value("User with email test@example.com already exists"));

        // Verify only one user exists
        assertThat(userRepository.findAll()).hasSize(1);
        assertThat(patientRepository.findAll()).hasSize(1);
    }

    @Test
    void registerPatient_WithInvalidBloodType_ShouldReturnBadRequest() throws Exception {
        // Given
        PasswordDto passwordDto = new PasswordDto("password123", "password123");
        UserRegistrationDto userDto = new UserRegistrationDto(
                "test@example.com",
                passwordDto,
                "John",
                "Doe",
                "+1234567890"
        );
        PatientRegistrationDto invalidPatientDto = new PatientRegistrationDto(
                userDto,
                "Emergency Contact",
                "+9876543210",
                "XYZ",  // Invalid blood type
                List.of()
        );

        // When & Then
        mockMvc.perform(post("/users/register/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPatientDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.bloodType").value("Blood type must be A, B, AB, O, + or -"));

        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void registerPatient_WithInvalidPhoneNumber_ShouldReturnBadRequest() throws Exception {
        // Given
        PasswordDto passwordDto = new PasswordDto("password123", "password123");
        UserRegistrationDto userDto = new UserRegistrationDto(
                "test@example.com",
                passwordDto,
                "John",
                "Doe",
                "invalid-phone"
        );
        PatientRegistrationDto invalidPatientDto = new PatientRegistrationDto(
                userDto,
                "Emergency Contact",
                "+9876543210",
                "A+",
                List.of()
        );

        // When & Then
        mockMvc.perform(post("/users/register/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPatientDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['user.phoneNumber']").value("Emergency contact phone must be a valid phone number"));
    }

    @Test
    void registerDoctor_WithPasswordMismatch_ShouldReturnBadRequest() throws Exception {
        // Given
        PasswordDto mismatchedPasswordDto = new PasswordDto("password123", "different456");
        UserRegistrationDto userDto = new UserRegistrationDto(
                "test@example.com",
                mismatchedPasswordDto,
                "John",
                "Doe",
                "+1234567890"
        );
        DoctorRegistrationDto doctorDto = new DoctorRegistrationDto(
                userDto,
                "LICENSE123",
                "Cardiology",
                10,
                "Biography",
                new BigDecimal("150.00")
        );

        // When & Then
        mockMvc.perform(post("/users/register/doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doctorDto)))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void registerDoctor_WithNegativeConsultationFee_ShouldReturnBadRequest() throws Exception {
        // Given
        PasswordDto passwordDto = new PasswordDto("password123", "password123");
        UserRegistrationDto userDto = new UserRegistrationDto(
                "test@example.com",
                passwordDto,
                "John",
                "Doe",
                "+1234567890"
        );
        DoctorRegistrationDto invalidDoctorDto = new DoctorRegistrationDto(
                userDto,
                "LICENSE123",
                "Cardiology",
                10,
                "Biography",
                new BigDecimal("-10.00")  // Negative fee
        );

        // When & Then
        mockMvc.perform(post("/users/register/doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDoctorDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.consultationFee").value("Fee must be at least 0"));
    }

    @Test
    void registerPatient_WithEmptyAllergiesList_ShouldReturnCreated() throws Exception {
        // Given
        PasswordDto passwordDto = new PasswordDto("password123", "password123");
        UserRegistrationDto userDto = new UserRegistrationDto(
                "test@example.com",
                passwordDto,
                "John",
                "Doe",
                "+1234567890"
        );
        PatientRegistrationDto patientDto = new PatientRegistrationDto(
                userDto,
                "Emergency Contact",
                "+9876543210",
                "O-",
                List.of()  // Empty allergies list
        );

        // When & Then
        mockMvc.perform(post("/users/register/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientDto)))
                .andExpect(status().isCreated());

        List<Patient> patients = patientRepository.findAll();
        assertThat(patients).hasSize(1);
        assertThat(patients.getFirst().getAllergies()).isEmpty();
    }

    @Test
    void registerDoctor_AndPatient_WithDifferentEmails_ShouldSucceed() throws Exception {
        // Given - Register doctor
        mockMvc.perform(post("/users/register/doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDoctorRegistrationDto)))
                .andExpect(status().isCreated());

        // Given - Create patient with different email
        PasswordDto passwordDto = new PasswordDto("password123", "password123");
        UserRegistrationDto patientUserDto = new UserRegistrationDto(
                "patient@example.com",
                passwordDto,
                "Jane",
                "Smith",
                "+9876543210"
        );
        PatientRegistrationDto patientDto = new PatientRegistrationDto(
                patientUserDto,
                "Emergency Contact",
                "+1111111111",
                "B+",
                List.of("Shellfish")
        );

        // When & Then - Register patient
        mockMvc.perform(post("/users/register/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientDto)))
                .andExpect(status().isCreated());

        // Verify both users exist
        assertThat(userRepository.findAll()).hasSize(2);
        assertThat(doctorRepository.findAll()).hasSize(1);
        assertThat(patientRepository.findAll()).hasSize(1);
    }
}