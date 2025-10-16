package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.model.Appointment;
import com.dogukanpolat.telemedicine.model.Doctor;
import com.dogukanpolat.telemedicine.model.Patient;
import com.dogukanpolat.telemedicine.model.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificationService emailService;

    private Appointment testAppointment;
    private UserModel patientUser;
    private UserModel doctorUser;

    @BeforeEach
    void setUp() {
        patientUser = new UserModel();
        patientUser.setEmail("patient@test.com");
        patientUser.setFirstName("John");
        patientUser.setLastName("Doe");

        doctorUser = new UserModel();
        doctorUser.setEmail("doctor@test.com");
        doctorUser.setFirstName("Jane");
        doctorUser.setLastName("Smith");

        Patient patient = new Patient();
        patient.setId(UUID.randomUUID());
        patient.setUser(patientUser);

        Doctor doctor = new Doctor();
        doctor.setId(UUID.randomUUID());
        doctor.setUser(doctorUser);
        doctor.setSpecialization("Cardiology");

        testAppointment = new Appointment();
        testAppointment.setId(UUID.randomUUID());
        testAppointment.setPatient(patient);
        testAppointment.setDoctor(doctor);
        testAppointment.setScheduledDate(LocalDate.of(2025, 10, 20));
        testAppointment.setScheduledTime(LocalTime.of(14, 30));
        testAppointment.setDurationMinutes(45);


    }

    @Test
    void sendAppointmentConfirmation_ShouldSendTwoEmails() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        // When
        emailService.sendAppointmentConfirmation(testAppointment);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());

        var messages = messageCaptor.getAllValues();
        assertThat(messages).hasSize(2);
    }

    @Test
    void sendAppointmentConfirmation_PatientEmail_ShouldHaveCorrectContent() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        // When
        emailService.sendAppointmentConfirmation(testAppointment);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());

        SimpleMailMessage patientEmail = messageCaptor.getAllValues().getFirst();

        assertThat(patientEmail.getTo()).containsExactly("patient@test.com");
        assertThat(patientEmail.getSubject()).isEqualTo("Appointment Confirmation");
        assertThat(patientEmail.getFrom()).isEqualTo("noreply@telemedicine.com");
        assertThat(patientEmail.getText())
                .contains("Dear John Doe")
                .contains("Dr. Jane Smith")
                .contains("Cardiology")
                .contains("October 20, 2025")
                .contains("02:30 PM")
                .contains("45 minutes");
    }

    @Test
    void sendAppointmentConfirmation_DoctorEmail_ShouldHaveCorrectContent() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        // When
        emailService.sendAppointmentConfirmation(testAppointment);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());

        SimpleMailMessage doctorEmail = messageCaptor.getAllValues().get(1);

        assertThat(doctorEmail.getTo()).containsExactly("doctor@test.com");
        assertThat(doctorEmail.getSubject()).isEqualTo("New Appointment Scheduled");
        assertThat(doctorEmail.getText())
                .contains("Dear Dr. Jane Smith")
                .contains("John Doe")
                .contains("October 20, 2025")
                .contains("45 minutes");
    }

    @Test
    void sendAppointmentCancellation_ShouldSendTwoEmails() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        // When
        emailService.sendAppointmentCancellation(testAppointment);

        // Then
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendAppointmentCancellation_ShouldHaveCorrectContent() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        // When
        emailService.sendAppointmentCancellation(testAppointment);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());

        var messages = messageCaptor.getAllValues();

        assertThat(messages.get(0).getTo()).containsExactly("patient@test.com");
        assertThat(messages.get(1).getTo()).containsExactly("doctor@test.com");

        for (SimpleMailMessage message : messages) {
            assertThat(message.getSubject()).isEqualTo("Appointment Cancelled");
            assertThat(message.getText())
                    .contains("cancelled")
                    .contains("October 20, 2025");
        }
    }

    @Test
    void sendAppointmentConfirmation_ShouldHandleException_WhenMailSenderFails() {
        // Given
        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When - should not throw exception due to try-catch
        emailService.sendAppointmentConfirmation(testAppointment);

        // Then
        verify(mailSender, atLeastOnce()).send(any(SimpleMailMessage.class));
    }
}