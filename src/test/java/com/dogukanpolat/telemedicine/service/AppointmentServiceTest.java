package com.dogukanpolat.telemedicine.service;


import com.dogukanpolat.telemedicine.dto.appointment.AppointmentRequestDto;
import com.dogukanpolat.telemedicine.mappers.AppointmentMapper;
import com.dogukanpolat.telemedicine.model.Appointment;
import com.dogukanpolat.telemedicine.model.Doctor;
import com.dogukanpolat.telemedicine.model.Patient;
import com.dogukanpolat.telemedicine.model.enums.AppointmentStatus;
import com.dogukanpolat.telemedicine.repository.AppointmentRepository;
import com.dogukanpolat.telemedicine.repository.DoctorRepository;
import com.dogukanpolat.telemedicine.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private EmailNotificationService emailService;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private UUID patientId;
    private UUID doctorId;
    private UUID appointmentId;
    private Appointment testAppointment;
    private AppointmentRequestDto requestDto;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();

        Patient patient = new Patient();
        patient.setId(patientId);

        Doctor doctor = new Doctor();
        doctor.setId(doctorId);

        testAppointment = new Appointment();
        testAppointment.setId(appointmentId);
        testAppointment.setPatient(patient);
        testAppointment.setDoctor(doctor);
        testAppointment.setScheduledDate(LocalDate.now().plusDays(1));
        testAppointment.setScheduledTime(LocalTime.of(10, 0));
        testAppointment.setDurationMinutes(30);
        testAppointment.setStatus(AppointmentStatus.SCHEDULED);

        requestDto = new AppointmentRequestDto(
                patientId,
                doctorId,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                30
        );
    }

    @Test
    void getAppointmentsByPatientId_ShouldReturnAppointmentList() {
        // Given
        List<Appointment> expected = List.of(testAppointment);
        when(appointmentRepository.findByPatientId(patientId)).thenReturn(expected);

        // When
        List<Appointment> result = appointmentService.getAppointmentsByPatientId(patientId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getPatient().getId()).isEqualTo(patientId);
        verify(appointmentRepository).findByPatientId(patientId);
    }

    @Test
    void getAppointmentsByDoctorId_ShouldReturnAppointmentList() {
        // Given
        List<Appointment> expected = List.of(testAppointment);
        when(appointmentRepository.findByDoctorId(doctorId)).thenReturn(expected);

        // When
        List<Appointment> result = appointmentService.getAppointmentsByDoctorId(doctorId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDoctor().getId()).isEqualTo(doctorId);
        verify(appointmentRepository).findByDoctorId(doctorId);
    }

    @Test
    void createAppointment_ShouldSaveAndSendEmail() {
        // Given
        when(appointmentMapper.toAppointment(requestDto)).thenReturn(testAppointment);
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(new Patient()));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(new Doctor()));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);
        doNothing().when(emailService).sendAppointmentConfirmation(any(Appointment.class));

        // When
        Appointment result = appointmentService.createAppointment(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();

        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(appointmentCaptor.capture());
        verify(emailService).sendAppointmentConfirmation(testAppointment);

        Appointment savedAppointment = appointmentCaptor.getValue();
        assertThat(savedAppointment.getCreatedAt()).isNotNull();
    }

    @Test
    void deleteAppointment_ShouldCallRepository() {
        // Given
        doNothing().when(appointmentRepository).deleteById(appointmentId);

        // When
        appointmentService.deleteAppointment(appointmentId);

        // Then
        verify(appointmentRepository).deleteById(appointmentId);
    }

    @Test
    void changeAppointmentStatus_ShouldUpdateStatus() {
        // Given
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // When
        Appointment result = appointmentService.changeAppointmentStatus(appointmentId, AppointmentStatus.CONFIRMED);

        // Then
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        verify(appointmentRepository).findById(appointmentId);
        verify(appointmentRepository).save(testAppointment);
    }

    @Test
    void changeAppointmentStatus_ShouldThrowException_WhenAppointmentNotFound() {
        // Given
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                appointmentService.changeAppointmentStatus(appointmentId, AppointmentStatus.CONFIRMED)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Appointment cannot be null");
    }

    @Test
    void changeAppointmentStatus_AllStatuses_ShouldWork() {
        // Given
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

        // Test all status transitions
        Appointment confirmed = appointmentService.changeAppointmentStatus(appointmentId, AppointmentStatus.CONFIRMED);
        assertThat(confirmed.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);

        Appointment cancelled = appointmentService.changeAppointmentStatus(appointmentId, AppointmentStatus.CANCELLED);
        assertThat(cancelled.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);

        Appointment completed = appointmentService.changeAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED);
        assertThat(completed.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
    }


}