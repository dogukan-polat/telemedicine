package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.notification.NotificationPreferenceResponseDto;
import com.dogukanpolat.telemedicine.dto.notification.NotificationPreferenceUpdateDto;
import com.dogukanpolat.telemedicine.mappers.NotificationMapper;
import com.dogukanpolat.telemedicine.model.NotificationPreference;
import com.dogukanpolat.telemedicine.model.UserModel;
import com.dogukanpolat.telemedicine.model.enums.NotificationChannel;
import com.dogukanpolat.telemedicine.model.enums.NotificationType;
import com.dogukanpolat.telemedicine.repository.NotificationPreferenceRepository;
import com.dogukanpolat.telemedicine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    public List<NotificationPreferenceResponseDto> getUserPreferences(UUID userId) {
        log.info("Fetching notification preferences for user: {}", userId);

        List<NotificationPreference> preferences = preferenceRepository.findByUserId(userId);

        return preferences.stream().map(notificationMapper::toPreferenceDto).toList();
    }

    @Transactional
    public NotificationPreferenceResponseDto updatePreference(UUID userId, NotificationPreferenceUpdateDto updateDto) {
        log.info("Updating notification preference for user: {}, type: {}, channel: {}",
                userId, updateDto.notificationType(), updateDto.channel());

        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        NotificationPreference preference = preferenceRepository
                .findByUserIdAndTypeAndChannel(userId, updateDto.notificationType(), updateDto.channel())
                .orElseGet(() -> {
                    NotificationPreference newPref = new NotificationPreference();
                    newPref.setUser(user);
                    newPref.setNotificationType(updateDto.notificationType());
                    newPref.setChannel(updateDto.channel());
                    return newPref;
                });

        preference.setIsEnabled(updateDto.isEnabled());

        NotificationPreference saved = preferenceRepository.save(preference);
        log.info("Successfully updated notification preference: {}", saved.getId());

        return notificationMapper.toPreferenceDto(saved);
    }

    @Transactional
    public List<NotificationPreferenceResponseDto> updateBulkPreferences(
            UUID userId,
            List<NotificationPreferenceUpdateDto> updates
    ) {
        log.info("Updating {} notification preferences for user: {}", updates.size(), userId);

        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<NotificationPreference> preferences = new ArrayList<>();

        for (NotificationPreferenceUpdateDto updateDto : updates) {
            NotificationPreference preference = preferenceRepository
                    .findByUserIdAndTypeAndChannel(userId, updateDto.notificationType(), updateDto.channel())
                    .orElseGet(() -> {
                        NotificationPreference newPref = new NotificationPreference();
                        newPref.setUser(user);
                        newPref.setNotificationType(updateDto.notificationType());
                        newPref.setChannel(updateDto.channel());
                        return newPref;
                    });

            preference.setIsEnabled(updateDto.isEnabled());
            preferences.add(preference);
        }

        List<NotificationPreference> saved = preferenceRepository.saveAll(preferences);
        log.info("Successfully updated {} notification preferences", saved.size());

        return saved.stream()
                .map(notificationMapper::toPreferenceDto).toList();
    }

    @Transactional
    public void initializeDefaultPreferences(UUID userId) {
        log.info("Initializing default notification preferences for user: {}", userId);

        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<NotificationPreference> preferences = new ArrayList<>();

        // Default appointment-related notifications via EMAIL
        NotificationType[] appointmentTypes = {
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationType.APPOINTMENT_REMINDER_24H,
                NotificationType.APPOINTMENT_REMINDER_1H,
                NotificationType.APPOINTMENT_CANCELLATION,
                NotificationType.APPOINTMENT_RESCHEDULED
        };

        for (NotificationType type : appointmentTypes) {
            if (preferenceRepository.findByUserIdAndTypeAndChannel(userId, type, NotificationChannel.EMAIL).isEmpty()) {
                NotificationPreference pref = new NotificationPreference();
                pref.setUser(user);
                pref.setNotificationType(type);
                pref.setChannel(NotificationChannel.EMAIL);
                pref.setIsEnabled(true);
                preferences.add(pref);
            }
        }

        if (!preferences.isEmpty()) {
            preferenceRepository.saveAll(preferences);
            log.info("Initialized {} default notification preferences for user: {}",
                    preferences.size(), userId);
        }
    }

    @Transactional
    public void disableAllNotifications(UUID userId) {
        log.info("Disabling all notifications for user: {}", userId);

        List<NotificationPreference> preferences = preferenceRepository.findByUserId(userId);
        preferences.forEach(pref -> pref.setIsEnabled(false));
        preferenceRepository.saveAll(preferences);

        log.info("Disabled {} notification preferences for user: {}", preferences.size(), userId);
    }

    @Transactional
    public void enableAllNotifications(UUID userId) {
        log.info("Enabling all notifications for user: {}", userId);

        List<NotificationPreference> preferences = preferenceRepository.findByUserId(userId);
        preferences.forEach(pref -> pref.setIsEnabled(true));
        preferenceRepository.saveAll(preferences);

        log.info("Enabled {} notification preferences for user: {}", preferences.size(), userId);
    }
}
