package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.notification.NotificationLogResponseDto;
import com.dogukanpolat.telemedicine.dto.notification.NotificationPreferenceResponseDto;
import com.dogukanpolat.telemedicine.dto.notification.NotificationPreferenceUpdateDto;
import com.dogukanpolat.telemedicine.service.NotificationPreferenceService;
import com.dogukanpolat.telemedicine.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for managing notification preferences and viewing notification history")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {
    private final NotificationPreferenceService preferenceService;
    private final NotificationService notificationService;

    @Operation(
            summary = "Get user notification preferences",
            description = "Retrieve all notification preferences for the authenticated user",
            parameters = @Parameter(description = "User ID", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Notification preferences retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NotificationPreferenceResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or expired JWT"
            )
    })
    @GetMapping("/preferences/user/{userId}")
    public ResponseEntity<List<NotificationPreferenceResponseDto>> getUserPreferences(@PathVariable UUID userId) {
        return ResponseEntity.ok(preferenceService.getUserPreferences(userId));
    }

    @Operation(
            summary = "Update notification preference",
            description = "Update a single notification preference for a user",
            parameters = @Parameter(description = "User ID", required = true),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Notification preference update",
                    required = true,
                    content = @Content(schema = @Schema(implementation = NotificationPreferenceUpdateDto.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Preference updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationPreferenceResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PatchMapping("/preferences/user/{userId}")
    public ResponseEntity<NotificationPreferenceResponseDto> updatePreference(
            @PathVariable UUID userId,
            @Valid @RequestBody NotificationPreferenceUpdateDto updateDto
    ) {
        return ResponseEntity.ok(preferenceService.updatePreference(userId, updateDto));
    }

    @Operation(
            summary = "Update multiple notification preferences",
            description = "Bulk update notification preferences for a user",
            parameters = @Parameter(description = "User ID", required = true),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of notification preference updates",
                    required = true,
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = NotificationPreferenceUpdateDto.class))
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Preferences updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NotificationPreferenceResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PatchMapping("/preferences/user/{userId}/bulk")
    public ResponseEntity<List<NotificationPreferenceResponseDto>> updateBulkPreferences(
            @PathVariable UUID userId,
            @Valid @RequestBody List<NotificationPreferenceUpdateDto> updates
    ) {
        return ResponseEntity.ok(preferenceService.updateBulkPreferences(userId, updates));
    }

    @Operation(
            summary = "Initialize default notification preferences",
            description = "Set up default notification preferences for a new user",
            parameters = @Parameter(description = "User ID", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Default preferences initialized successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PostMapping("/preferences/user/{userId}/initialize")
    public ResponseEntity<Void> initializeDefaultPreferences(@PathVariable UUID userId) {
        preferenceService.initializeDefaultPreferences(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Disable all notifications",
            description = "Disable all notification channels for a user",
            parameters = @Parameter(description = "User ID", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "All notifications disabled successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PatchMapping("/preferences/user/{userId}/disable-all")
    public ResponseEntity<Void> disableAllNotifications(@PathVariable UUID userId) {
        preferenceService.disableAllNotifications(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Enable all notifications",
            description = "Enable all notification channels for a user",
            parameters = @Parameter(description = "User ID", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "All notifications enabled successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PatchMapping("/preferences/user/{userId}/enable-all")
    public ResponseEntity<Void> enableAllNotifications(@PathVariable UUID userId) {
        preferenceService.enableAllNotifications(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get notification history",
            description = "Retrieve notification history for a user",
            parameters = @Parameter(description = "User ID", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Notification history retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NotificationLogResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @GetMapping("/history/user/{userId}")
    public ResponseEntity<List<NotificationLogResponseDto>> getNotificationHistory(@PathVariable UUID userId) {
        return ResponseEntity.ok(notificationService.getUserNotificationHistory(userId));
    }

    @Operation(
            summary = "Get failed notifications",
            description = "Retrieve all failed notifications for a user",
            parameters = @Parameter(description = "User ID", required = true)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Failed notifications retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NotificationLogResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @GetMapping("/history/user/{userId}/failed")
    public ResponseEntity<List<NotificationLogResponseDto>> getFailedNotifications(@PathVariable UUID userId) {
        return ResponseEntity.ok(notificationService.getFailedNotifications(userId));
    }
}
