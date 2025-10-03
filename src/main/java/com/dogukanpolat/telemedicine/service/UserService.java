package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.user.DoctorRegistrationDto;
import com.dogukanpolat.telemedicine.dto.user.PatientRegistrationDto;
import com.dogukanpolat.telemedicine.mappers.UserMapper;
import com.dogukanpolat.telemedicine.model.Doctor;
import com.dogukanpolat.telemedicine.model.Patient;
import com.dogukanpolat.telemedicine.model.UserModel;
import com.dogukanpolat.telemedicine.model.enums.Role;
import com.dogukanpolat.telemedicine.repository.DoctorRepository;
import com.dogukanpolat.telemedicine.repository.PatientRepository;
import com.dogukanpolat.telemedicine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public Doctor registerDoctor(DoctorRegistrationDto doctorRegistrationDto) {
        Doctor doctor = userMapper.toDoctor(doctorRegistrationDto);
        UserModel userModel = userMapper.toUser(doctorRegistrationDto.user());
        userModel.setRole(Role.DOCTOR);
        UserModel savedUser = userRepository.save(userModel);
        doctor.setUser(savedUser);
        return doctorRepository.save(doctor);
    }

    public Patient registerPatient(PatientRegistrationDto patientRegistrationDto) {
        Patient patient = userMapper.toPatient(patientRegistrationDto);
        UserModel userModel = userMapper.toUser(patientRegistrationDto.user());
        userModel.setRole(Role.PATIENT);
        UserModel savedUser = userRepository.save(userModel);
        patient.setUser(savedUser);
        return patientRepository.save(patient);
    }
}
