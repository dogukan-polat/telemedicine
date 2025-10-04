package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.user.DoctorRegistrationDto;
import com.dogukanpolat.telemedicine.dto.user.DoctorResponseDto;
import com.dogukanpolat.telemedicine.dto.user.PatientRegistrationDto;
import com.dogukanpolat.telemedicine.dto.user.PatientResponseDto;
import com.dogukanpolat.telemedicine.exception.DuplicateUserException;
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

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    public DoctorResponseDto registerDoctor(DoctorRegistrationDto doctorRegistrationDto) {
        Doctor doctor = userMapper.toDoctor(doctorRegistrationDto);
        UserModel userModel = userMapper.toUser(doctorRegistrationDto.user());
        if (userRepository.existsByEmail(userModel.getEmail())) {
            throw new DuplicateUserException("User with email " + userModel.getEmail() + " already exists");
        }
        userModel.setRole(Role.DOCTOR);
        userModel.setPassword(passwordEncoder.encode(userModel.getPassword()));
        UserModel savedUser = userRepository.save(userModel);
        doctor.setUser(savedUser);
        Doctor savedDoctor = doctorRepository.save(doctor);
        return userMapper.toDoctorResponse(savedDoctor);
    }

    public PatientResponseDto registerPatient(PatientRegistrationDto patientRegistrationDto) {
        Patient patient = userMapper.toPatient(patientRegistrationDto);
        UserModel userModel = userMapper.toUser(patientRegistrationDto.user());
        if (userRepository.existsByEmail(userModel.getEmail())) {
            throw new DuplicateUserException("User with email " + userModel.getEmail() + " already exists");
        }
        userModel.setRole(Role.PATIENT);
        userModel.setPassword(passwordEncoder.encode(userModel.getPassword()));
        UserModel savedUser = userRepository.save(userModel);
        patient.setUser(savedUser);
        Patient savedPatieht = patientRepository.save(patient);
        return userMapper.toPatientResponse(savedPatieht);
    }
}
