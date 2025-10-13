package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.model.Appointment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Async
    public void sendAppointmentConfirmation(Appointment appointment) {
        try {
            // Email to patient
            sendEmail(
                    appointment.getPatient().getUser().getEmail(),
                    "Appointment Confirmation",
                    buildPatientConfirmationMessage(appointment)
            );

            // Email to doctor
            sendEmail(
                    appointment.getDoctor().getUser().getEmail(),
                    "New Appointment Scheduled",
                    buildDoctorNotificationMessage(appointment)
            );

            log.info("Appointment confirmation emails sent for appointment: {}", appointment.getId());
        } catch (Exception e) {
            log.error("Failed to send appointment confirmation emails", e);
        }
    }

    @Async
    public void sendAppointmentCancellation(Appointment appointment) {
        try {
            sendEmail(
                    appointment.getPatient().getUser().getEmail(),
                    "Appointment Cancelled",
                    buildCancellationMessage(appointment)
            );

            sendEmail(
                    appointment.getDoctor().getUser().getEmail(),
                    "Appointment Cancelled",
                    buildCancellationMessage(appointment)
            );

            log.info("Appointment cancellation emails sent for appointment: {}", appointment.getId());
        } catch (Exception e) {
            log.error("Failed to send cancellation emails", e);
        }
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("noreply@telemedicine.com");

        mailSender.send(message);
    }

    private String buildPatientConfirmationMessage(Appointment appointment) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        return String.format("""
            Dear %s %s,
            
            Your appointment has been confirmed!
            
            Doctor: Dr. %s %s
            Specialization: %s
            Date: %s
            Time: %s
            Duration: %d minutes
            
            Please arrive 10 minutes early for check-in.
            
            If you need to cancel or reschedule, please contact us at least 24 hours in advance.
            
            Best regards,
            Telemedicine Platform
            """,
                appointment.getPatient().getUser().getFirstName(),
                appointment.getPatient().getUser().getLastName(),
                appointment.getDoctor().getUser().getFirstName(),
                appointment.getDoctor().getUser().getLastName(),
                appointment.getDoctor().getSpecialization(),
                appointment.getScheduledDate().format(dateFormatter),
                appointment.getScheduledTime().format(timeFormatter),
                appointment.getDurationMinutes()
        );
    }

    private String buildDoctorNotificationMessage(Appointment appointment) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        return String.format("""
            Dear Dr. %s %s,
            
            A new appointment has been scheduled.
            
            Patient: %s %s
            Date: %s
            Time: %s
            Duration: %d minutes
            
            Please review your schedule accordingly.
            
            Best regards,
            Telemedicine Platform
            """,
                appointment.getDoctor().getUser().getFirstName(),
                appointment.getDoctor().getUser().getLastName(),
                appointment.getPatient().getUser().getFirstName(),
                appointment.getPatient().getUser().getLastName(),
                appointment.getScheduledDate().format(dateFormatter),
                appointment.getScheduledTime().format(timeFormatter),
                appointment.getDurationMinutes()
        );
    }

    private String buildCancellationMessage(Appointment appointment) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        return String.format("""
            Dear User,
            
            The following appointment has been cancelled:
            
            Date: %s
            Time: %s
            
            If you have any questions, please contact support.
            
            Best regards,
            Telemedicine Platform
            """,
                appointment.getScheduledDate().format(dateFormatter),
                appointment.getScheduledTime().format(timeFormatter)
        );
    }
}