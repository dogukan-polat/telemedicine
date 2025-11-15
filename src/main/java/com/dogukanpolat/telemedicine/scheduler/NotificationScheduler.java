package com.dogukanpolat.telemedicine.scheduler;

import com.dogukanpolat.telemedicine.model.Appointment;
import com.dogukanpolat.telemedicine.model.enums.AppointmentStatus;
import com.dogukanpolat.telemedicine.repository.AppointmentRepository;
import com.dogukanpolat.telemedicine.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 * * * *")
    public void send24HourReminders() {
        log.info("Running 24-hour reminder scheduler");
        try {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            List<Appointment> appointments = appointmentRepository.findAll().stream()
                    .filter(apt -> apt.getScheduledDate().equals(tomorrow))
                    .filter(apt -> apt.getStatus() == AppointmentStatus.SCHEDULED ||
                            apt.getStatus() == AppointmentStatus.CONFIRMED)
                    .toList();

            log.info("Found {} appointments scheduled for tomorrow", appointments.size());

            for (Appointment appointment : appointments) {
                try {
                    notificationService.sendAppointmentReminder24Hours(appointment);
                } catch (Exception e) {
                    log.error("Failed to send 24-hour reminder for appointment: {}",
                            appointment.getId(), e);
                }
            }

            log.info("Completed 24-hour reminder processing");
        } catch (Exception e) {
            log.error("Error in 24-hour reminder scheduler", e);
        }
    }

    @Scheduled(cron = "0 */15 * * * *") // Every 15 minutes
    public void send1HourReminders() {
        log.info("Running 1-hour appointment reminder scheduler");

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourLater = now.plusHours(1);

            // Get appointments starting in the next hour (with 15-minute buffer)
            List<Appointment> appointments = appointmentRepository.findAll().stream()
                    .filter(apt -> {
                        LocalDateTime appointmentTime = LocalDateTime.of(
                                apt.getScheduledDate(),
                                apt.getScheduledTime()
                        );
                        return appointmentTime.isAfter(now) &&
                                appointmentTime.isBefore(oneHourLater.plusMinutes(15));
                    })
                    .filter(apt -> apt.getStatus() == AppointmentStatus.SCHEDULED ||
                            apt.getStatus() == AppointmentStatus.CONFIRMED)
                    .toList();

            log.info("Found {} appointments starting in the next hour", appointments.size());

            for (Appointment appointment : appointments) {
                try {
                    notificationService.sendAppointmentReminder1Hour(appointment);
                } catch (Exception e) {
                    log.error("Failed to send 1-hour reminder for appointment: {}",
                            appointment.getId(), e);
                }
            }

            log.info("Completed 1-hour reminder processing");
        } catch (Exception e) {
            log.error("Error in 1-hour reminder scheduler", e);
        }
    }

    @Scheduled(cron = "0 0 2 * * SUN") // Weekly on Sunday at 2 AM
    public void cleanupOldNotificationLogs() {
        log.info("Running notification logs cleanup scheduler");

        try {
            // TODO: Implement cleanup logic
            // Archive or delete notification logs older than 90 days

            log.info("Notification logs cleanup completed");
        } catch (Exception e) {
            log.error("Error in notification logs cleanup scheduler", e);
        }
    }
}
