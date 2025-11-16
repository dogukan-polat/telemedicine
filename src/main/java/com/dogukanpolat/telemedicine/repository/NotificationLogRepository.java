package com.dogukanpolat.telemedicine.repository;

import com.dogukanpolat.telemedicine.model.NotificationLog;
import com.dogukanpolat.telemedicine.model.enums.NotificationStatus;
import com.dogukanpolat.telemedicine.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    List<NotificationLog> findByUserId(UUID userId);

    List<NotificationLog> findByUserIdAndStatus(UUID userId, NotificationStatus status);

    List<NotificationLog> findByAppointmentId(UUID appointmentId);

    @Modifying
    @Query("DELETE FROM NotificationLog nl WHERE nl.createdAt < :before ")
    List<NotificationLog> deletePendingNotificationsOlderThan(
            OffsetDateTime before
    );

    @Query("SELECT nl FROM NotificationLog nl WHERE nl.user.id = :userId " +
            "AND nl.notificationType = :type AND nl.appointment.id = :appointmentId")
    List<NotificationLog> findByUserIdAndTypeAndAppointmentId(
            UUID userId,
            NotificationType type,
            UUID appointmentId
    );

    @Query("SELECT COUNT(nl) FROM NotificationLog nl WHERE nl.status = :status")
    Long countByStatus(NotificationStatus status);
}
