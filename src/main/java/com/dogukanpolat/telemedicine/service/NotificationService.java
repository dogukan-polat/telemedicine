package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.notification.NotificationLogResponseDto;
import com.dogukanpolat.telemedicine.dto.notification.PushNotificationDto;
import com.dogukanpolat.telemedicine.mappers.NotificationMapper;
import com.dogukanpolat.telemedicine.model.Appointment;
import com.dogukanpolat.telemedicine.model.NotificationLog;
import com.dogukanpolat.telemedicine.model.NotificationPreference;
import com.dogukanpolat.telemedicine.model.UserModel;
import com.dogukanpolat.telemedicine.model.enums.NotificationChannel;
import com.dogukanpolat.telemedicine.model.enums.NotificationStatus;
import com.dogukanpolat.telemedicine.model.enums.NotificationType;
import com.dogukanpolat.telemedicine.repository.NotificationLogRepository;
import com.dogukanpolat.telemedicine.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationLogRepository logRepository;
    private final EmailNotificationService emailService;
    private final PushNotificationService pushService;
    private final NotificationMapper notificationMapper;

    @Async
    @Transactional
    public void sendNotification(
            UserModel user,
            NotificationType type,
            String subject,
            String content,
            Appointment appointment
    ) {
        log.info("Preparing to send notification type: {} to user: {}", type, user.getId());

        // Get user's enabled notification preferences for this type
        List<NotificationPreference> preferences =
                preferenceRepository.findEnabledPreferencesByUserIdAndType(user.getId(), type);

        if (preferences.isEmpty()) {
            log.info("No enabled notification preferences found for user: {} and type: {}",
                    user.getId(), type);
            return;
        }

        // Send notification through each enabled channel
        for (NotificationPreference preference : preferences) {
            sendThroughChannel(user, type, subject, content, appointment, preference.getChannel());
        }
    }

    @Async
    public void sendAppointmentReminder24Hours(Appointment appointment) {
        log.info("Sending 24-hour reminder for appointment: {}", appointment.getId());

        String subject = "Appointment Reminder - Tomorrow";
        String content = buildAppointmentReminderMessage(appointment, "24 hours");

        // Notify patient
        sendNotification(
                appointment.getPatient().getUser(),
                NotificationType.APPOINTMENT_REMINDER_24H,
                subject,
                content,
                appointment
        );

        // Notify doctor
        sendNotification(
                appointment.getDoctor().getUser(),
                NotificationType.APPOINTMENT_REMINDER_24H,
                "Appointment Reminder - Patient: " + appointment.getPatient().getUser().getFirstName(),
                content,
                appointment
        );
    }

    @Async
    public void sendAppointmentReminder1Hour(Appointment appointment) {
        log.info("Sending 1-hour reminder for appointment: {}", appointment.getId());

        String subject = "Appointment Reminder - In 1 Hour";
        String content = buildAppointmentReminderMessage(appointment, "1 hour");

        // Notify patient
        sendNotification(
                appointment.getPatient().getUser(),
                NotificationType.APPOINTMENT_REMINDER_1H,
                subject,
                content,
                appointment
        );

        // Notify doctor
        sendNotification(
                appointment.getDoctor().getUser(),
                NotificationType.APPOINTMENT_REMINDER_1H,
                "Upcoming Appointment - Patient: " + appointment.getPatient().getUser().getFirstName(),
                content,
                appointment
        );
    }

    @Async
    public void sendLabResultsNotification(UserModel user, String labTestName) {
        log.info("Sending lab results notification to user: {}", user.getId());

        String subject = "Lab Results Ready - " + labTestName;
        String content = String.format("""
            Dear %s %s,
            
            Your lab results for %s are now ready.
            
            Please log in to your patient portal to view your results.
            If you have any questions, please contact your healthcare provider.
            
            Best regards,
            Telemedicine Platform
            """,
                user.getFirstName(),
                user.getLastName(),
                labTestName
        );

        sendNotification(user, NotificationType.LAB_RESULTS_READY, subject, content, null);
    }

    @Async
    public void sendPrescriptionRefillReminder(UserModel user, String medicationName, int daysRemaining) {
        log.info("Sending prescription refill reminder to user: {}", user.getId());

        String subject = "Prescription Refill Reminder - " + medicationName;
        String content = String.format("""
            Dear %s %s,
            
            This is a reminder that your prescription for %s has %d days remaining.
            
            Please contact your doctor to request a refill if needed.
            
            Best regards,
            Telemedicine Platform
            """,
                user.getFirstName(),
                user.getLastName(),
                medicationName,
                daysRemaining
        );

        sendNotification(user, NotificationType.PRESCRIPTION_REFILL_REMINDER, subject, content, null);
    }

    @Async
    public void sendPrescriptionReadyNotification(UserModel user, String pharmacyName) {
        log.info("Sending prescription ready notification to user: {}", user.getId());

        String subject = "Prescription Ready for Pickup";
        String content = String.format("""
            Dear %s %s,
            
            Your prescription is ready for pickup at %s.
            
            Please bring your ID and insurance card when picking up your medication.
            
            Best regards,
            Telemedicine Platform
            """,
                user.getFirstName(),
                user.getLastName(),
                pharmacyName
        );

        sendNotification(user, NotificationType.PRESCRIPTION_READY, subject, content, null);
    }

    public void cleanUp(OffsetDateTime olderThan) {
        logRepository.deletePendingNotificationsOlderThan(olderThan);
    }

    private void sendThroughChannel(
            UserModel user,
            NotificationType type,
            String subject,
            String content,
            Appointment appointment,
            NotificationChannel channel
    ) {
        NotificationLog notificationLog = createNotificationLog(user, type, channel, subject, content, appointment);

        try {
            String recipient = getRecipient(user, channel);
            notificationLog.setRecipient(recipient);

            boolean success = switch (channel) {
                case EMAIL -> emailService.sendEmail(recipient, subject, content);
                case PUSH -> {
                    String deviceToken = user.getDeviceToken();
                    if (deviceToken == null || deviceToken.isEmpty()) {
                        log.warn("No device token found for user: {}", user.getId());
                        yield false;
                    }

                    PushNotificationDto pushDto = new PushNotificationDto(
                            deviceToken,
                            user.getId(),
                            subject,
                            content
                    );
                    yield pushService.sendPushNotification(pushDto);
                }
            };

            if (success) {
                notificationLog.setStatus(NotificationStatus.SENT);
                notificationLog.setSentAt(OffsetDateTime.now());
                log.info("Successfully sent {} notification to user: {} via {}",
                        type, user.getId(), channel);
            } else {
                notificationLog.setStatus(NotificationStatus.FAILED);
                notificationLog.setErrorMessage("Failed to send notification");
                log.error("Failed to send {} notification to user: {} via {}",
                        type, user.getId(), channel);
            }
        } catch (Exception e) {
            notificationLog.setStatus(NotificationStatus.FAILED);
            notificationLog.setErrorMessage(e.getMessage());
            log.error("Error sending {} notification to user: {} via {}: {}",
                    type, user.getId(), channel, e.getMessage(), e);
        } finally {
            logRepository.save(notificationLog);
        }
    }

    private NotificationLog createNotificationLog(
            UserModel user,
            NotificationType type,
            NotificationChannel channel,
            String subject,
            String content,
            Appointment appointment
    ) {
        NotificationLog log = new NotificationLog();
        log.setUser(user);
        log.setNotificationType(type);
        log.setChannel(channel);
        log.setSubject(subject);
        log.setContent(content);
        log.setAppointment(appointment);
        log.setStatus(NotificationStatus.PENDING);
        return log;
    }

    private String getRecipient(UserModel user, NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> user.getEmail();
            case PUSH -> user.getId().toString(); // Device token would be stored separately
        };
    }

    private String buildAppointmentReminderMessage(Appointment appointment, String timeframe) {
        return String.format("""
            Your appointment with Dr. %s %s is coming up in %s.
            
            Date: %s
            Time: %s
            Duration: %d minutes
            
            Please arrive 10 minutes early.
            
            Best regards,
            Telemedicine Platform
            """,
                appointment.getDoctor().getUser().getFirstName(),
                appointment.getDoctor().getUser().getLastName(),
                timeframe,
                appointment.getScheduledDate(),
                appointment.getScheduledTime(),
                appointment.getDurationMinutes()
        );
    }

    public List<NotificationLogResponseDto> getUserNotificationHistory(UUID userId) {
        List<NotificationLog> logs = logRepository.findByUserId(userId);
        return logs.stream().map(notificationMapper::toLogDto).toList();
    }

    public List<NotificationLogResponseDto> getFailedNotifications(UUID userId) {
        List<NotificationLog> logs = logRepository.findByUserIdAndStatus(userId, NotificationStatus.FAILED);
        return logs.stream().map(notificationMapper::toLogDto).toList();
    }
}
