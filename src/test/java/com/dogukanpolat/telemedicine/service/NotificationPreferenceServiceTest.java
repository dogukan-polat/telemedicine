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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceServiceTest {

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationPreferenceService notificationPreferenceService;

    private UUID userId;
    private UserModel testUser;
    private NotificationPreference testPreference;
    private NotificationPreferenceResponseDto responseDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = new UserModel();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        testPreference = new NotificationPreference();
        testPreference.setId(UUID.randomUUID());
        testPreference.setUser(testUser);
        testPreference.setNotificationType(NotificationType.APPOINTMENT_CONFIRMED);
        testPreference.setChannel(NotificationChannel.EMAIL);
        testPreference.setIsEnabled(true);
        testPreference.setCreatedAt(OffsetDateTime.now());
        testPreference.setUpdatedAt(OffsetDateTime.now());

        responseDto = new NotificationPreferenceResponseDto(
                testPreference.getId(),
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationChannel.EMAIL,
                true,
                testPreference.getCreatedAt(),
                testPreference.getUpdatedAt()
        );
    }

    @Test
    void getUserPreferences_ShouldReturnUserPreferences() {
        // Given
        List<NotificationPreference> preferences = List.of(testPreference);
        when(preferenceRepository.findByUserId(userId)).thenReturn(preferences);
        when(notificationMapper.toPreferenceDto(testPreference)).thenReturn(responseDto);

        // When
        List<NotificationPreferenceResponseDto> result = notificationPreferenceService.getUserPreferences(userId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().notificationType()).isEqualTo(NotificationType.APPOINTMENT_CONFIRMED);
        assertThat(result.getFirst().channel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(result.getFirst().isEnabled()).isTrue();
        verify(preferenceRepository).findByUserId(userId);
        verify(notificationMapper).toPreferenceDto(testPreference);
    }

    @Test
    void getUserPreferences_WhenNoPreferences_ShouldReturnEmptyList() {
        // Given
        when(preferenceRepository.findByUserId(userId)).thenReturn(List.of());

        // When
        List<NotificationPreferenceResponseDto> result = notificationPreferenceService.getUserPreferences(userId);

        // Then
        assertThat(result).isEmpty();
        verify(preferenceRepository).findByUserId(userId);
        verify(notificationMapper, never()).toPreferenceDto(any());
    }

    @Test
    void updatePreference_ExistingPreference_ShouldUpdateAndReturn() {
        // Given
        NotificationPreferenceUpdateDto updateDto = new NotificationPreferenceUpdateDto(
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationChannel.EMAIL,
                false
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(preferenceRepository.findByUserIdAndTypeAndChannel(
                userId,
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationChannel.EMAIL
        )).thenReturn(Optional.of(testPreference));
        when(preferenceRepository.save(testPreference)).thenReturn(testPreference);
        when(notificationMapper.toPreferenceDto(testPreference)).thenReturn(responseDto);

        // When
        NotificationPreferenceResponseDto result = notificationPreferenceService.updatePreference(userId, updateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(testPreference.getIsEnabled()).isFalse();
        verify(userRepository).findById(userId);
        verify(preferenceRepository).findByUserIdAndTypeAndChannel(
                userId,
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationChannel.EMAIL
        );
        verify(preferenceRepository).save(testPreference);
        verify(notificationMapper).toPreferenceDto(testPreference);
    }

    @Test
    void updatePreference_NewPreference_ShouldCreateAndReturn() {
        // Given
        NotificationPreferenceUpdateDto updateDto = new NotificationPreferenceUpdateDto(
                NotificationType.APPOINTMENT_REMINDER_24H,
                NotificationChannel.PUSH,
                true
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(preferenceRepository.findByUserIdAndTypeAndChannel(
                userId,
                NotificationType.APPOINTMENT_REMINDER_24H,
                NotificationChannel.PUSH
        )).thenReturn(Optional.empty());

        ArgumentCaptor<NotificationPreference> preferenceCaptor = ArgumentCaptor.forClass(NotificationPreference.class);
        when(preferenceRepository.save(preferenceCaptor.capture())).thenAnswer(i -> i.getArgument(0));
        when(notificationMapper.toPreferenceDto(any(NotificationPreference.class))).thenReturn(responseDto);

        // When
        NotificationPreferenceResponseDto result = notificationPreferenceService.updatePreference(userId, updateDto);

        // Then
        assertThat(result).isNotNull();
        NotificationPreference savedPreference = preferenceCaptor.getValue();
        assertThat(savedPreference.getUser()).isEqualTo(testUser);
        assertThat(savedPreference.getNotificationType()).isEqualTo(NotificationType.APPOINTMENT_REMINDER_24H);
        assertThat(savedPreference.getChannel()).isEqualTo(NotificationChannel.PUSH);
        assertThat(savedPreference.getIsEnabled()).isTrue();
        verify(userRepository).findById(userId);
        verify(preferenceRepository).save(any(NotificationPreference.class));
    }

    @Test
    void updatePreference_UserNotFound_ShouldThrowException() {
        // Given
        NotificationPreferenceUpdateDto updateDto = new NotificationPreferenceUpdateDto(
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationChannel.EMAIL,
                true
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> notificationPreferenceService.updatePreference(userId, updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(userId);
        verify(preferenceRepository, never()).save(any());
    }

    @Test
    void updateBulkPreferences_ShouldUpdateMultiplePreferences() {
        // Given
        NotificationPreferenceUpdateDto update1 = new NotificationPreferenceUpdateDto(
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationChannel.EMAIL,
                true
        );
        NotificationPreferenceUpdateDto update2 = new NotificationPreferenceUpdateDto(
                NotificationType.APPOINTMENT_REMINDER_24H,
                NotificationChannel.PUSH,
                false
        );
        List<NotificationPreferenceUpdateDto> updates = List.of(update1, update2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(preferenceRepository.findByUserIdAndTypeAndChannel(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(preferenceRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));
        when(notificationMapper.toPreferenceDto(any())).thenReturn(responseDto);

        // When
        List<NotificationPreferenceResponseDto> result = notificationPreferenceService.updateBulkPreferences(userId, updates);

        // Then
        assertThat(result).hasSize(2);
        verify(userRepository).findById(userId);
        verify(preferenceRepository).saveAll(any());
        verify(notificationMapper, times(2)).toPreferenceDto(any());
    }

    @Test
    void updateBulkPreferences_UserNotFound_ShouldThrowException() {
        // Given
        List<NotificationPreferenceUpdateDto> updates = List.of(
                new NotificationPreferenceUpdateDto(
                        NotificationType.APPOINTMENT_CONFIRMED,
                        NotificationChannel.EMAIL,
                        true
                )
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> notificationPreferenceService.updateBulkPreferences(userId, updates))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(userId);
        verify(preferenceRepository, never()).saveAll(any());
    }

    @Test
    void updateBulkPreferences_WithMixedExistingAndNewPreferences_ShouldHandleBoth() {
        // Given
        NotificationPreference existingPref = new NotificationPreference();
        existingPref.setId(UUID.randomUUID());
        existingPref.setUser(testUser);
        existingPref.setNotificationType(NotificationType.APPOINTMENT_CONFIRMED);
        existingPref.setChannel(NotificationChannel.EMAIL);
        existingPref.setIsEnabled(false);

        NotificationPreferenceUpdateDto update1 = new NotificationPreferenceUpdateDto(
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationChannel.EMAIL,
                true  // Update existing
        );
        NotificationPreferenceUpdateDto update2 = new NotificationPreferenceUpdateDto(
                NotificationType.APPOINTMENT_REMINDER_24H,
                NotificationChannel.PUSH,
                true  // New preference
        );
        List<NotificationPreferenceUpdateDto> updates = List.of(update1, update2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(preferenceRepository.findByUserIdAndTypeAndChannel(
                userId,
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationChannel.EMAIL
        )).thenReturn(Optional.of(existingPref));
        when(preferenceRepository.findByUserIdAndTypeAndChannel(
                userId,
                NotificationType.APPOINTMENT_REMINDER_24H,
                NotificationChannel.PUSH
        )).thenReturn(Optional.empty());

        ArgumentCaptor<List<NotificationPreference>> captor = ArgumentCaptor.forClass(List.class);
        when(preferenceRepository.saveAll(captor.capture())).thenAnswer(i -> i.getArgument(0));
        when(notificationMapper.toPreferenceDto(any())).thenReturn(responseDto);

        // When
        List<NotificationPreferenceResponseDto> result = notificationPreferenceService.updateBulkPreferences(userId, updates);

        // Then
        assertThat(result).hasSize(2);
        List<NotificationPreference> savedPreferences = captor.getValue();
        assertThat(savedPreferences).hasSize(2);
        assertThat(savedPreferences.get(0).getIsEnabled()).isTrue();  // Updated existing
        assertThat(savedPreferences.get(1).getIsEnabled()).isTrue();  // New preference
        verify(preferenceRepository).saveAll(any());
    }

    @Test
    void initializeDefaultPreferences_ShouldCreateDefaultPreferences() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(preferenceRepository.findByUserIdAndTypeAndChannel(any(), any(), any()))
                .thenReturn(Optional.empty());

        ArgumentCaptor<List<NotificationPreference>> captor = ArgumentCaptor.forClass(List.class);
        when(preferenceRepository.saveAll(captor.capture())).thenAnswer(i -> i.getArgument(0));

        // When
        notificationPreferenceService.initializeDefaultPreferences(userId);

        // Then
        List<NotificationPreference> savedPreferences = captor.getValue();
        assertThat(savedPreferences).hasSize(5);  // 5 appointment-related notification types
        assertThat(savedPreferences).allMatch(pref -> pref.getChannel() == NotificationChannel.EMAIL);
        assertThat(savedPreferences).allMatch(NotificationPreference::getIsEnabled);
        assertThat(savedPreferences).allMatch(pref -> pref.getUser().equals(testUser));

        verify(userRepository).findById(userId);
        verify(preferenceRepository, times(5)).findByUserIdAndTypeAndChannel(any(), any(), any());
        verify(preferenceRepository).saveAll(any());
    }

    @Test
    void initializeDefaultPreferences_WhenPreferencesAlreadyExist_ShouldNotCreateDuplicates() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(preferenceRepository.findByUserIdAndTypeAndChannel(any(), any(), any()))
                .thenReturn(Optional.of(testPreference));

        // When
        notificationPreferenceService.initializeDefaultPreferences(userId);

        // Then
        verify(userRepository).findById(userId);
        verify(preferenceRepository, times(5)).findByUserIdAndTypeAndChannel(any(), any(), any());
        verify(preferenceRepository, never()).saveAll(any());  // Nothing to save
    }

    @Test
    void initializeDefaultPreferences_UserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> notificationPreferenceService.initializeDefaultPreferences(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(userId);
        verify(preferenceRepository, never()).saveAll(any());
    }

    @Test
    void disableAllNotifications_ShouldDisableAllUserPreferences() {
        // Given
        NotificationPreference pref1 = new NotificationPreference();
        pref1.setIsEnabled(true);
        NotificationPreference pref2 = new NotificationPreference();
        pref2.setIsEnabled(true);
        NotificationPreference pref3 = new NotificationPreference();
        pref3.setIsEnabled(true);

        List<NotificationPreference> preferences = new ArrayList<>(List.of(pref1, pref2, pref3));

        when(preferenceRepository.findByUserId(userId)).thenReturn(preferences);
        when(preferenceRepository.saveAll(preferences)).thenReturn(preferences);

        // When
        notificationPreferenceService.disableAllNotifications(userId);

        // Then
        assertThat(preferences).allMatch(pref -> !pref.getIsEnabled());
        verify(preferenceRepository).findByUserId(userId);
        verify(preferenceRepository).saveAll(preferences);
    }

    @Test
    void disableAllNotifications_WhenNoPreferences_ShouldNotFail() {
        // Given
        when(preferenceRepository.findByUserId(userId)).thenReturn(List.of());

        // When
        notificationPreferenceService.disableAllNotifications(userId);

        // Then
        verify(preferenceRepository).findByUserId(userId);
        verify(preferenceRepository).saveAll(List.of());
    }

    @Test
    void enableAllNotifications_ShouldEnableAllUserPreferences() {
        // Given
        NotificationPreference pref1 = new NotificationPreference();
        pref1.setIsEnabled(false);
        NotificationPreference pref2 = new NotificationPreference();
        pref2.setIsEnabled(false);
        NotificationPreference pref3 = new NotificationPreference();
        pref3.setIsEnabled(false);

        List<NotificationPreference> preferences = new ArrayList<>(List.of(pref1, pref2, pref3));

        when(preferenceRepository.findByUserId(userId)).thenReturn(preferences);
        when(preferenceRepository.saveAll(preferences)).thenReturn(preferences);

        // When
        notificationPreferenceService.enableAllNotifications(userId);

        // Then
        assertThat(preferences).allMatch(NotificationPreference::getIsEnabled);
        verify(preferenceRepository).findByUserId(userId);
        verify(preferenceRepository).saveAll(preferences);
    }

    @Test
    void enableAllNotifications_WhenNoPreferences_ShouldNotFail() {
        // Given
        when(preferenceRepository.findByUserId(userId)).thenReturn(List.of());

        // When
        notificationPreferenceService.enableAllNotifications(userId);

        // Then
        verify(preferenceRepository).findByUserId(userId);
        verify(preferenceRepository).saveAll(List.of());
    }

    @Test
    void updatePreference_ShouldTogglePreferenceState() {
        // Given
        NotificationPreferenceUpdateDto updateDto = new NotificationPreferenceUpdateDto(
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationChannel.EMAIL,
                false
        );

        testPreference.setIsEnabled(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(preferenceRepository.findByUserIdAndTypeAndChannel(
                userId,
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationChannel.EMAIL
        )).thenReturn(Optional.of(testPreference));
        when(preferenceRepository.save(testPreference)).thenReturn(testPreference);
        when(notificationMapper.toPreferenceDto(testPreference)).thenReturn(responseDto);

        // When
        notificationPreferenceService.updatePreference(userId, updateDto);

        // Then
        assertThat(testPreference.getIsEnabled()).isFalse();
        verify(preferenceRepository).save(testPreference);
    }

    @Test
    void updateBulkPreferences_WithEmptyList_ShouldReturnEmptyList() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(preferenceRepository.saveAll(any())).thenReturn(List.of());

        // When
        List<NotificationPreferenceResponseDto> result = notificationPreferenceService.updateBulkPreferences(
                userId,
                List.of()
        );

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findById(userId);
        verify(preferenceRepository).saveAll(any());
    }

    @Test
    void initializeDefaultPreferences_ShouldOnlyCreateAppointmentRelatedPreferences() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(preferenceRepository.findByUserIdAndTypeAndChannel(any(), any(), any()))
                .thenReturn(Optional.empty());

        ArgumentCaptor<List<NotificationPreference>> captor = ArgumentCaptor.forClass(List.class);
        when(preferenceRepository.saveAll(captor.capture())).thenAnswer(i -> i.getArgument(0));

        // When
        notificationPreferenceService.initializeDefaultPreferences(userId);

        // Then
        List<NotificationPreference> savedPreferences = captor.getValue();
        assertThat(savedPreferences).hasSize(5);
        assertThat(savedPreferences).extracting(NotificationPreference::getNotificationType)
                .containsExactlyInAnyOrder(
                        NotificationType.APPOINTMENT_CONFIRMED,
                        NotificationType.APPOINTMENT_REMINDER_24H,
                        NotificationType.APPOINTMENT_REMINDER_1H,
                        NotificationType.APPOINTMENT_CANCELLATION,
                        NotificationType.APPOINTMENT_RESCHEDULED
                );
    }

    @Test
    void disableAllNotifications_WithMixedStates_ShouldDisableAll() {
        // Given
        NotificationPreference enabledPref = new NotificationPreference();
        enabledPref.setIsEnabled(true);
        NotificationPreference alreadyDisabledPref = new NotificationPreference();
        alreadyDisabledPref.setIsEnabled(false);

        List<NotificationPreference> preferences = new ArrayList<>(List.of(enabledPref, alreadyDisabledPref));

        when(preferenceRepository.findByUserId(userId)).thenReturn(preferences);
        when(preferenceRepository.saveAll(preferences)).thenReturn(preferences);

        // When
        notificationPreferenceService.disableAllNotifications(userId);

        // Then
        assertThat(preferences).allMatch(pref -> !pref.getIsEnabled());
        verify(preferenceRepository).saveAll(preferences);
    }

    @Test
    void enableAllNotifications_WithMixedStates_ShouldEnableAll() {
        // Given
        NotificationPreference disabledPref = new NotificationPreference();
        disabledPref.setIsEnabled(false);
        NotificationPreference alreadyEnabledPref = new NotificationPreference();
        alreadyEnabledPref.setIsEnabled(true);

        List<NotificationPreference> preferences = new ArrayList<>(List.of(disabledPref, alreadyEnabledPref));

        when(preferenceRepository.findByUserId(userId)).thenReturn(preferences);
        when(preferenceRepository.saveAll(preferences)).thenReturn(preferences);

        // When
        notificationPreferenceService.enableAllNotifications(userId);

        // Then
        assertThat(preferences).allMatch(NotificationPreference::getIsEnabled);
        verify(preferenceRepository).saveAll(preferences);
    }

    @Test
    void getUserPreferences_WithMultipleChannels_ShouldReturnAll() {
        // Given
        NotificationPreference emailPref = new NotificationPreference();
        emailPref.setId(UUID.randomUUID());
        emailPref.setNotificationType(NotificationType.APPOINTMENT_CONFIRMED);
        emailPref.setChannel(NotificationChannel.EMAIL);
        emailPref.setIsEnabled(true);

        NotificationPreference pushPref = new NotificationPreference();
        pushPref.setId(UUID.randomUUID());
        pushPref.setNotificationType(NotificationType.APPOINTMENT_CONFIRMED);
        pushPref.setChannel(NotificationChannel.PUSH);
        pushPref.setIsEnabled(false);

        List<NotificationPreference> preferences = List.of(emailPref, pushPref);

        when(preferenceRepository.findByUserId(userId)).thenReturn(preferences);
        when(notificationMapper.toPreferenceDto(any())).thenReturn(responseDto);

        // When
        List<NotificationPreferenceResponseDto> result = notificationPreferenceService.getUserPreferences(userId);

        // Then
        assertThat(result).hasSize(2);
        verify(preferenceRepository).findByUserId(userId);
        verify(notificationMapper, times(2)).toPreferenceDto(any());
    }

    @Test
    void updatePreference_MultipleTimes_ShouldUpdateEachTime() {
        // Given
        NotificationPreferenceUpdateDto updateDto1 = new NotificationPreferenceUpdateDto(
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationChannel.EMAIL,
                false
        );
        NotificationPreferenceUpdateDto updateDto2 = new NotificationPreferenceUpdateDto(
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationChannel.EMAIL,
                true
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(preferenceRepository.findByUserIdAndTypeAndChannel(any(), any(), any()))
                .thenReturn(Optional.of(testPreference));
        when(preferenceRepository.save(testPreference)).thenReturn(testPreference);
        when(notificationMapper.toPreferenceDto(testPreference)).thenReturn(responseDto);

        // When
        notificationPreferenceService.updatePreference(userId, updateDto1);
        notificationPreferenceService.updatePreference(userId, updateDto2);

        // Then
        verify(preferenceRepository, times(2)).save(testPreference);
    }

    @Test
    void initializeDefaultPreferences_WithPartialExistingPreferences_ShouldOnlyCreateMissing() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Mock that APPOINTMENT_CONFIRMED already exists
        when(preferenceRepository.findByUserIdAndTypeAndChannel(
                userId,
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationChannel.EMAIL
        )).thenReturn(Optional.of(testPreference));

        // Other preferences don't exist
        when(preferenceRepository.findByUserIdAndTypeAndChannel(
                eq(userId),
                argThat(type -> type != NotificationType.APPOINTMENT_CONFIRMED),
                eq(NotificationChannel.EMAIL)
        )).thenReturn(Optional.empty());

        ArgumentCaptor<List<NotificationPreference>> captor = ArgumentCaptor.forClass(List.class);
        when(preferenceRepository.saveAll(captor.capture())).thenAnswer(i -> i.getArgument(0));

        // When
        notificationPreferenceService.initializeDefaultPreferences(userId);

        // Then
        List<NotificationPreference> savedPreferences = captor.getValue();
        assertThat(savedPreferences).hasSize(4);  // 5 total - 1 existing = 4 new
        assertThat(savedPreferences).noneMatch(pref ->
                pref.getNotificationType() == NotificationType.APPOINTMENT_CONFIRMED
        );
        verify(preferenceRepository).saveAll(any());
    }
}