package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.notification.NotificationLogResponseDto;
import com.dogukanpolat.telemedicine.dto.notification.PushNotificationDto;
import com.dogukanpolat.telemedicine.mappers.NotificationMapper;
import com.dogukanpolat.telemedicine.model.*;
import com.dogukanpolat.telemedicine.model.enums.NotificationChannel;
import com.dogukanpolat.telemedicine.model.enums.NotificationStatus;
import com.dogukanpolat.telemedicine.model.enums.NotificationType;
import com.dogukanpolat.telemedicine.repository.NotificationLogRepository;
import com.dogukanpolat.telemedicine.repository.NotificationPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @Mock
    private NotificationLogRepository logRepository;

    @Mock
    private EmailNotificationService emailService;

    @Mock
    private PushNotificationService pushService;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

    private UserModel testUser;
    private UserModel doctorUser;
    private Appointment testAppointment;
    private NotificationPreference emailPreference;
    private NotificationPreference pushPreference;
    private NotificationLog testLog;
    private NotificationLogResponseDto logResponseDto;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new UserModel();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("patient@test.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setDeviceToken("test-device-token");

        // Setup doctor user
        doctorUser = new UserModel();
        doctorUser.setId(UUID.randomUUID());
        doctorUser.setEmail("doctor@test.com");
        doctorUser.setFirstName("Jane");
        doctorUser.setLastName("Smith");

        // Setup doctor
        Doctor doctor = new Doctor();
        doctor.setId(UUID.randomUUID());
        doctor.setUser(doctorUser);
        doctor.setSpecialization("Cardiology");

        // Setup patient
        Patient patient = new Patient();
        patient.setId(UUID.randomUUID());
        patient.setUser(testUser);

        // Setup appointment
        testAppointment = new Appointment();
        testAppointment.setId(UUID.randomUUID());
        testAppointment.setPatient(patient);
        testAppointment.setDoctor(doctor);
        testAppointment.setScheduledDate(LocalDate.now().plusDays(1));
        testAppointment.setScheduledTime(LocalTime.of(10, 0));
        testAppointment.setDurationMinutes(30);

        // Setup email preference
        emailPreference = new NotificationPreference();
        emailPreference.setId(UUID.randomUUID());
        emailPreference.setUser(testUser);
        emailPreference.setNotificationType(NotificationType.APPOINTMENT_CONFIRMED);
        emailPreference.setChannel(NotificationChannel.EMAIL);
        emailPreference.setIsEnabled(true);

        // Setup push preference
        pushPreference = new NotificationPreference();
        pushPreference.setId(UUID.randomUUID());
        pushPreference.setUser(testUser);
        pushPreference.setNotificationType(NotificationType.APPOINTMENT_CONFIRMED);
        pushPreference.setChannel(NotificationChannel.PUSH);
        pushPreference.setIsEnabled(true);

        // Setup notification log
        testLog = new NotificationLog();
        testLog.setId(UUID.randomUUID());
        testLog.setUser(testUser);
        testLog.setNotificationType(NotificationType.APPOINTMENT_CONFIRMED);
        testLog.setChannel(NotificationChannel.EMAIL);
        testLog.setStatus(NotificationStatus.SENT);
        testLog.setRecipient("patient@test.com");
        testLog.setSubject("Test Subject");
        testLog.setContent("Test Content");
        testLog.setCreatedAt(OffsetDateTime.now());

        // Setup log response DTO
        logResponseDto = new NotificationLogResponseDto(
                testLog.getId(),
                testLog.getNotificationType(),
                testLog.getChannel(),
                testLog.getStatus(),
                testLog.getRecipient(),
                testLog.getSubject(),
                testLog.getErrorMessage(),
                testLog.getSentAt(),
                testLog.getCreatedAt(),
                null
        );
    }

    @Test
    void sendNotification_WithEmailPreference_ShouldSendEmail() {
        // Given
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.APPOINTMENT_CONFIRMED
        )).thenReturn(List.of(emailPreference));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendNotification(
                testUser,
                NotificationType.APPOINTMENT_CONFIRMED,
                "Test Subject",
                "Test Content",
                testAppointment
        );

        // Then
        verify(preferenceRepository).findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.APPOINTMENT_CONFIRMED
        );
        verify(emailService).sendEmail("patient@test.com", "Test Subject", "Test Content");
        verify(logRepository).save(any(NotificationLog.class));
    }

    @Test
    void sendNotification_WithPushPreference_ShouldSendPushNotification() {
        // Given
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.APPOINTMENT_CONFIRMED
        )).thenReturn(List.of(pushPreference));
        when(pushService.sendPushNotification(any(PushNotificationDto.class))).thenReturn(true);
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendNotification(
                testUser,
                NotificationType.APPOINTMENT_CONFIRMED,
                "Test Subject",
                "Test Content",
                testAppointment
        );

        // Then
        ArgumentCaptor<PushNotificationDto> pushCaptor = ArgumentCaptor.forClass(PushNotificationDto.class);
        verify(pushService).sendPushNotification(pushCaptor.capture());

        PushNotificationDto capturedPush = pushCaptor.getValue();
        assertThat(capturedPush.deviceToken()).isEqualTo("test-device-token");
        assertThat(capturedPush.userId()).isEqualTo(testUser.getId());
        assertThat(capturedPush.title()).isEqualTo("Test Subject");
        assertThat(capturedPush.body()).isEqualTo("Test Content");
        verify(logRepository).save(any(NotificationLog.class));
    }

    @Test
    void sendNotification_WithBothChannels_ShouldSendThroughBoth() {
        // Given
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.APPOINTMENT_CONFIRMED
        )).thenReturn(List.of(emailPreference, pushPreference));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        when(pushService.sendPushNotification(any(PushNotificationDto.class))).thenReturn(true);
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendNotification(
                testUser,
                NotificationType.APPOINTMENT_CONFIRMED,
                "Test Subject",
                "Test Content",
                testAppointment
        );

        // Then
        verify(emailService).sendEmail(anyString(), anyString(), anyString());
        verify(pushService).sendPushNotification(any(PushNotificationDto.class));
        verify(logRepository, times(2)).save(any(NotificationLog.class));
    }

    @Test
    void sendNotification_WithNoPreferences_ShouldNotSendAnything() {
        // Given
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.APPOINTMENT_CONFIRMED
        )).thenReturn(List.of());

        // When
        notificationService.sendNotification(
                testUser,
                NotificationType.APPOINTMENT_CONFIRMED,
                "Test Subject",
                "Test Content",
                testAppointment
        );

        // Then
        verify(preferenceRepository).findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.APPOINTMENT_CONFIRMED
        );
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
        verify(pushService, never()).sendPushNotification(any());
        verify(logRepository, never()).save(any());
    }

    @Test
    void sendNotification_WhenEmailFails_ShouldLogFailure() {
        // Given
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.APPOINTMENT_CONFIRMED
        )).thenReturn(List.of(emailPreference));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(false);

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        when(logRepository.save(logCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendNotification(
                testUser,
                NotificationType.APPOINTMENT_CONFIRMED,
                "Test Subject",
                "Test Content",
                testAppointment
        );

        // Then
        verify(emailService).sendEmail(anyString(), anyString(), anyString());
        NotificationLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(savedLog.getErrorMessage()).isEqualTo("Failed to send notification");
    }

    @Test
    void sendNotification_WhenPushFails_ShouldLogFailure() {
        // Given
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.APPOINTMENT_CONFIRMED
        )).thenReturn(List.of(pushPreference));
        when(pushService.sendPushNotification(any(PushNotificationDto.class))).thenReturn(false);

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        when(logRepository.save(logCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendNotification(
                testUser,
                NotificationType.APPOINTMENT_CONFIRMED,
                "Test Subject",
                "Test Content",
                testAppointment
        );

        // Then
        verify(pushService).sendPushNotification(any());
        NotificationLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(savedLog.getErrorMessage()).isEqualTo("Failed to send notification");
    }

    @Test
    void sendNotification_WhenExceptionThrown_ShouldLogFailureWithErrorMessage() {
        // Given
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.APPOINTMENT_CONFIRMED
        )).thenReturn(List.of(emailPreference));
        when(emailService.sendEmail(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("SMTP server error"));

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        when(logRepository.save(logCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendNotification(
                testUser,
                NotificationType.APPOINTMENT_CONFIRMED,
                "Test Subject",
                "Test Content",
                testAppointment
        );

        // Then
        NotificationLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(savedLog.getErrorMessage()).isEqualTo("SMTP server error");
    }

    @Test
    void sendNotification_WithNoDeviceToken_ShouldNotSendPush() {
        // Given
        testUser.setDeviceToken(null);
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.APPOINTMENT_CONFIRMED
        )).thenReturn(List.of(pushPreference));

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        when(logRepository.save(logCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendNotification(
                testUser,
                NotificationType.APPOINTMENT_CONFIRMED,
                "Test Subject",
                "Test Content",
                testAppointment
        );

        // Then
        verify(pushService, never()).sendPushNotification(any());
        NotificationLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getStatus()).isEqualTo(NotificationStatus.FAILED);
    }

    @Test
    void sendNotification_WithEmptyDeviceToken_ShouldNotSendPush() {
        // Given
        testUser.setDeviceToken("");
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.APPOINTMENT_CONFIRMED
        )).thenReturn(List.of(pushPreference));

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        when(logRepository.save(logCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendNotification(
                testUser,
                NotificationType.APPOINTMENT_CONFIRMED,
                "Test Subject",
                "Test Content",
                testAppointment
        );

        // Then
        verify(pushService, never()).sendPushNotification(any());
        NotificationLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getStatus()).isEqualTo(NotificationStatus.FAILED);
    }

    @Test
    void sendAppointmentReminder24Hours_ShouldSendToPatientAndDoctor() {
        // Given
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(any(), any()))
                .thenReturn(List.of(emailPreference));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendAppointmentReminder24Hours(testAppointment);

        // Then
        verify(preferenceRepository, times(2)).findEnabledPreferencesByUserIdAndType(
                any(),
                eq(NotificationType.APPOINTMENT_REMINDER_24H)
        );
        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendAppointmentReminder1Hour_ShouldSendToPatientAndDoctor() {
        // Given
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(any(), any()))
                .thenReturn(List.of(emailPreference));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendAppointmentReminder1Hour(testAppointment);

        // Then
        verify(preferenceRepository, times(2)).findEnabledPreferencesByUserIdAndType(
                any(),
                eq(NotificationType.APPOINTMENT_REMINDER_1H)
        );
        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendLabResultsNotification_ShouldSendNotification() {
        // Given
        String labTestName = "Blood Test";
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.LAB_RESULTS_READY
        )).thenReturn(List.of(emailPreference));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendLabResultsNotification(testUser, labTestName);

        // Then
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(
                eq("patient@test.com"),
                subjectCaptor.capture(),
                contentCaptor.capture()
        );

        assertThat(subjectCaptor.getValue()).contains("Lab Results Ready");
        assertThat(subjectCaptor.getValue()).contains(labTestName);
        assertThat(contentCaptor.getValue()).contains(labTestName);
        assertThat(contentCaptor.getValue()).contains("John");
        assertThat(contentCaptor.getValue()).contains("Doe");
    }

    @Test
    void sendPrescriptionRefillReminder_ShouldSendNotification() {
        // Given
        String medicationName = "Aspirin";
        int daysRemaining = 5;
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.PRESCRIPTION_REFILL_REMINDER
        )).thenReturn(List.of(emailPreference));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendPrescriptionRefillReminder(testUser, medicationName, daysRemaining);

        // Then
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(
                eq("patient@test.com"),
                anyString(),
                contentCaptor.capture()
        );

        assertThat(contentCaptor.getValue()).contains(medicationName);
        assertThat(contentCaptor.getValue()).contains("5 days");
    }

    @Test
    void sendPrescriptionReadyNotification_ShouldSendNotification() {
        // Given
        String pharmacyName = "Main Street Pharmacy";
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.PRESCRIPTION_READY
        )).thenReturn(List.of(emailPreference));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendPrescriptionReadyNotification(testUser, pharmacyName);

        // Then
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(
                eq("patient@test.com"),
                anyString(),
                contentCaptor.capture()
        );

        assertThat(contentCaptor.getValue()).contains(pharmacyName);
        assertThat(contentCaptor.getValue()).contains("ready for pickup");
    }

    @Test
    void getUserNotificationHistory_ShouldReturnUserNotifications() {
        // Given
        List<NotificationLog> logs = List.of(testLog);
        when(logRepository.findByUserId(testUser.getId())).thenReturn(logs);
        when(notificationMapper.toLogDto(testLog)).thenReturn(logResponseDto);

        // When
        List<NotificationLogResponseDto> result = notificationService.getUserNotificationHistory(testUser.getId());

        // Then
        assertThat(result).hasSize(1);
        verify(logRepository).findByUserId(testUser.getId());
        verify(notificationMapper).toLogDto(any());
    }

    @Test
    void sendNotification_WithNullAppointment_ShouldStillWork() {
        // Given
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.LAB_RESULTS_READY
        )).thenReturn(List.of(emailPreference));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        when(logRepository.save(logCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendNotification(
                testUser,
                NotificationType.LAB_RESULTS_READY,
                "Test Subject",
                "Test Content",
                null
        );

        // Then
        NotificationLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getAppointment()).isNull();
        assertThat(savedLog.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void sendAppointmentReminder24Hours_ShouldIncludeAppointmentDetails() {
        // Given
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(any(), any()))
                .thenReturn(List.of(emailPreference));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendAppointmentReminder24Hours(testAppointment);

        // Then
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService, times(2)).sendEmail(
                anyString(),
                anyString(),
                contentCaptor.capture()
        );

        List<String> contents = contentCaptor.getAllValues();
        assertThat(contents).allMatch(content ->
                content.contains("Jane") &&
                        content.contains("Smith") &&
                        content.contains(testAppointment.getScheduledDate().toString()) &&
                        content.contains("30 minutes")
        );
    }

    @Test
    void sendAppointmentReminder1Hour_ShouldIncludeAppointmentDetails() {
        // Given
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(any(), any()))
                .thenReturn(List.of(emailPreference));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendAppointmentReminder1Hour(testAppointment);

        // Then
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService, times(2)).sendEmail(
                anyString(),
                anyString(),
                contentCaptor.capture()
        );

        List<String> contents = contentCaptor.getAllValues();
        assertThat(contents).allMatch(content ->
                content.contains("Jane") &&
                        content.contains("Smith") &&
                        content.contains(testAppointment.getScheduledTime().toString())
        );
    }

    @Test
    void sendPrescriptionRefillReminder_WithZeroDaysRemaining_ShouldStillSend() {
        // Given
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.PRESCRIPTION_REFILL_REMINDER
        )).thenReturn(List.of(emailPreference));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendPrescriptionRefillReminder(testUser, "Medication", 0);

        // Then
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(
                anyString(),
                anyString(),
                contentCaptor.capture()
        );

        assertThat(contentCaptor.getValue()).contains("0 days");
    }

    @Test
    void sendNotification_WhenEmailSucceedsAndPushFails_ShouldLogBothSeparately() {
        // Given
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.APPOINTMENT_CONFIRMED
        )).thenReturn(List.of(emailPreference, pushPreference));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        when(pushService.sendPushNotification(any())).thenReturn(false);

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        when(logRepository.save(logCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendNotification(
                testUser,
                NotificationType.APPOINTMENT_CONFIRMED,
                "Test Subject",
                "Test Content",
                null
        );

        // Then
        List<NotificationLog> savedLogs = logCaptor.getAllValues();
        assertThat(savedLogs).hasSize(2);

        NotificationLog emailLog = savedLogs.stream()
                .filter(log -> log.getChannel() == NotificationChannel.EMAIL)
                .findFirst()
                .orElseThrow();
        assertThat(emailLog.getStatus()).isEqualTo(NotificationStatus.SENT);

        NotificationLog pushLog = savedLogs.stream()
                .filter(log -> log.getChannel() == NotificationChannel.PUSH)
                .findFirst()
                .orElseThrow();
        assertThat(pushLog.getStatus()).isEqualTo(NotificationStatus.FAILED);
    }

    @Test
    void sendNotification_ShouldSetPendingStatusInitially() {
        // Given
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.APPOINTMENT_CONFIRMED
        )).thenReturn(List.of(emailPreference));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        when(logRepository.save(logCaptor.capture())).thenAnswer(invocation -> {
            NotificationLog log = invocation.getArgument(0);
            // Verify initial status before completion
            if (log.getSentAt() == null) {
                assertThat(log.getStatus()).isEqualTo(NotificationStatus.PENDING);
            }
            return log;
        });

        // When
        notificationService.sendNotification(
                testUser,
                NotificationType.APPOINTMENT_CONFIRMED,
                "Test Subject",
                "Test Content",
                null
        );

        // Then
        verify(logRepository).save(any(NotificationLog.class));
    }

    @Test
    void cleanUp_WithSpecificCutoffDate_ShouldPassCorrectDate() {
        // Given
        OffsetDateTime specificCutoff = OffsetDateTime.now().minusDays(30);
        when(logRepository.deletePendingNotificationsOlderThan(specificCutoff))
                .thenReturn(List.of());

        // When
        notificationService.cleanUp(specificCutoff);

        // Then
        verify(logRepository).deletePendingNotificationsOlderThan(specificCutoff);
    }

    @Test
    void sendLabResultsNotification_ShouldIncludePatientName() {
        // Given
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.LAB_RESULTS_READY
        )).thenReturn(List.of(emailPreference));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendLabResultsNotification(testUser, "X-Ray");

        // Then
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(
                anyString(),
                anyString(),
                contentCaptor.capture()
        );

        String content = contentCaptor.getValue();
        assertThat(content).contains("John");
        assertThat(content).contains("Doe");
        assertThat(content).contains("X-Ray");
    }

    @Test
    void sendPrescriptionReadyNotification_ShouldIncludePickupInstructions() {
        // Given
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.PRESCRIPTION_READY
        )).thenReturn(List.of(emailPreference));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendPrescriptionReadyNotification(testUser, "CVS Pharmacy");

        // Then
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(
                anyString(),
                anyString(),
                contentCaptor.capture()
        );

        String content = contentCaptor.getValue();
        assertThat(content).contains("ID");
        assertThat(content).contains("insurance card");
    }

    @Test
    void getFailedNotifications_ShouldOnlyReturnFailedStatus() {
        // Given
        NotificationLog sentLog = new NotificationLog();
        sentLog.setStatus(NotificationStatus.SENT);
        NotificationLog failedLog = new NotificationLog();
        failedLog.setStatus(NotificationStatus.FAILED);

        when(logRepository.findByUserIdAndStatus(testUser.getId(), NotificationStatus.FAILED))
                .thenReturn(List.of(failedLog));
        when(notificationMapper.toLogDto(failedLog)).thenReturn(logResponseDto);

        // When
        List<NotificationLogResponseDto> result = notificationService.getFailedNotifications(testUser.getId());

        // Then
        assertThat(result).hasSize(1);
        verify(logRepository).findByUserIdAndStatus(testUser.getId(), NotificationStatus.FAILED);
    }

    @Test
    void sendAppointmentReminder24Hours_PatientMessage_ShouldIncludePatientSpecificContent() {
        // Given
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.APPOINTMENT_REMINDER_24H
        )).thenReturn(List.of(emailPreference));
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendAppointmentReminder24Hours(testAppointment);

        // Then
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService, atLeastOnce()).sendEmail(
                eq("patient@test.com"),
                subjectCaptor.capture(),
                anyString()
        );

        assertThat(subjectCaptor.getValue()).contains("Appointment Reminder");
    }

    @Test
    void sendAppointmentReminder24Hours_DoctorMessage_ShouldIncludePatientName() {
        NotificationPreference doctorEmailPref = new NotificationPreference();
        doctorEmailPref.setId(UUID.randomUUID());
        doctorEmailPref.setUser(doctorUser);
        doctorEmailPref.setNotificationType(NotificationType.APPOINTMENT_REMINDER_24H);
        doctorEmailPref.setChannel(NotificationChannel.EMAIL);
        doctorEmailPref.setIsEnabled(true);

        // Stub for both patient and doctor
        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                testUser.getId(),
                NotificationType.APPOINTMENT_REMINDER_24H
        )).thenReturn(List.of()); // Patient has no preferences

        when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
                doctorUser.getId(),
                NotificationType.APPOINTMENT_REMINDER_24H
        )).thenReturn(List.of(doctorEmailPref));

        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        // When
        notificationService.sendAppointmentReminder24Hours(testAppointment);

        // Then
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(
                eq("doctor@test.com"),
                subjectCaptor.capture(),
                anyString()
        );

        assertThat(subjectCaptor.getValue()).contains("John");
        }


@Test
void getUserNotificationHistory_WhenNoLogs_ShouldReturnEmptyList() {
    // Given
    when(logRepository.findByUserId(testUser.getId())).thenReturn(List.of());

    // When
    List<NotificationLogResponseDto> result = notificationService.getUserNotificationHistory(testUser.getId());

    // Then
    assertThat(result).isEmpty();
    verify(logRepository).findByUserId(testUser.getId());
    verify(notificationMapper, never()).toLogDto(any());
}

@Test
void getFailedNotifications_ShouldReturnFailedNotifications() {
    // Given
    testLog.setStatus(NotificationStatus.FAILED);
    List<NotificationLog> failedLogs = List.of(testLog);
    when(logRepository.findByUserIdAndStatus(testUser.getId(), NotificationStatus.FAILED))
            .thenReturn(failedLogs);
    when(notificationMapper.toLogDto(testLog)).thenReturn(logResponseDto);

    // When
    List<NotificationLogResponseDto> result = notificationService.getFailedNotifications(testUser.getId());

    // Then
    assertThat(result).hasSize(1);
    verify(logRepository).findByUserIdAndStatus(testUser.getId(), NotificationStatus.FAILED);
    verify(notificationMapper).toLogDto(testLog);
}

@Test
void getFailedNotifications_WhenNoFailedLogs_ShouldReturnEmptyList() {
    // Given
    when(logRepository.findByUserIdAndStatus(testUser.getId(), NotificationStatus.FAILED))
            .thenReturn(List.of());

    // When
    List<NotificationLogResponseDto> result = notificationService.getFailedNotifications(testUser.getId());

    // Then
    assertThat(result).isEmpty();
    verify(logRepository).findByUserIdAndStatus(testUser.getId(), NotificationStatus.FAILED);
    verify(notificationMapper, never()).toLogDto(any());
}

@Test
void cleanUp_ShouldDeleteOldPendingNotifications() {
    // Given
    OffsetDateTime cutoff = OffsetDateTime.now().minusDays(7);
    when(logRepository.deletePendingNotificationsOlderThan(cutoff)).thenReturn(List.of());

    // When
    notificationService.cleanUp(cutoff);

    // Then
    verify(logRepository).deletePendingNotificationsOlderThan(cutoff);
}

@Test
void sendNotification_ShouldSetCorrectLogFields() {
    // Given
    when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
            testUser.getId(),
            NotificationType.APPOINTMENT_CONFIRMED
    )).thenReturn(List.of(emailPreference));
    when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

    ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
    when(logRepository.save(logCaptor.capture())).thenAnswer(i -> i.getArgument(0));

    // When
    notificationService.sendNotification(
            testUser,
            NotificationType.APPOINTMENT_CONFIRMED,
            "Test Subject",
            "Test Content",
            testAppointment
    );

    // Then
    NotificationLog savedLog = logCaptor.getValue();
    assertThat(savedLog.getUser()).isEqualTo(testUser);
    assertThat(savedLog.getNotificationType()).isEqualTo(NotificationType.APPOINTMENT_CONFIRMED);
    assertThat(savedLog.getChannel()).isEqualTo(NotificationChannel.EMAIL);
    assertThat(savedLog.getSubject()).isEqualTo("Test Subject");
    assertThat(savedLog.getContent()).isEqualTo("Test Content");
    assertThat(savedLog.getAppointment()).isEqualTo(testAppointment);
    assertThat(savedLog.getRecipient()).isEqualTo("patient@test.com");
    assertThat(savedLog.getStatus()).isEqualTo(NotificationStatus.SENT);
    assertThat(savedLog.getSentAt()).isNotNull();
}

@Test
void sendNotification_WithSuccessfulEmail_ShouldSetSentStatus() {
    // Given
    when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
            testUser.getId(),
            NotificationType.APPOINTMENT_CONFIRMED
    )).thenReturn(List.of(emailPreference));
    when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

    ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
    when(logRepository.save(logCaptor.capture())).thenAnswer(i -> i.getArgument(0));

    // When
    notificationService.sendNotification(
            testUser,
            NotificationType.APPOINTMENT_CONFIRMED,
            "Test Subject",
            "Test Content",
            null
    );

    // Then
    NotificationLog savedLog = logCaptor.getValue();
    assertThat(savedLog.getStatus()).isEqualTo(NotificationStatus.SENT);
    assertThat(savedLog.getSentAt()).isNotNull();
    assertThat(savedLog.getErrorMessage()).isNull();
}

@Test
void sendAppointmentReminder24Hours_ShouldContainCorrectTimeframe() {
    // Given
    when(preferenceRepository.findEnabledPreferencesByUserIdAndType(any(), any()))
            .thenReturn(List.of(emailPreference));
    when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
    when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

    // When
    notificationService.sendAppointmentReminder24Hours(testAppointment);

    // Then
    ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService, times(2)).sendEmail(
            anyString(),
            anyString(),
            contentCaptor.capture()
    );

    List<String> contents = contentCaptor.getAllValues();
    assertThat(contents).allMatch(content -> content.contains("24 hours"));
}

@Test
void sendAppointmentReminder1Hour_ShouldContainCorrectTimeframe() {
    // Given
    when(preferenceRepository.findEnabledPreferencesByUserIdAndType(any(), any()))
            .thenReturn(List.of(emailPreference));
    when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
    when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

    // When
    notificationService.sendAppointmentReminder1Hour(testAppointment);

    // Then
    ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService, times(2)).sendEmail(
            anyString(),
            anyString(),
            contentCaptor.capture()
    );

    List<String> contents = contentCaptor.getAllValues();
    assertThat(contents).allMatch(content -> content.contains("1 hour"));
}

@Test
void sendNotification_WithMultipleFailures_ShouldLogEachSeparately() {
    // Given
    when(preferenceRepository.findEnabledPreferencesByUserIdAndType(
            testUser.getId(),
            NotificationType.APPOINTMENT_CONFIRMED
    )).thenReturn(List.of(emailPreference, pushPreference));
    when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(false);
    when(pushService.sendPushNotification(any())).thenReturn(false);

    ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
    when(logRepository.save(logCaptor.capture())).thenAnswer(i -> i.getArgument(0));

    // When
    notificationService.sendNotification(
            testUser,
            NotificationType.APPOINTMENT_CONFIRMED,
            "Test Subject",
            "Test Content",
            null
    );

    // Then
    List<NotificationLog> savedLogs = logCaptor.getAllValues();
    assertThat(savedLogs).hasSize(2);
    assertThat(savedLogs).allMatch(log -> log.getStatus() == NotificationStatus.FAILED);
}


}