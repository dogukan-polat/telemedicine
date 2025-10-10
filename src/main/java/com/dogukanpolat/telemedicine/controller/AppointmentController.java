package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentRepository appointmentRepository;
}
