package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.appointment.AppointmentRequestDto;
import com.dogukanpolat.telemedicine.model.Appointment;
import com.dogukanpolat.telemedicine.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;

    @GetMapping("/patient/{id}")
    public ResponseEntity<List<Appointment>> getAppointmentsByPatientId(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatientId(id));
    }

    @GetMapping("/doctor/{id}")
    public ResponseEntity<List<Appointment>> getAppointmentsByDoctorId(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctorId(id));
    }

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody AppointmentRequestDto appointmentRequest) {
        return ResponseEntity.ok(appointmentService.createAppointment(appointmentRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable UUID id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
