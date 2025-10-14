package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.appointment.AppointmentRequestDto;
import com.dogukanpolat.telemedicine.mappers.AppointmentMapper;
import com.dogukanpolat.telemedicine.model.Appointment;
import com.dogukanpolat.telemedicine.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final EmailNotificationService emailService;

    public List<Appointment> getAppointmentsByPatientId(UUID id) {
        return appointmentRepository.findByPatientId(id);
    }

    public List<Appointment> getAppointmentsByDoctorId(UUID id) {
        return appointmentRepository.findByDoctorId(id);
    }

    public Appointment createAppointment(AppointmentRequestDto appointmentRequest) {
        Appointment appointment = appointmentMapper.toAppointment(appointmentRequest);
        appointment.setId(UUID.randomUUID());
        appointment.setCreatedAt(Instant.now());
        Appointment saved = appointmentRepository.save(appointment);
        emailService.sendAppointmentConfirmation(saved);
        return saved;
    }

    public void deleteAppointment(UUID id) {
        appointmentRepository.deleteById(id);
    }

    public Appointment cancelAppointment(UUID id) {
        return null;
    }
}
