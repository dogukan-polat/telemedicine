package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.user.DoctorRegistrationDto;
import com.dogukanpolat.telemedicine.dto.user.PatientRegistrationDto;
import com.dogukanpolat.telemedicine.model.Doctor;
import com.dogukanpolat.telemedicine.model.Patient;
import com.dogukanpolat.telemedicine.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/doctor/register")
    public Doctor registerDoctor(@Valid @RequestBody DoctorRegistrationDto doctorRegistrationDto) {
        return userService.registerDoctor(doctorRegistrationDto);
    }

    @PostMapping("/patient/register")
    public Patient registerPatient(@Valid @RequestBody PatientRegistrationDto patientRegistrationDto) {
        return userService.registerPatient(patientRegistrationDto);
    }
}
