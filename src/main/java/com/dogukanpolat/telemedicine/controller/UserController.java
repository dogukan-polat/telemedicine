package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.user.*;
import com.dogukanpolat.telemedicine.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register/doctor")
    public ResponseEntity<DoctorResponseDto> registerDoctor(@Valid @RequestBody DoctorRegistrationDto doctorRegistrationDto) {
        return new ResponseEntity<>(userService.registerDoctor(doctorRegistrationDto), HttpStatus.CREATED);
    }

    @PostMapping("/register/patient")
    public ResponseEntity<PatientResponseDto> registerPatient(@Valid @RequestBody PatientRegistrationDto patientRegistrationDto) {
        return new ResponseEntity<>(userService.registerPatient(patientRegistrationDto), HttpStatus.CREATED);
    }

    @PostMapping("/register/admin")
    public ResponseEntity<Void> registerAdmin(@Valid @RequestBody UserRegistrationDto userRegistrationDto) {
        userService.registerAdmin(userRegistrationDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
