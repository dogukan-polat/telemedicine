package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.appointment.AppointmentRequestDto;
import com.dogukanpolat.telemedicine.dto.appointment.AppointmentResponseDto;
import com.dogukanpolat.telemedicine.exception.AvailabilityException;
import com.dogukanpolat.telemedicine.mappers.AppointmentMapper;
import com.dogukanpolat.telemedicine.model.Appointment;
import com.dogukanpolat.telemedicine.model.enums.AppointmentStatus;
import com.dogukanpolat.telemedicine.model.enums.DayOfWeek;
import com.dogukanpolat.telemedicine.repository.AppointmentRepository;
import com.dogukanpolat.telemedicine.repository.DoctorRepository;
import com.dogukanpolat.telemedicine.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final EmailNotificationService emailService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityService doctorAvailabilityService;

    public List<AppointmentResponseDto> getAppointmentsByPatientId(UUID id) {
        return appointmentRepository.findByPatientId(id).stream().map(appointmentMapper::toAppointmentResponseDto).toList();
    }

    public List<AppointmentResponseDto> getAppointmentsByDoctorId(UUID id) {
        return appointmentRepository.findByDoctorId(id).stream().map(appointmentMapper::toAppointmentResponseDto).toList();
    }

    public AppointmentResponseDto createAppointment(AppointmentRequestDto appointmentRequest) {
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(appointmentRequest.scheduledDate().getDayOfWeek().name());
        LocalTime endTime = appointmentRequest.scheduledTime().plusMinutes(appointmentRequest.durationMinutes());

        boolean isAvailable = doctorAvailabilityService.isDoctorAvailable(
                appointmentRequest.doctorId(),
                dayOfWeek,
                appointmentRequest.scheduledTime(),
                endTime
        );

        if (!isAvailable) {
            throw new AvailabilityException("Doctor is not available at the requested time. Please check doctor's availability schedule.");
        }

        Appointment appointment = appointmentMapper.toAppointment(appointmentRequest);
        appointment.setId(UUID.randomUUID());
        appointment.setPatient(patientRepository.findById(appointmentRequest.patientId()).orElseThrow());
        appointment.setDoctor(doctorRepository.findById(appointmentRequest.doctorId()).orElseThrow());
        appointment.setCreatedAt(Instant.now());
        Appointment saved = appointmentRepository.save(appointment);
        emailService.sendAppointmentConfirmation(saved);
        return appointmentMapper.toAppointmentResponseDto(saved);
    }

    public void deleteAppointment(UUID id) {
        appointmentRepository.deleteById(id);
    }

    public AppointmentResponseDto changeAppointmentStatus(UUID id, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment cannot be null"));
        appointment.setStatus(status);
        Appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toAppointmentResponseDto(saved);
    }
}
