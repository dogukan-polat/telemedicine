package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.user.*;
import com.dogukanpolat.telemedicine.security.JwtUtils;
import com.dogukanpolat.telemedicine.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/doctor/register")
    public ResponseEntity<DoctorResponseDto> registerDoctor(@Valid @RequestBody DoctorRegistrationDto doctorRegistrationDto) {
        return new ResponseEntity<>(userService.registerDoctor(doctorRegistrationDto), HttpStatus.CREATED);
    }

    @PostMapping("/patient/register")
    public ResponseEntity<PatientResponseDto> registerPatient(@Valid @RequestBody PatientRegistrationDto patientRegistrationDto) {
        return new ResponseEntity<>(userService.registerPatient(patientRegistrationDto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody UserLoginDto userLoginDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLoginDto.email(),
                        userLoginDto.password()
                )
        );

        String token = jwtUtils.generateToken(userLoginDto.email());
        return ResponseEntity.ok(new JwtResponseDto(token));
    }
}
