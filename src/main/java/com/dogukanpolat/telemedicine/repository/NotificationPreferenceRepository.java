package com.dogukanpolat.telemedicine.repository;

import com.dogukanpolat.telemedicine.model.NotificationPreference;
import com.dogukanpolat.telemedicine.model.enums.NotificationChannel;
import com.dogukanpolat.telemedicine.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    List<NotificationPreference> findByUserId(UUID userId);

    @Query("SELECT np FROM NotificationPreference np WHERE np.user.id = :userId " +
            "AND np.notificationType = :type AND np.channel = :channel")
    Optional<NotificationPreference> findByUserIdAndTypeAndChannel(
            UUID userId,
            NotificationType type,
            NotificationChannel channel
    );

    @Query("SELECT np FROM NotificationPreference np WHERE np.user.id = :userId " +
            "AND np.notificationType = :type AND np.isEnabled = true")
    List<NotificationPreference> findEnabledPreferencesByUserIdAndType(
            UUID userId,
            NotificationType type
    );

    void deleteByUserId(UUID userId);
}
