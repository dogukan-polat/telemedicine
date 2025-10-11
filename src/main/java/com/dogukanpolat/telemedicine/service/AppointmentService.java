package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.model.Appointment;
import com.dogukanpolat.telemedicine.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;

    public List<Appointment> getAppointmentsByPatientId(UUID id) {
        return appointmentRepository.findByPatientId(id);
    }

    public List<Appointment> getAppointmentsByDoctorId(UUID id) {
        return appointmentRepository.findByDoctorId(id);
    }

    public Appointment createAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public void deleteAppointment(UUID id) {
        appointmentRepository.deleteById(id);
    }
}
