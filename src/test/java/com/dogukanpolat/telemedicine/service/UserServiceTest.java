package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.user.*;
import com.dogukanpolat.telemedicine.exception.DuplicateUserException;
import com.dogukanpolat.telemedicine.mappers.UserMapper;
import com.dogukanpolat.telemedicine.model.Doctor;
import com.dogukanpolat.telemedicine.model.Patient;
import com.dogukanpolat.telemedicine.model.UserModel;
import com.dogukanpolat.telemedicine.model.enums.Role;
import com.dogukanpolat.telemedicine.repository.DoctorRepository;
import com.dogukanpolat.telemedicine.repository.PatientRepository;
import com.dogukanpolat.telemedicine.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDto userRegistrationDto;
    private UserModel userModel;
    private PasswordDto passwordDto;

    @BeforeEach
    void setUp() {
        passwordDto = new PasswordDto("password123", "password123");
        userRegistrationDto = new UserRegistrationDto(
                "test@example.com",
                passwordDto,
                "John",
                "Doe",
                "+1234567890",
                LocalDate.of(1990, 1, 1)
        );

        userModel = new UserModel();
        userModel.setId(UUID.randomUUID());
        userModel.setEmail("test@example.com");
        userModel.setPassword("password123");
        userModel.setFirstName("John");
        userModel.setLastName("Doe");
        userModel.setPhoneNumber("+1234567890");
    }

    @Test
    void registerDoctor_Success() {
        // Given
        DoctorRegistrationDto doctorRegistrationDto = new DoctorRegistrationDto(
                userRegistrationDto,
                "LICENSE123",
                "Cardiology",
                10,
                "Experienced cardiologist",
                new BigDecimal("150.00")
        );

        Doctor doctor = new Doctor();
        doctor.setMedicalLicenseNumber("LICENSE123");
        doctor.setSpecialization("Cardiology");
        doctor.setYearsOfExperience(10);
        doctor.setBiography("Experienced cardiologist");
        doctor.setConsultationFee(new BigDecimal("150.00"));

        UserModel savedUser = new UserModel();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole(Role.DOCTOR);
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setPhoneNumber("+1234567890");

        Doctor savedDoctor = new Doctor();
        savedDoctor.setId(UUID.randomUUID());
        savedDoctor.setUser(savedUser);
        savedDoctor.setMedicalLicenseNumber("LICENSE123");
        savedDoctor.setSpecialization("Cardiology");
        savedDoctor.setYearsOfExperience(10);

        DoctorResponseDto expectedResponse = new DoctorResponseDto(
                "John",
                "Doe",
                "+1234567890",
                "Cardiology",
                10
        );

        when(userMapper.toDoctor(doctorRegistrationDto)).thenReturn(doctor);
        when(userMapper.toUser(userRegistrationDto)).thenReturn(userModel);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserModel.class))).thenReturn(savedUser);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(savedDoctor);
        when(userMapper.toDoctorResponse(savedDoctor)).thenReturn(expectedResponse);

        // When
        DoctorResponseDto response = userService.registerDoctor(doctorRegistrationDto);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.phoneNumber()).isEqualTo("+1234567890");
        assertThat(response.specialization()).isEqualTo("Cardiology");
        assertThat(response.yearsOfExperience()).isEqualTo(10);

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(UserModel.class));
        verify(doctorRepository).save(any(Doctor.class));
        verify(userMapper).toDoctorResponse(savedDoctor);
    }

    @Test
    void registerDoctor_ThrowsDuplicateUserException_WhenEmailExists() {
        // Given
        DoctorRegistrationDto doctorRegistrationDto = new DoctorRegistrationDto(
                userRegistrationDto,
                "LICENSE123",
                "Cardiology",
                10,
                "Experienced cardiologist",
                new BigDecimal("150.00")
        );

        Doctor doctor = new Doctor();
        when(userMapper.toDoctor(doctorRegistrationDto)).thenReturn(doctor);
        when(userMapper.toUser(userRegistrationDto)).thenReturn(userModel);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerDoctor(doctorRegistrationDto))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessage("User with email test@example.com already exists");

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(UserModel.class));
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void registerDoctor_SetsCorrectRoleAndEncodesPassword() {
        // Given
        DoctorRegistrationDto doctorRegistrationDto = new DoctorRegistrationDto(
                userRegistrationDto,
                "LICENSE123",
                "Cardiology",
                10,
                "Experienced cardiologist",
                new BigDecimal("150.00")
        );

        Doctor doctor = new Doctor();
        UserModel savedUser = new UserModel();
        savedUser.setId(UUID.randomUUID());
        Doctor savedDoctor = new Doctor();
        savedDoctor.setUser(savedUser);

        when(userMapper.toDoctor(doctorRegistrationDto)).thenReturn(doctor);
        when(userMapper.toUser(userRegistrationDto)).thenReturn(userModel);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserModel.class))).thenReturn(savedUser);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(savedDoctor);
        when(userMapper.toDoctorResponse(any())).thenReturn(
                new DoctorResponseDto("John", "Doe", "+1234567890", "Cardiology", 10)
        );

        // When
        userService.registerDoctor(doctorRegistrationDto);

        // Then
        verify(passwordEncoder).encode("password123");
        assertThat(userModel.getRole()).isEqualTo(Role.DOCTOR);
        assertThat(userModel.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    void registerPatient_Success() {
        // Given
        PatientRegistrationDto patientRegistrationDto = new PatientRegistrationDto(
                userRegistrationDto,
                "Jane Smith",
                "+9876543210",
                "A+",
                List.of("Penicillin", "Peanuts")
        );

        Patient patient = new Patient();
        patient.setEmergencyContactName("Jane Smith");
        patient.setEmergencyContactPhone("+9876543210");
        patient.setBloodType("A+");
        patient.setAllergies(List.of("Penicillin", "Peanuts"));

        UserModel savedUser = new UserModel();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole(Role.PATIENT);
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setPhoneNumber("+1234567890");

        Patient savedPatient = new Patient();
        savedPatient.setId(UUID.randomUUID());
        savedPatient.setUser(savedUser);
        savedPatient.setBloodType("A+");

        PatientResponseDto expectedResponse = new PatientResponseDto(
                "John",
                "Doe",
                "+1234567890",
                "A+"
        );

        when(userMapper.toPatient(patientRegistrationDto)).thenReturn(patient);
        when(userMapper.toUser(userRegistrationDto)).thenReturn(userModel);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserModel.class))).thenReturn(savedUser);
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);
        when(userMapper.toPatientResponse(savedPatient)).thenReturn(expectedResponse);

        // When
        PatientResponseDto response = userService.registerPatient(patientRegistrationDto);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.phoneNumber()).isEqualTo("+1234567890");
        assertThat(response.bloodType()).isEqualTo("A+");

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(UserModel.class));
        verify(patientRepository).save(any(Patient.class));
        verify(userMapper).toPatientResponse(savedPatient);
    }

    @Test
    void registerPatient_ThrowsDuplicateUserException_WhenEmailExists() {
        // Given
        PatientRegistrationDto patientRegistrationDto = new PatientRegistrationDto(
                userRegistrationDto,
                "Jane Smith",
                "+9876543210",
                "A+",
                List.of("Penicillin")
        );

        Patient patient = new Patient();
        when(userMapper.toPatient(patientRegistrationDto)).thenReturn(patient);
        when(userMapper.toUser(userRegistrationDto)).thenReturn(userModel);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerPatient(patientRegistrationDto))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessage("User with email test@example.com already exists");

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(UserModel.class));
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void registerPatient_SetsCorrectRoleAndEncodesPassword() {
        // Given
        PatientRegistrationDto patientRegistrationDto = new PatientRegistrationDto(
                userRegistrationDto,
                "Jane Smith",
                "+9876543210",
                "A+",
                List.of("Penicillin")
        );

        Patient patient = new Patient();
        UserModel savedUser = new UserModel();
        savedUser.setId(UUID.randomUUID());
        Patient savedPatient = new Patient();
        savedPatient.setUser(savedUser);

        when(userMapper.toPatient(patientRegistrationDto)).thenReturn(patient);
        when(userMapper.toUser(userRegistrationDto)).thenReturn(userModel);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserModel.class))).thenReturn(savedUser);
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);
        when(userMapper.toPatientResponse(any())).thenReturn(
                new PatientResponseDto("John", "Doe", "+1234567890", "A+")
        );

        // When
        userService.registerPatient(patientRegistrationDto);

        // Then
        verify(passwordEncoder).encode("password123");
        assertThat(userModel.getRole()).isEqualTo(Role.PATIENT);
        assertThat(userModel.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    void registerPatient_WithNullOptionalFields_Success() {
        // Given
        PatientRegistrationDto patientRegistrationDto = new PatientRegistrationDto(
                userRegistrationDto,
                null,
                null,
                null,
                null
        );

        Patient patient = new Patient();
        UserModel savedUser = new UserModel();
        savedUser.setId(UUID.randomUUID());
        Patient savedPatient = new Patient();
        savedPatient.setUser(savedUser);

        when(userMapper.toPatient(patientRegistrationDto)).thenReturn(patient);
        when(userMapper.toUser(userRegistrationDto)).thenReturn(userModel);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserModel.class))).thenReturn(savedUser);
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);
        when(userMapper.toPatientResponse(any())).thenReturn(
                new PatientResponseDto("John", "Doe", "+1234567890", null)
        );

        // When
        PatientResponseDto response = userService.registerPatient(patientRegistrationDto);

        // Then
        assertThat(response).isNotNull();
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void registerDoctor_AssociatesUserWithDoctor() {
        // Given
        DoctorRegistrationDto doctorRegistrationDto = new DoctorRegistrationDto(
                userRegistrationDto,
                "LICENSE123",
                "Cardiology",
                10,
                "Biography",
                new BigDecimal("150.00")
        );

        Doctor doctor = new Doctor();
        UserModel savedUser = new UserModel();
        savedUser.setId(UUID.randomUUID());
        Doctor savedDoctor = new Doctor();

        when(userMapper.toDoctor(doctorRegistrationDto)).thenReturn(doctor);
        when(userMapper.toUser(userRegistrationDto)).thenReturn(userModel);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserModel.class))).thenReturn(savedUser);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(savedDoctor);
        when(userMapper.toDoctorResponse(any())).thenReturn(
                new DoctorResponseDto("John", "Doe", "+1234567890", "Cardiology", 10)
        );

        // When
        userService.registerDoctor(doctorRegistrationDto);

        // Then
        assertThat(doctor.getUser()).isEqualTo(savedUser);
        verify(doctorRepository).save(doctor);
    }

    @Test
    void registerPatient_AssociatesUserWithPatient() {
        // Given
        PatientRegistrationDto patientRegistrationDto = new PatientRegistrationDto(
                userRegistrationDto,
                "Emergency Contact",
                "+9876543210",
                "O+",
                List.of()
        );

        Patient patient = new Patient();
        UserModel savedUser = new UserModel();
        savedUser.setId(UUID.randomUUID());
        Patient savedPatient = new Patient();

        when(userMapper.toPatient(patientRegistrationDto)).thenReturn(patient);
        when(userMapper.toUser(userRegistrationDto)).thenReturn(userModel);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserModel.class))).thenReturn(savedUser);
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);
        when(userMapper.toPatientResponse(any())).thenReturn(
                new PatientResponseDto("John", "Doe", "+1234567890", "O+")
        );

        // When
        userService.registerPatient(patientRegistrationDto);

        // Then
        assertThat(patient.getUser()).isEqualTo(savedUser);
        verify(patientRepository).save(patient);
    }
}