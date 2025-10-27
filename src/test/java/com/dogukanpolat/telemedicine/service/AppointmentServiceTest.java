package com.dogukanpolat.telemedicine.service;


import com.dogukanpolat.telemedicine.dto.appointment.AppointmentRequestDto;
import com.dogukanpolat.telemedicine.dto.appointment.AppointmentResponseDto;
import com.dogukanpolat.telemedicine.mappers.AppointmentMapper;
import com.dogukanpolat.telemedicine.model.Appointment;
import com.dogukanpolat.telemedicine.model.Doctor;
import com.dogukanpolat.telemedicine.model.Patient;
import com.dogukanpolat.telemedicine.model.UserModel;
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
    private AppointmentResponseDto responseDto;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();

        UserModel patientUser = new UserModel();
        patientUser.setFirstName("John");
        patientUser.setLastName("Doe");

        UserModel doctorUser = new UserModel();
        doctorUser.setFirstName("Jane");
        doctorUser.setLastName("Smith");

        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setUser(patientUser);

        Doctor doctor = new Doctor();
        doctor.setId(doctorId);
        doctor.setUser(doctorUser);

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

        responseDto = new AppointmentResponseDto(
                "John",
                "Doe",
                "Jane",
                "Smith",
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                30,
                AppointmentStatus.SCHEDULED
        );
    }

    @Test
    void getAppointmentsByPatientId_ShouldReturnAppointmentList() {
        // Given
        List<Appointment> appointments = List.of(testAppointment);
        when(appointmentRepository.findByPatientId(patientId)).thenReturn(appointments);
        when(appointmentMapper.toAppointmentResponseDto(testAppointment)).thenReturn(responseDto);

        // When
        List<AppointmentResponseDto> result = appointmentService.getAppointmentsByPatientId(patientId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().patientFirstName()).isEqualTo("John");
        assertThat(result.getFirst().patientLastName()).isEqualTo("Doe");
        assertThat(result.getFirst().doctorFirstName()).isEqualTo("Jane");
        assertThat(result.getFirst().doctorLastName()).isEqualTo("Smith");
        verify(appointmentRepository).findByPatientId(patientId);
        verify(appointmentMapper).toAppointmentResponseDto(testAppointment);
    }

    @Test
    void getAppointmentsByDoctorId_ShouldReturnAppointmentList() {
        // Given
        List<Appointment> appointments = List.of(testAppointment);
        when(appointmentRepository.findByDoctorId(doctorId)).thenReturn(appointments);
        when(appointmentMapper.toAppointmentResponseDto(testAppointment)).thenReturn(responseDto);

        // When
        List<AppointmentResponseDto> result = appointmentService.getAppointmentsByDoctorId(doctorId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().doctorFirstName()).isEqualTo("Jane");
        assertThat(result.getFirst().doctorLastName()).isEqualTo("Smith");
        verify(appointmentRepository).findByDoctorId(doctorId);
        verify(appointmentMapper).toAppointmentResponseDto(testAppointment);
    }

    @Test
    void createAppointment_ShouldSaveAndSendEmail() {
        // Given
        when(appointmentMapper.toAppointment(requestDto)).thenReturn(testAppointment);
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(new Patient()));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(new Doctor()));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);
        when(appointmentMapper.toAppointmentResponseDto(testAppointment)).thenReturn(responseDto);
        doNothing().when(emailService).sendAppointmentConfirmation(any(Appointment.class));

        // When
        AppointmentResponseDto result = appointmentService.createAppointment(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.doctorFirstName()).isNotNull();
        assertThat(result.doctorLastName()).isNotNull();

        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(appointmentCaptor.capture());
        verify(emailService).sendAppointmentConfirmation(testAppointment);
        verify(appointmentMapper).toAppointmentResponseDto(testAppointment);

        Appointment savedAppointment = appointmentCaptor.getValue();
        assertThat(savedAppointment.getCreatedAt()).isNotNull();
        assertThat(savedAppointment.getId()).isNotNull();
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
        when(appointmentMapper.toAppointmentResponseDto(testAppointment)).thenAnswer(i -> {
            Appointment appointment = i.getArgument(0);
            return new AppointmentResponseDto(
                    appointment.getPatient().getUser().getFirstName(),
                    appointment.getPatient().getUser().getLastName(),
                    appointment.getDoctor().getUser().getFirstName(),
                    appointment.getDoctor().getUser().getLastName(),
                    appointment.getScheduledDate(),
                    appointment.getScheduledTime(),
                    appointment.getDurationMinutes(),
                    appointment.getStatus());
        });

        // When
        AppointmentResponseDto result = appointmentService.changeAppointmentStatus(appointmentId, AppointmentStatus.CONFIRMED);

        // Then
        assertThat(result.status()).isEqualTo(AppointmentStatus.CONFIRMED);
        verify(appointmentRepository).findById(appointmentId);
        verify(appointmentRepository).save(testAppointment);
        verify(appointmentMapper).toAppointmentResponseDto(testAppointment);
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
        when(appointmentMapper.toAppointmentResponseDto(testAppointment)).thenAnswer(i -> {
            Appointment appointment = i.getArgument(0);
            return new AppointmentResponseDto(
                    appointment.getPatient().getUser().getFirstName(),
                    appointment.getPatient().getUser().getLastName(),
                    appointment.getDoctor().getUser().getFirstName(),
                    appointment.getDoctor().getUser().getLastName(),
                    appointment.getScheduledDate(),
                    appointment.getScheduledTime(),
                    appointment.getDurationMinutes(),
                    appointment.getStatus());
        });

        // Test all status transitions
        AppointmentResponseDto confirmed = appointmentService.changeAppointmentStatus(appointmentId, AppointmentStatus.CONFIRMED);
        assertThat(confirmed.status()).isEqualTo(AppointmentStatus.CONFIRMED);

        AppointmentResponseDto cancelled = appointmentService.changeAppointmentStatus(appointmentId, AppointmentStatus.CANCELLED);
        assertThat(cancelled.status()).isEqualTo(AppointmentStatus.CANCELLED);

        AppointmentResponseDto completed = appointmentService.changeAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED);
        assertThat(completed.status()).isEqualTo(AppointmentStatus.COMPLETED);
    }


}