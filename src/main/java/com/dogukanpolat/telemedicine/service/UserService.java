package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.user.*;
import com.dogukanpolat.telemedicine.exception.DuplicateUserException;
import com.dogukanpolat.telemedicine.exception.PasswordMismatchException;
import com.dogukanpolat.telemedicine.mappers.UserMapper;
import com.dogukanpolat.telemedicine.model.Doctor;
import com.dogukanpolat.telemedicine.model.Patient;
import com.dogukanpolat.telemedicine.model.UserModel;
import com.dogukanpolat.telemedicine.model.enums.Role;
import com.dogukanpolat.telemedicine.repository.DoctorRepository;
import com.dogukanpolat.telemedicine.repository.PatientRepository;
import com.dogukanpolat.telemedicine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String adminEmail;



    public DoctorResponseDto registerDoctor(DoctorRegistrationDto doctorRegistrationDto) {
        Doctor doctor = userMapper.toDoctor(doctorRegistrationDto);
        UserModel savedUser = registerUser(doctorRegistrationDto.user(), Role.DOCTOR);
        doctor.setUser(savedUser);
        Doctor savedDoctor = doctorRepository.save(doctor);
        return userMapper.toDoctorResponse(savedDoctor);
    }

    public PatientResponseDto registerPatient(PatientRegistrationDto patientRegistrationDto) {
        Patient patient = userMapper.toPatient(patientRegistrationDto);
        UserModel savedUser = registerUser(patientRegistrationDto.user(), Role.PATIENT);
        patient.setUser(savedUser);
        Patient savedPatieht = patientRepository.save(patient);
        return userMapper.toPatientResponse(savedPatieht);
    }

    public void registerAdmin(UserRegistrationDto userRegistrationDto) {
        if (!userRegistrationDto.email().equals(adminEmail)) {
            throw new BadCredentialsException("Only admin can register");
        }
        registerUser(userRegistrationDto, Role.ADMIN);
    }

    //Helper methods

    private UserModel registerUser(UserRegistrationDto userRegistrationDto, Role role) {
        UserModel userModel = userMapper.toUser(userRegistrationDto);
        validateUser(userRegistrationDto);
        userModel.setRole(role);
        userModel.setPassword(passwordEncoder.encode(userModel.getPassword()));
        userModel.setIsActive(true);
        return userRepository.save(userModel);
    }

    private void validateUser(UserRegistrationDto userRegistrationDto) {
        if (userRepository.existsByEmail(userRegistrationDto.email())) {
            throw new DuplicateUserException("User with email " + userRegistrationDto.email() + " already exists");
        }
        Optional<String> password = Optional.ofNullable(userRegistrationDto.password().password());
        Optional<String> confirmPassword = Optional.ofNullable(userRegistrationDto.password().confirmPassword());
        if (password.isEmpty() || confirmPassword.isEmpty()) {
            throw new RuntimeException("Password cannot be null");
        }
        if (!password.get().equals(confirmPassword.get())) {
            throw new PasswordMismatchException("Passwords do not match");
        }
    }


}
