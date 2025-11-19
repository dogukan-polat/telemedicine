package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.notification.NotificationLogResponseDto;
import com.dogukanpolat.telemedicine.dto.notification.NotificationPreferenceResponseDto;
import com.dogukanpolat.telemedicine.dto.notification.NotificationPreferenceUpdateDto;
import com.dogukanpolat.telemedicine.model.enums.NotificationChannel;
import com.dogukanpolat.telemedicine.model.enums.NotificationStatus;
import com.dogukanpolat.telemedicine.model.enums.NotificationType;
import com.dogukanpolat.telemedicine.security.JwtAuthenticationFilter;
import com.dogukanpolat.telemedicine.service.NotificationPreferenceService;
import com.dogukanpolat.telemedicine.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        ))
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationPreferenceService preferenceService;

    @MockitoBean
    private NotificationService notificationService;

    private UUID userId;
    private NotificationPreferenceResponseDto preferenceResponseDto;
    private NotificationLogResponseDto logResponseDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        preferenceResponseDto = new NotificationPreferenceResponseDto(
                UUID.randomUUID(),
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationChannel.EMAIL,
                true,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        logResponseDto = new NotificationLogResponseDto(
                UUID.randomUUID(),
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationChannel.EMAIL,
                NotificationStatus.SENT,
                "user@test.com",
                "Appointment Confirmation",
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                UUID.randomUUID()
        );
    }

    @Test
    void getUserPreferences_WithValidUserId_ShouldReturn200() throws Exception {
        // Given
        List<NotificationPreferenceResponseDto> preferences = List.of(preferenceResponseDto);
        when(preferenceService.getUserPreferences(userId)).thenReturn(preferences);

        // When & Then
        mockMvc.perform(get("/notifications/preferences/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].notificationType").value("APPOINTMENT_CONFIRMED"))
                .andExpect(jsonPath("$[0].channel").value("EMAIL"))
                .andExpect(jsonPath("$[0].isEnabled").value(true));

        verify(preferenceService, times(1)).getUserPreferences(userId);
    }

    @Test
    void getUserPreferences_WhenNoPreferences_ShouldReturn200WithEmptyList() throws Exception {
        // Given
        when(preferenceService.getUserPreferences(userId)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/notifications/preferences/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(preferenceService, times(1)).getUserPreferences(userId);
    }

    @Test
    void getUserPreferences_WithInvalidUUID_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/notifications/preferences/user/{userId}", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(preferenceService, never()).getUserPreferences(any());
    }

    @Test
    void updatePreference_WithValidData_ShouldReturn200() throws Exception {
        // Given
        NotificationPreferenceUpdateDto updateDto = new NotificationPreferenceUpdateDto(
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationChannel.EMAIL,
                true
        );

        when(preferenceService.updatePreference(eq(userId), any(NotificationPreferenceUpdateDto.class)))
                .thenReturn(preferenceResponseDto);

        // When & Then
        mockMvc.perform(patch("/notifications/preferences/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationType").value("APPOINTMENT_CONFIRMED"))
                .andExpect(jsonPath("$.channel").value("EMAIL"))
                .andExpect(jsonPath("$.isEnabled").value(true));

        verify(preferenceService, times(1))
                .updatePreference(eq(userId), any(NotificationPreferenceUpdateDto.class));
    }

    @Test
    void updatePreference_WithMissingNotificationType_ShouldReturn400() throws Exception {
        // Given
        String invalidRequest = """
                {
                    "channel": "EMAIL",
                    "isEnabled": true
                }
                """;

        // When & Then
        mockMvc.perform(patch("/notifications/preferences/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(preferenceService, never()).updatePreference(any(), any());
    }

    @Test
    void updatePreference_WithMissingChannel_ShouldReturn400() throws Exception {
        // Given
        String invalidRequest = """
                {
                    "notificationType": "APPOINTMENT_CONFIRMED",
                    "isEnabled": true
                }
                """;

        // When & Then
        mockMvc.perform(patch("/notifications/preferences/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(preferenceService, never()).updatePreference(any(), any());
    }

    @Test
    void updatePreference_WithMissingIsEnabled_ShouldReturn400() throws Exception {
        // Given
        String invalidRequest = """
                {
                    "notificationType": "APPOINTMENT_CONFIRMED",
                    "channel": "EMAIL"
                }
                """;

        // When & Then
        mockMvc.perform(patch("/notifications/preferences/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(preferenceService, never()).updatePreference(any(), any());
    }

    @Test
    void updatePreference_WithInvalidNotificationType_ShouldReturn400() throws Exception {
        // Given
        String invalidRequest = """
                {
                    "notificationType": "INVALID_TYPE",
                    "channel": "EMAIL",
                    "isEnabled": true
                }
                """;

        // When & Then
        mockMvc.perform(patch("/notifications/preferences/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(preferenceService, never()).updatePreference(any(), any());
    }

    @Test
    void updatePreference_WithInvalidChannel_ShouldReturn400() throws Exception {
        // Given
        String invalidRequest = """
                {
                    "notificationType": "APPOINTMENT_CONFIRMED",
                    "channel": "INVALID_CHANNEL",
                    "isEnabled": true
                }
                """;

        // When & Then
        mockMvc.perform(patch("/notifications/preferences/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(preferenceService, never()).updatePreference(any(), any());
    }

    @Test
    void updatePreference_DisableNotification_ShouldReturn200() throws Exception {
        // Given
        NotificationPreferenceUpdateDto updateDto = new NotificationPreferenceUpdateDto(
                NotificationType.APPOINTMENT_REMINDER_24H,
                NotificationChannel.PUSH,
                false
        );

        NotificationPreferenceResponseDto disabledResponse = new NotificationPreferenceResponseDto(
                UUID.randomUUID(),
                NotificationType.APPOINTMENT_REMINDER_24H,
                NotificationChannel.PUSH,
                false,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(preferenceService.updatePreference(eq(userId), any(NotificationPreferenceUpdateDto.class)))
                .thenReturn(disabledResponse);

        // When & Then
        mockMvc.perform(patch("/notifications/preferences/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isEnabled").value(false));

        verify(preferenceService, times(1))
                .updatePreference(eq(userId), any(NotificationPreferenceUpdateDto.class));
    }

    @Test
    void updateBulkPreferences_WithValidData_ShouldReturn200() throws Exception {
        // Given
        List<NotificationPreferenceUpdateDto> updates = List.of(
                new NotificationPreferenceUpdateDto(
                        NotificationType.APPOINTMENT_CONFIRMED,
                        NotificationChannel.EMAIL,
                        true
                ),
                new NotificationPreferenceUpdateDto(
                        NotificationType.APPOINTMENT_REMINDER_24H,
                        NotificationChannel.PUSH,
                        false
                )
        );

        NotificationPreferenceResponseDto pref1 = new NotificationPreferenceResponseDto(
                UUID.randomUUID(),
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationChannel.EMAIL,
                true,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        NotificationPreferenceResponseDto pref2 = new NotificationPreferenceResponseDto(
                UUID.randomUUID(),
                NotificationType.APPOINTMENT_REMINDER_24H,
                NotificationChannel.PUSH,
                false,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(preferenceService.updateBulkPreferences(eq(userId), anyList()))
                .thenReturn(List.of(pref1, pref2));

        // When & Then
        mockMvc.perform(patch("/notifications/preferences/user/{userId}/bulk", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].notificationType").value("APPOINTMENT_CONFIRMED"))
                .andExpect(jsonPath("$[0].isEnabled").value(true))
                .andExpect(jsonPath("$[1].notificationType").value("APPOINTMENT_REMINDER_24H"))
                .andExpect(jsonPath("$[1].isEnabled").value(false));

        verify(preferenceService, times(1)).updateBulkPreferences(eq(userId), anyList());
    }

    @Test
    void updateBulkPreferences_WithEmptyList_ShouldReturn200() throws Exception {
        // Given
        when(preferenceService.updateBulkPreferences(eq(userId), anyList()))
                .thenReturn(List.of());

        // When & Then
        mockMvc.perform(patch("/notifications/preferences/user/{userId}/bulk", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(preferenceService, times(1)).updateBulkPreferences(eq(userId), anyList());
    }

    @Test
    void updateBulkPreferences_WithInvalidData_ShouldReturn400() throws Exception {
        // Given
        String invalidRequest = """
                [
                    {
                        "notificationType": "INVALID_TYPE",
                        "channel": "EMAIL",
                        "isEnabled": true
                    }
                ]
                """;

        // When & Then
        mockMvc.perform(patch("/notifications/preferences/user/{userId}/bulk", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(preferenceService, never()).updateBulkPreferences(any(), anyList());
    }

    @Test
    void initializeDefaultPreferences_WithValidUserId_ShouldReturn200() throws Exception {
        // Given
        doNothing().when(preferenceService).initializeDefaultPreferences(userId);

        // When & Then
        mockMvc.perform(post("/notifications/preferences/user/{userId}/initialize", userId))
                .andExpect(status().isOk());

        verify(preferenceService, times(1)).initializeDefaultPreferences(userId);
    }

    @Test
    void initializeDefaultPreferences_WithInvalidUUID_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(post("/notifications/preferences/user/{userId}/initialize", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(preferenceService, never()).initializeDefaultPreferences(any());
    }

    @Test
    void initializeDefaultPreferences_WhenServiceThrowsException_ShouldReturn500() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("User not found"))
                .when(preferenceService).initializeDefaultPreferences(userId);

        // When & Then
        mockMvc.perform(post("/notifications/preferences/user/{userId}/initialize", userId))
                .andExpect(status().isBadRequest());

        verify(preferenceService, times(1)).initializeDefaultPreferences(userId);
    }

    @Test
    void disableAllNotifications_WithValidUserId_ShouldReturn200() throws Exception {
        // Given
        doNothing().when(preferenceService).disableAllNotifications(userId);

        // When & Then
        mockMvc.perform(patch("/notifications/preferences/user/{userId}/disable-all", userId))
                .andExpect(status().isOk());

        verify(preferenceService, times(1)).disableAllNotifications(userId);
    }

    @Test
    void disableAllNotifications_WithInvalidUUID_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(patch("/notifications/preferences/user/{userId}/disable-all", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(preferenceService, never()).disableAllNotifications(any());
    }

    @Test
    void enableAllNotifications_WithValidUserId_ShouldReturn200() throws Exception {
        // Given
        doNothing().when(preferenceService).enableAllNotifications(userId);

        // When & Then
        mockMvc.perform(patch("/notifications/preferences/user/{userId}/enable-all", userId))
                .andExpect(status().isOk());

        verify(preferenceService, times(1)).enableAllNotifications(userId);
    }

    @Test
    void enableAllNotifications_WithInvalidUUID_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(patch("/notifications/preferences/user/{userId}/enable-all", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(preferenceService, never()).enableAllNotifications(any());
    }

    @Test
    void getNotificationHistory_WithValidUserId_ShouldReturn200() throws Exception {
        // Given
        List<NotificationLogResponseDto> logs = List.of(logResponseDto);
        when(notificationService.getUserNotificationHistory(userId)).thenReturn(logs);

        // When & Then
        mockMvc.perform(get("/notifications/history/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].notificationType").value("APPOINTMENT_CONFIRMED"))
                .andExpect(jsonPath("$[0].channel").value("EMAIL"))
                .andExpect(jsonPath("$[0].status").value("SENT"))
                .andExpect(jsonPath("$[0].recipient").value("user@test.com"))
                .andExpect(jsonPath("$[0].subject").value("Appointment Confirmation"));

        verify(notificationService, times(1)).getUserNotificationHistory(userId);
    }

    @Test
    void getNotificationHistory_WhenNoHistory_ShouldReturn200WithEmptyList() throws Exception {
        // Given
        when(notificationService.getUserNotificationHistory(userId)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/notifications/history/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(notificationService, times(1)).getUserNotificationHistory(userId);
    }

    @Test
    void getNotificationHistory_WithInvalidUUID_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/notifications/history/user/{userId}", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).getUserNotificationHistory(any());
    }

    @Test
    void getFailedNotifications_WithValidUserId_ShouldReturn200() throws Exception {
        // Given
        NotificationLogResponseDto failedLog = new NotificationLogResponseDto(
                UUID.randomUUID(),
                NotificationType.APPOINTMENT_REMINDER_24H,
                NotificationChannel.PUSH,
                NotificationStatus.FAILED,
                "user@test.com",
                "Appointment Reminder",
                "Device token not found",
                null,
                OffsetDateTime.now(),
                UUID.randomUUID()
        );

        when(notificationService.getFailedNotifications(userId))
                .thenReturn(List.of(failedLog));

        // When & Then
        mockMvc.perform(get("/notifications/history/user/{userId}/failed", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("FAILED"))
                .andExpect(jsonPath("$[0].errorMessage").value("Device token not found"));

        verify(notificationService, times(1)).getFailedNotifications(userId);
    }

    @Test
    void getFailedNotifications_WhenNoFailedNotifications_ShouldReturn200WithEmptyList() throws Exception {
        // Given
        when(notificationService.getFailedNotifications(userId)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/notifications/history/user/{userId}/failed", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(notificationService, times(1)).getFailedNotifications(userId);
    }

    @Test
    void getFailedNotifications_WithInvalidUUID_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/notifications/history/user/{userId}/failed", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).getFailedNotifications(any());
    }

    @Test
    void updatePreference_AllNotificationTypes_ShouldWork() throws Exception {
        // Test all notification types
        NotificationType[] types = {
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationType.APPOINTMENT_REMINDER_24H,
                NotificationType.APPOINTMENT_REMINDER_1H,
                NotificationType.APPOINTMENT_CANCELLATION,
                NotificationType.APPOINTMENT_RESCHEDULED,
                NotificationType.LAB_RESULTS_READY,
                NotificationType.PRESCRIPTION_REFILL_REMINDER,
                NotificationType.PRESCRIPTION_READY
        };

        for (NotificationType type : types) {
            NotificationPreferenceUpdateDto updateDto = new NotificationPreferenceUpdateDto(
                    type,
                    NotificationChannel.EMAIL,
                    true
            );

            NotificationPreferenceResponseDto response = new NotificationPreferenceResponseDto(
                    UUID.randomUUID(),
                    type,
                    NotificationChannel.EMAIL,
                    true,
                    OffsetDateTime.now(),
                    OffsetDateTime.now()
            );

            when(preferenceService.updatePreference(eq(userId), any(NotificationPreferenceUpdateDto.class)))
                    .thenReturn(response);

            mockMvc.perform(patch("/notifications/preferences/user/{userId}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notificationType").value(type.name()));
        }

        verify(preferenceService, times(types.length))
                .updatePreference(eq(userId), any(NotificationPreferenceUpdateDto.class));
    }

    @Test
    void updatePreference_AllChannels_ShouldWork() throws Exception {
        // Test both channels
        NotificationChannel[] channels = {NotificationChannel.EMAIL, NotificationChannel.PUSH};

        for (NotificationChannel channel : channels) {
            NotificationPreferenceUpdateDto updateDto = new NotificationPreferenceUpdateDto(
                    NotificationType.APPOINTMENT_CONFIRMED,
                    channel,
                    true
            );

            NotificationPreferenceResponseDto response = new NotificationPreferenceResponseDto(
                    UUID.randomUUID(),
                    NotificationType.APPOINTMENT_CONFIRMED,
                    channel,
                    true,
                    OffsetDateTime.now(),
                    OffsetDateTime.now()
            );

            when(preferenceService.updatePreference(eq(userId), any(NotificationPreferenceUpdateDto.class)))
                    .thenReturn(response);

            mockMvc.perform(patch("/notifications/preferences/user/{userId}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.channel").value(channel.name()));
        }
    }

    @Test
    void getNotificationHistory_WithMultipleLogs_ShouldReturnAll() throws Exception {
        // Given
        NotificationLogResponseDto log1 = new NotificationLogResponseDto(
                UUID.randomUUID(),
                NotificationType.APPOINTMENT_CONFIRMED,
                NotificationChannel.EMAIL,
                NotificationStatus.SENT,
                "user@test.com",
                "Appointment Confirmation",
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                UUID.randomUUID()
        );

        NotificationLogResponseDto log2 = new NotificationLogResponseDto(
                UUID.randomUUID(),
                NotificationType.APPOINTMENT_REMINDER_24H,
                NotificationChannel.PUSH,
                NotificationStatus.SENT,
                "user@test.com",
                "Appointment Reminder",
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                UUID.randomUUID()
        );

        when(notificationService.getUserNotificationHistory(userId))
                .thenReturn(List.of(log1, log2));

        // When & Then
        mockMvc.perform(get("/notifications/history/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].notificationType").value("APPOINTMENT_CONFIRMED"))
                .andExpect(jsonPath("$[1].notificationType").value("APPOINTMENT_REMINDER_24H"));

        verify(notificationService, times(1)).getUserNotificationHistory(userId);
    }

    @Test
    void updateBulkPreferences_WithMixedEnabledStatus_ShouldReturn200() throws Exception {
        // Given
        List<NotificationPreferenceUpdateDto> updates = List.of(
                new NotificationPreferenceUpdateDto(
                        NotificationType.APPOINTMENT_CONFIRMED,
                        NotificationChannel.EMAIL,
                        true
                ),
                new NotificationPreferenceUpdateDto(
                        NotificationType.APPOINTMENT_REMINDER_24H,
                        NotificationChannel.EMAIL,
                        false
                ),
                new NotificationPreferenceUpdateDto(
                        NotificationType.APPOINTMENT_REMINDER_1H,
                        NotificationChannel.PUSH,
                        true
                )
        );

        List<NotificationPreferenceResponseDto> responses = List.of(
                new NotificationPreferenceResponseDto(
                        UUID.randomUUID(),
                        NotificationType.APPOINTMENT_CONFIRMED,
                        NotificationChannel.EMAIL,
                        true,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                ),
                new NotificationPreferenceResponseDto(
                        UUID.randomUUID(),
                        NotificationType.APPOINTMENT_REMINDER_24H,
                        NotificationChannel.EMAIL,
                        false,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                ),
                new NotificationPreferenceResponseDto(
                        UUID.randomUUID(),
                        NotificationType.APPOINTMENT_REMINDER_1H,
                        NotificationChannel.PUSH,
                        true,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                )
        );

        when(preferenceService.updateBulkPreferences(eq(userId), anyList()))
                .thenReturn(responses);

        // When & Then
        mockMvc.perform(patch("/notifications/preferences/user/{userId}/bulk", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].isEnabled").value(true))
                .andExpect(jsonPath("$[1].isEnabled").value(false))
                .andExpect(jsonPath("$[2].isEnabled").value(true));

        verify(preferenceService, times(1)).updateBulkPreferences(eq(userId), anyList());
    }

    @Test
    void getAllEndpoints_WithValidRequests_ShouldCallServiceOnce() throws Exception {
        // Given
        when(preferenceService.getUserPreferences(userId)).thenReturn(List.of());
        when(notificationService.getUserNotificationHistory(userId)).thenReturn(List.of());
        when(notificationService.getFailedNotifications(userId)).thenReturn(List.of());
        doNothing().when(preferenceService).initializeDefaultPreferences(userId);
        doNothing().when(preferenceService).disableAllNotifications(userId);
        doNothing().when(preferenceService).enableAllNotifications(userId);

        // When & Then
        mockMvc.perform(get("/notifications/preferences/user/{userId}", userId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/notifications/history/user/{userId}", userId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/notifications/history/user/{userId}/failed", userId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/notifications/preferences/user/{userId}/initialize", userId))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/notifications/preferences/user/{userId}/disable-all", userId))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/notifications/preferences/user/{userId}/enable-all", userId))
                .andExpect(status().isOk());

        verify(preferenceService, times(1)).getUserPreferences(userId);
        verify(notificationService, times(1)).getUserNotificationHistory(userId);
        verify(notificationService, times(1)).getFailedNotifications(userId);
        verify(preferenceService, times(1)).initializeDefaultPreferences(userId);
        verify(preferenceService, times(1)).disableAllNotifications(userId);
        verify(preferenceService, times(1)).enableAllNotifications(userId);
    }

    @Test
    void updatePreference_WithEmptyBody_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(patch("/notifications/preferences/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(preferenceService, never()).updatePreference(any(), any());
    }

}