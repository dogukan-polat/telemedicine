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



    public DoctorResponseDto registerDoctor(DoctorRegistrationDto doctorRegistrationDto) {
        Doctor doctor = userMapper.toDoctor(doctorRegistrationDto);
        UserModel userModel = userMapper.toUser(doctorRegistrationDto.user());
        validateUser(doctorRegistrationDto.user());
        userModel.setRole(Role.DOCTOR);
        userModel.setPassword(passwordEncoder.encode(userModel.getPassword()));
        userModel.setIsActive(true);
        UserModel savedUser = userRepository.save(userModel);
        doctor.setUser(savedUser);
        Doctor savedDoctor = doctorRepository.save(doctor);
        return userMapper.toDoctorResponse(savedDoctor);
    }

    public PatientResponseDto registerPatient(PatientRegistrationDto patientRegistrationDto) {
        Patient patient = userMapper.toPatient(patientRegistrationDto);
        validateUser(patientRegistrationDto.user());
        UserModel userModel = userMapper.toUser(patientRegistrationDto.user());
        userModel.setRole(Role.PATIENT);
        userModel.setPassword(passwordEncoder.encode(userModel.getPassword()));
        userModel.setIsActive(true);
        UserModel savedUser = userRepository.save(userModel);
        patient.setUser(savedUser);
        Patient savedPatieht = patientRepository.save(patient);
        return userMapper.toPatientResponse(savedPatieht);
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
