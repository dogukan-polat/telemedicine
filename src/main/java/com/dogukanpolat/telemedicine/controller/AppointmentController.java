package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.model.Appointment;
import com.dogukanpolat.telemedicine.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentRepository appointmentRepository;

    @GetMapping("/patient/{id}")
    public ResponseEntity<List<Appointment>> getAppointmentsByPatientId(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentRepository.findByPatientId(id));
    }

    @GetMapping("/doctor/{id}")
    public ResponseEntity<List<Appointment>> getAppointmentsByDoctorId(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentRepository.findByDoctorId(id));
    }

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {
        return ResponseEntity.ok(appointmentRepository.save(appointment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable UUID id) {
        appointmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
