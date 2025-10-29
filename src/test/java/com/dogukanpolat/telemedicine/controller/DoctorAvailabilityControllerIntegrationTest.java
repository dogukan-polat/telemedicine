package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.availability.AvailabilityRequestDto;
import com.dogukanpolat.telemedicine.dto.availability.AvailabilityResponseDto;
import com.dogukanpolat.telemedicine.dto.availability.AvailabilityUpdateDto;
import com.dogukanpolat.telemedicine.dto.availability.BulkAvailabilityRequestDto;
import com.dogukanpolat.telemedicine.model.enums.DayOfWeek;
import com.dogukanpolat.telemedicine.security.JwtAuthenticationFilter;
import com.dogukanpolat.telemedicine.service.DoctorAvailabilityService;
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

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DoctorAvailabilityController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        ))
@AutoConfigureMockMvc(addFilters = false)
class DoctorAvailabilityControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DoctorAvailabilityService availabilityService;

    private UUID doctorId;
    private UUID availabilityId;
    private AvailabilityRequestDto requestDto;
    private AvailabilityResponseDto responseDto;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        availabilityId = UUID.randomUUID();

        requestDto = new AvailabilityRequestDto(
                doctorId,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );

        responseDto = new AvailabilityResponseDto(
                availabilityId,
                doctorId,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                true,
                OffsetDateTime.now()
        );
    }

    @Test
    void createAvailability_ShouldReturn201WithValidData() throws Exception {
        when(availabilityService.createAvailability(any(AvailabilityRequestDto.class))).thenReturn(responseDto);
        mockMvc.perform(post("/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(availabilityId.toString()))
                .andExpect(jsonPath("$.doctorId").value(doctorId.toString()))
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.startTime").value("09:00:00"))
                .andExpect(jsonPath("$.endTime").value("17:00:00"))
                .andExpect(jsonPath("$.isAvailable").value(true));

        verify(availabilityService).createAvailability(any(AvailabilityRequestDto.class));
    }

    @Test
    void createAvailability_WithMissingDoctorId_ShouldReturn400() throws Exception {
        // Given
        String invalidRequest = """
                {
                    "dayOfWeek": "MONDAY",
                    "startTime": "09:00:00",
                    "endTime": "17:00:00"
                }
                """;

        // When & Then
        mockMvc.perform(post("/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).createAvailability(any());
    }

    @Test
    void createAvailability_WithMissingDayOfWeek_ShouldReturn400() throws Exception {
        // Given
        String invalidRequest = String.format("""
                {
                    "doctorId": "%s",
                    "startTime": "09:00:00",
                    "endTime": "17:00:00"
                }
                """, doctorId);

        // When & Then
        mockMvc.perform(post("/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).createAvailability(any());
    }

    @Test
    void createAvailability_WithMissingTimes_ShouldReturn400() throws Exception {
        // Given
        String invalidRequest = String.format("""
                {
                    "doctorId": "%s",
                    "dayOfWeek": "MONDAY"
                }
                """, doctorId);

        // When & Then
        mockMvc.perform(post("/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).createAvailability(any());
    }

    @Test
    void createAvailability_WithInvalidDayOfWeek_ShouldReturn400() throws Exception {
        // Given
        String invalidRequest = String.format("""
                {
                    "doctorId": "%s",
                    "dayOfWeek": "INVALIDDAY",
                    "startTime": "09:00:00",
                    "endTime": "17:00:00"
                }
                """, doctorId);

        // When & Then
        mockMvc.perform(post("/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).createAvailability(any());
    }

    @Test
    void createBulkAvailability_WithValidData_ShouldReturn201() throws Exception {
        // Given
        BulkAvailabilityRequestDto bulkRequest = new BulkAvailabilityRequestDto(
                doctorId,
                List.of(
                        new BulkAvailabilityRequestDto.AvailabilitySlot(
                                DayOfWeek.MONDAY,
                                LocalTime.of(9, 0),
                                LocalTime.of(12, 0)
                        ),
                        new BulkAvailabilityRequestDto.AvailabilitySlot(
                                DayOfWeek.TUESDAY,
                                LocalTime.of(9, 0),
                                LocalTime.of(12, 0)
                        )
                )
        );

        AvailabilityResponseDto response1 = new AvailabilityResponseDto(
                UUID.randomUUID(), doctorId, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0), true, OffsetDateTime.now()
        );
        AvailabilityResponseDto response2 = new AvailabilityResponseDto(
                UUID.randomUUID(), doctorId, DayOfWeek.TUESDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0), true, OffsetDateTime.now()
        );

        when(availabilityService.createBulkAvailability(any(BulkAvailabilityRequestDto.class)))
                .thenReturn(List.of(response1, response2));

        // When & Then
        mockMvc.perform(post("/availability/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$[1].dayOfWeek").value("TUESDAY"))
                .andExpect(jsonPath("$[0].startTime").value("09:00:00"))
                .andExpect(jsonPath("$[0].endTime").value("12:00:00"));

        verify(availabilityService, times(1)).createBulkAvailability(any(BulkAvailabilityRequestDto.class));
    }

    @Test
    void createBulkAvailability_WithEmptySlots_ShouldReturn400() throws Exception {
        // Given
        String invalidRequest = String.format("""
                {
                    "doctorId": "%s",
                    "slots": []
                }
                """, doctorId);

        // When & Then
        mockMvc.perform(post("/availability/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).createBulkAvailability(any());
    }

    @Test
    void createBulkAvailability_WithNullSlots_ShouldReturn400() throws Exception {
        // Given
        String invalidRequest = String.format("""
                {
                    "doctorId": "%s"
                }
                """, doctorId);

        // When & Then
        mockMvc.perform(post("/availability/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).createBulkAvailability(any());
    }

    @Test
    void getDoctorAvailability_WithValidDoctorId_ShouldReturn200() throws Exception {
        // Given
        AvailabilityResponseDto monday = new AvailabilityResponseDto(
                UUID.randomUUID(), doctorId, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(17, 0), true, OffsetDateTime.now()
        );
        AvailabilityResponseDto tuesday = new AvailabilityResponseDto(
                UUID.randomUUID(), doctorId, DayOfWeek.TUESDAY,
                LocalTime.of(9, 0), LocalTime.of(17, 0), true, OffsetDateTime.now()
        );

        when(availabilityService.getDoctorAvailability(doctorId))
                .thenReturn(List.of(monday, tuesday));

        // When & Then
        mockMvc.perform(get("/availability/doctor/{doctorId}", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].doctorId").value(doctorId.toString()))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$[1].dayOfWeek").value("TUESDAY"));

        verify(availabilityService, times(1)).getDoctorAvailability(doctorId);
    }

    @Test
    void getDoctorAvailability_WhenNoAvailability_ShouldReturn200WithEmptyList() throws Exception {
        // Given
        when(availabilityService.getDoctorAvailability(doctorId))
                .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/availability/doctor/{doctorId}", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(availabilityService, times(1)).getDoctorAvailability(doctorId);
    }

    @Test
    void getDoctorAvailabilityByDay_WithValidData_ShouldReturn200() throws Exception {
        // Given
        AvailabilityResponseDto morning = new AvailabilityResponseDto(
                UUID.randomUUID(), doctorId, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0), true, OffsetDateTime.now()
        );
        AvailabilityResponseDto afternoon = new AvailabilityResponseDto(
                UUID.randomUUID(), doctorId, DayOfWeek.MONDAY,
                LocalTime.of(13, 0), LocalTime.of(17, 0), true, OffsetDateTime.now()
        );

        when(availabilityService.getDoctorAvailabilityByDay(doctorId, DayOfWeek.MONDAY))
                .thenReturn(List.of(morning, afternoon));

        // When & Then
        mockMvc.perform(get("/availability/doctor/{doctorId}/day/{dayOfWeek}", doctorId, "MONDAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$[0].startTime").value("09:00:00"))
                .andExpect(jsonPath("$[0].endTime").value("12:00:00"))
                .andExpect(jsonPath("$[1].startTime").value("13:00:00"))
                .andExpect(jsonPath("$[1].endTime").value("17:00:00"));

        verify(availabilityService, times(1)).getDoctorAvailabilityByDay(doctorId, DayOfWeek.MONDAY);
    }

    @Test
    void getDoctorAvailabilityByDay_WithInvalidDay_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/availability/doctor/{doctorId}/day/{dayOfWeek}", doctorId, "INVALIDDAY"))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).getDoctorAvailabilityByDay(any(), any());
    }

    @Test
    void getDoctorAvailabilityByDay_ForAllDays_ShouldWork() throws Exception {
        // Given
        String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};

        for (String day : days) {
            when(availabilityService.getDoctorAvailabilityByDay(eq(doctorId), eq(DayOfWeek.valueOf(day))))
                    .thenReturn(List.of(responseDto));

            // When & Then
            mockMvc.perform(get("/availability/doctor/{doctorId}/day/{dayOfWeek}", doctorId, day))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }

    @Test
    void getActiveAvailability_WithValidDoctorId_ShouldReturn200() throws Exception {
        // Given
        AvailabilityResponseDto active1 = new AvailabilityResponseDto(
                UUID.randomUUID(), doctorId, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(17, 0), true, OffsetDateTime.now()
        );
        AvailabilityResponseDto active2 = new AvailabilityResponseDto(
                UUID.randomUUID(), doctorId, DayOfWeek.TUESDAY,
                LocalTime.of(9, 0), LocalTime.of(17, 0), true, OffsetDateTime.now()
        );

        when(availabilityService.getActiveAvailability(doctorId))
                .thenReturn(List.of(active1, active2));

        // When & Then
        mockMvc.perform(get("/availability/doctor/{doctorId}/active", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].isAvailable").value(true))
                .andExpect(jsonPath("$[1].isAvailable").value(true));

        verify(availabilityService, times(1)).getActiveAvailability(doctorId);
    }

    @Test
    void getActiveAvailability_WhenNoActiveSlots_ShouldReturn200WithEmptyList() throws Exception {
        // Given
        when(availabilityService.getActiveAvailability(doctorId))
                .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/availability/doctor/{doctorId}/active", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(availabilityService, times(1)).getActiveAvailability(doctorId);
    }

    @Test
    void updateAvailability_WithValidData_ShouldReturn200() throws Exception {
        // Given
        AvailabilityUpdateDto updateDto = new AvailabilityUpdateDto(
                LocalTime.of(10, 0),
                LocalTime.of(18, 0),
                true
        );

        AvailabilityResponseDto updatedResponse = new AvailabilityResponseDto(
                availabilityId,
                doctorId,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(18, 0),
                true,
                OffsetDateTime.now()
        );

        when(availabilityService.updateAvailability(eq(availabilityId), any(AvailabilityUpdateDto.class)))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(patch("/availability/{availabilityId}", availabilityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(availabilityId.toString()))
                .andExpect(jsonPath("$.startTime").value("10:00:00"))
                .andExpect(jsonPath("$.endTime").value("18:00:00"))
                .andExpect(jsonPath("$.isAvailable").value(true));

        verify(availabilityService, times(1)).updateAvailability(eq(availabilityId), any(AvailabilityUpdateDto.class));
    }

    @Test
    void updateAvailability_WithPartialData_ShouldReturn200() throws Exception {
        // Given - Only updating isAvailable
        AvailabilityUpdateDto updateDto = new AvailabilityUpdateDto(
                null,
                null,
                false
        );

        AvailabilityResponseDto updatedResponse = new AvailabilityResponseDto(
                availabilityId,
                doctorId,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                false,
                OffsetDateTime.now()
        );

        when(availabilityService.updateAvailability(eq(availabilityId), any(AvailabilityUpdateDto.class)))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(patch("/availability/{availabilityId}", availabilityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(false));

        verify(availabilityService, times(1)).updateAvailability(eq(availabilityId), any(AvailabilityUpdateDto.class));
    }

    @Test
    void updateAvailability_WithMissingIsAvailable_ShouldReturn400() throws Exception {
        // Given - isAvailable is required
        String invalidRequest = """
                {
                    "startTime": "10:00:00",
                    "endTime": "18:00:00"
                }
                """;

        // When & Then
        mockMvc.perform(patch("/availability/{availabilityId}", availabilityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).updateAvailability(any(), any());
    }

    @Test
    void updateAvailability_WithInvalidUUID_ShouldReturn400() throws Exception {
        // Given
        AvailabilityUpdateDto updateDto = new AvailabilityUpdateDto(
                LocalTime.of(10, 0),
                LocalTime.of(18, 0),
                true
        );

        // When & Then
        mockMvc.perform(patch("/availability/{availabilityId}", "invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).updateAvailability(any(), any());
    }

    @Test
    void deleteAvailability_WithValidId_ShouldReturn204() throws Exception {
        // Given
        doNothing().when(availabilityService).deleteAvailability(availabilityId);

        // When & Then
        mockMvc.perform(delete("/availability/{availabilityId}", availabilityId))
                .andExpect(status().isNoContent());

        verify(availabilityService, times(1)).deleteAvailability(availabilityId);
    }

    @Test
    void deleteAvailability_WithInvalidUUID_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(delete("/availability/{availabilityId}", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).deleteAvailability(any());
    }

    @Test
    void deleteAvailability_WhenNotFound_ShouldReturn400() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Availability slot not found"))
                .when(availabilityService).deleteAvailability(availabilityId);

        // When & Then
        mockMvc.perform(delete("/availability/{availabilityId}", availabilityId))
                .andExpect(status().isBadRequest());

        verify(availabilityService, times(1)).deleteAvailability(availabilityId);
    }

    @Test
    void deleteAllDoctorAvailability_WithValidDoctorId_ShouldReturn204() throws Exception {
        // Given
        doNothing().when(availabilityService).deleteAllDoctorAvailability(doctorId);

        // When & Then
        mockMvc.perform(delete("/availability/doctor/{doctorId}", doctorId))
                .andExpect(status().isNoContent());

        verify(availabilityService, times(1)).deleteAllDoctorAvailability(doctorId);
    }

    @Test
    void deleteAllDoctorAvailability_WithInvalidUUID_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(delete("/availability/doctor/{doctorId}", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).deleteAllDoctorAvailability(any());
    }

    @Test
    void createAvailability_WithServiceException_ShouldReturn500() throws Exception {
        // Given
        when(availabilityService.createAvailability(any(AvailabilityRequestDto.class)))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());

        verify(availabilityService, times(1)).createAvailability(any(AvailabilityRequestDto.class));
    }

    @Test
    void createAvailability_WithMultipleSlotsForSameDay_ShouldSucceed() throws Exception {
        // Given - Morning slot
        AvailabilityRequestDto morningSlot = new AvailabilityRequestDto(
                doctorId,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );

        AvailabilityResponseDto morningResponse = new AvailabilityResponseDto(
                UUID.randomUUID(), doctorId, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0), true, OffsetDateTime.now()
        );

        when(availabilityService.createAvailability(any(AvailabilityRequestDto.class)))
                .thenReturn(morningResponse);

        // When & Then - Create morning slot
        mockMvc.perform(post("/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(morningSlot)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").value("09:00:00"))
                .andExpect(jsonPath("$.endTime").value("12:00:00"));

        // Given - Afternoon slot
        AvailabilityRequestDto afternoonSlot = new AvailabilityRequestDto(
                doctorId,
                DayOfWeek.MONDAY,
                LocalTime.of(13, 0),
                LocalTime.of(17, 0)
        );

        AvailabilityResponseDto afternoonResponse = new AvailabilityResponseDto(
                UUID.randomUUID(), doctorId, DayOfWeek.MONDAY,
                LocalTime.of(13, 0), LocalTime.of(17, 0), true, OffsetDateTime.now()
        );

        when(availabilityService.createAvailability(any(AvailabilityRequestDto.class)))
                .thenReturn(afternoonResponse);

        // When & Then - Create afternoon slot
        mockMvc.perform(post("/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(afternoonSlot)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").value("13:00:00"))
                .andExpect(jsonPath("$.endTime").value("17:00:00"));

        verify(availabilityService, times(2)).createAvailability(any(AvailabilityRequestDto.class));
    }

    @Test
    void getAllEndpoints_WithValidRequests_ShouldCallServiceOnce() throws Exception {
        // Given
        when(availabilityService.getDoctorAvailability(doctorId)).thenReturn(List.of());
        when(availabilityService.getDoctorAvailabilityByDay(doctorId, DayOfWeek.MONDAY)).thenReturn(List.of());
        when(availabilityService.getActiveAvailability(doctorId)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/availability/doctor/{doctorId}", doctorId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/availability/doctor/{doctorId}/day/{dayOfWeek}", doctorId, "MONDAY"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/availability/doctor/{doctorId}/active", doctorId))
                .andExpect(status().isOk());

        verify(availabilityService, times(1)).getDoctorAvailability(doctorId);
        verify(availabilityService, times(1)).getDoctorAvailabilityByDay(doctorId, DayOfWeek.MONDAY);
        verify(availabilityService, times(1)).getActiveAvailability(doctorId);
    }

    @Test
    void createBulkAvailability_WithMixedDays_ShouldReturn201() throws Exception {
        // Given
        BulkAvailabilityRequestDto request = new BulkAvailabilityRequestDto(
                doctorId,
                List.of(
                        new BulkAvailabilityRequestDto.AvailabilitySlot(
                                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)
                        ),
                        new BulkAvailabilityRequestDto.AvailabilitySlot(
                                DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)
                        ),
                        new BulkAvailabilityRequestDto.AvailabilitySlot(
                                DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)
                        )
                )
        );

        List<AvailabilityResponseDto> responses = List.of(
                new AvailabilityResponseDto(UUID.randomUUID(), doctorId, DayOfWeek.MONDAY,
                        LocalTime.of(9, 0), LocalTime.of(17, 0), true, OffsetDateTime.now()),
                new AvailabilityResponseDto(UUID.randomUUID(), doctorId, DayOfWeek.WEDNESDAY,
                        LocalTime.of(9, 0), LocalTime.of(17, 0), true, OffsetDateTime.now()),
                new AvailabilityResponseDto(UUID.randomUUID(), doctorId, DayOfWeek.FRIDAY,
                        LocalTime.of(9, 0), LocalTime.of(17, 0), true, OffsetDateTime.now())
        );

        when(availabilityService.createBulkAvailability(any(BulkAvailabilityRequestDto.class)))
                .thenReturn(responses);

        // When & Then
        mockMvc.perform(post("/availability/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$[1].dayOfWeek").value("WEDNESDAY"))
                .andExpect(jsonPath("$[2].dayOfWeek").value("FRIDAY"));

        verify(availabilityService, times(1)).createBulkAvailability(any(BulkAvailabilityRequestDto.class));
    }

    @Test
    void updateAvailability_ToDisable_ShouldReturn200() throws Exception {
        // Given
        AvailabilityUpdateDto updateDto = new AvailabilityUpdateDto(
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                false // Disable the slot
        );

        AvailabilityResponseDto disabledResponse = new AvailabilityResponseDto(
                availabilityId,
                doctorId,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                false,
                OffsetDateTime.now()
        );

        when(availabilityService.updateAvailability(eq(availabilityId), any(AvailabilityUpdateDto.class)))
                .thenReturn(disabledResponse);

        // When & Then
        mockMvc.perform(patch("/availability/{availabilityId}", availabilityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(false));

        verify(availabilityService, times(1)).updateAvailability(eq(availabilityId), any(AvailabilityUpdateDto.class));
    }

    @Test
    void getDoctorAvailability_WithMultipleDoctors_ShouldReturnCorrectData() throws Exception {
        // Given
        UUID doctor1Id = UUID.randomUUID();
        UUID doctor2Id = UUID.randomUUID();

        AvailabilityResponseDto doctor1Availability = new AvailabilityResponseDto(
                UUID.randomUUID(), doctor1Id, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(17, 0), true, OffsetDateTime.now()
        );

        AvailabilityResponseDto doctor2Availability = new AvailabilityResponseDto(
                UUID.randomUUID(), doctor2Id, DayOfWeek.TUESDAY,
                LocalTime.of(10, 0), LocalTime.of(18, 0), true, OffsetDateTime.now()
        );

        when(availabilityService.getDoctorAvailability(doctor1Id))
                .thenReturn(List.of(doctor1Availability));
        when(availabilityService.getDoctorAvailability(doctor2Id))
                .thenReturn(List.of(doctor2Availability));

        // When & Then - Doctor 1
        mockMvc.perform(get("/availability/doctor/{doctorId}", doctor1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].doctorId").value(doctor1Id.toString()))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"));

        // When & Then - Doctor 2
        mockMvc.perform(get("/availability/doctor/{doctorId}", doctor2Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].doctorId").value(doctor2Id.toString()))
                .andExpect(jsonPath("$[0].dayOfWeek").value("TUESDAY"));

        verify(availabilityService, times(1)).getDoctorAvailability(doctor1Id);
        verify(availabilityService, times(1)).getDoctorAvailability(doctor2Id);
    }

    @Test
    void createAvailability_WithEdgeTimeCases_ShouldSucceed() throws Exception {
        // Given - Midnight to noon
        AvailabilityRequestDto midnightRequest = new AvailabilityRequestDto(
                doctorId,
                DayOfWeek.MONDAY,
                LocalTime.of(0, 0),
                LocalTime.of(12, 0)
        );

        AvailabilityResponseDto midnightResponse = new AvailabilityResponseDto(
                UUID.randomUUID(), doctorId, DayOfWeek.MONDAY,
                LocalTime.of(0, 0), LocalTime.of(12, 0), true, OffsetDateTime.now()
        );

        when(availabilityService.createAvailability(any(AvailabilityRequestDto.class)))
                .thenReturn(midnightResponse);

        mockMvc.perform(post("/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(midnightRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").value("00:00:00"))
                .andExpect(jsonPath("$.endTime").value("12:00:00"));

        // Given - Late night
        AvailabilityRequestDto lateNightRequest = new AvailabilityRequestDto(
                doctorId,
                DayOfWeek.MONDAY,
                LocalTime.of(20, 0),
                LocalTime.of(23, 59)
        );

        AvailabilityResponseDto lateNightResponse = new AvailabilityResponseDto(
                UUID.randomUUID(), doctorId, DayOfWeek.MONDAY,
                LocalTime.of(20, 0), LocalTime.of(23, 59), true, OffsetDateTime.now()
        );

        when(availabilityService.createAvailability(any(AvailabilityRequestDto.class)))
                .thenReturn(lateNightResponse);

        mockMvc.perform(post("/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lateNightRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").value("20:00:00"))
                .andExpect(jsonPath("$.endTime").value("23:59:00"));
    }

    @Test
    void createBulkAvailability_With24x7Schedule_ShouldReturn201() throws Exception {
        // Given - 24/7 coverage
        List<BulkAvailabilityRequestDto.AvailabilitySlot> allDaySlots = List.of(
                new BulkAvailabilityRequestDto.AvailabilitySlot(DayOfWeek.MONDAY, LocalTime.of(0, 0), LocalTime.of(23, 59)),
                new BulkAvailabilityRequestDto.AvailabilitySlot(DayOfWeek.TUESDAY, LocalTime.of(0, 0), LocalTime.of(23, 59)),
                new BulkAvailabilityRequestDto.AvailabilitySlot(DayOfWeek.WEDNESDAY, LocalTime.of(0, 0), LocalTime.of(23, 59)),
                new BulkAvailabilityRequestDto.AvailabilitySlot(DayOfWeek.THURSDAY, LocalTime.of(0, 0), LocalTime.of(23, 59)),
                new BulkAvailabilityRequestDto.AvailabilitySlot(DayOfWeek.FRIDAY, LocalTime.of(0, 0), LocalTime.of(23, 59)),
                new BulkAvailabilityRequestDto.AvailabilitySlot(DayOfWeek.SATURDAY, LocalTime.of(0, 0), LocalTime.of(23, 59)),
                new BulkAvailabilityRequestDto.AvailabilitySlot(DayOfWeek.SUNDAY, LocalTime.of(0, 0), LocalTime.of(23, 59))
        );

        BulkAvailabilityRequestDto request = new BulkAvailabilityRequestDto(doctorId, allDaySlots);

        List<AvailabilityResponseDto> responses = allDaySlots.stream()
                .map(slot -> new AvailabilityResponseDto(
                        UUID.randomUUID(), doctorId, slot.dayOfWeek(),
                        slot.startTime(), slot.endTime(), true, OffsetDateTime.now()
                ))
                .toList();

        when(availabilityService.createBulkAvailability(any(BulkAvailabilityRequestDto.class)))
                .thenReturn(responses);

        // When & Then
        mockMvc.perform(post("/availability/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[*].startTime", everyItem(is("00:00:00"))))
                .andExpect(jsonPath("$[*].endTime", everyItem(is("23:59:00"))));

        verify(availabilityService, times(1)).createBulkAvailability(any(BulkAvailabilityRequestDto.class));
    }

    @Test
    void getDoctorAvailabilityByDay_ForWeekend_ShouldWork() throws Exception {
        // Given - Saturday
        AvailabilityResponseDto saturdayResponse = new AvailabilityResponseDto(
                UUID.randomUUID(), doctorId, DayOfWeek.SATURDAY,
                LocalTime.of(10, 0), LocalTime.of(14, 0), true, OffsetDateTime.now()
        );

        when(availabilityService.getDoctorAvailabilityByDay(doctorId, DayOfWeek.SATURDAY))
                .thenReturn(List.of(saturdayResponse));

        mockMvc.perform(get("/availability/doctor/{doctorId}/day/{dayOfWeek}", doctorId, "SATURDAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dayOfWeek").value("SATURDAY"))
                .andExpect(jsonPath("$[0].startTime").value("10:00:00"));

        // Given - Sunday
        AvailabilityResponseDto sundayResponse = new AvailabilityResponseDto(
                UUID.randomUUID(), doctorId, DayOfWeek.SUNDAY,
                LocalTime.of(10, 0), LocalTime.of(14, 0), true, OffsetDateTime.now()
        );

        when(availabilityService.getDoctorAvailabilityByDay(doctorId, DayOfWeek.SUNDAY))
                .thenReturn(List.of(sundayResponse));

        mockMvc.perform(get("/availability/doctor/{doctorId}/day/{dayOfWeek}", doctorId, "SUNDAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dayOfWeek").value("SUNDAY"));
    }

    @Test
    void updateAvailability_EnableDisableMultipleTimes_ShouldWork() throws Exception {
        // Given
        AvailabilityUpdateDto disableDto = new AvailabilityUpdateDto(null, null, false);
        AvailabilityUpdateDto enableDto = new AvailabilityUpdateDto(null, null, true);

        AvailabilityResponseDto disabledResponse = new AvailabilityResponseDto(
                availabilityId, doctorId, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(17, 0), false, OffsetDateTime.now()
        );

        AvailabilityResponseDto enabledResponse = new AvailabilityResponseDto(
                availabilityId, doctorId, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(17, 0), true, OffsetDateTime.now()
        );

        // When & Then - Disable
        when(availabilityService.updateAvailability(eq(availabilityId), any(AvailabilityUpdateDto.class)))
                .thenReturn(disabledResponse);

        mockMvc.perform(patch("/availability/{availabilityId}", availabilityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(disableDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(false));

        // When & Then - Enable
        when(availabilityService.updateAvailability(eq(availabilityId), any(AvailabilityUpdateDto.class)))
                .thenReturn(enabledResponse);

        mockMvc.perform(patch("/availability/{availabilityId}", availabilityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enableDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(true));

        verify(availabilityService, times(2)).updateAvailability(eq(availabilityId), any(AvailabilityUpdateDto.class));
    }

    @Test
    void createBulkAvailability_WithSingleSlot_ShouldReturn201() throws Exception {
        // Given
        BulkAvailabilityRequestDto request = new BulkAvailabilityRequestDto(
                doctorId,
                List.of(
                        new BulkAvailabilityRequestDto.AvailabilitySlot(
                                DayOfWeek.MONDAY,
                                LocalTime.of(9, 0),
                                LocalTime.of(17, 0)
                        )
                )
        );

        AvailabilityResponseDto response = new AvailabilityResponseDto(
                UUID.randomUUID(), doctorId, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(17, 0), true, OffsetDateTime.now()
        );

        when(availabilityService.createBulkAvailability(any(BulkAvailabilityRequestDto.class)))
                .thenReturn(List.of(response));

        // When & Then
        mockMvc.perform(post("/availability/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"));

        verify(availabilityService, times(1)).createBulkAvailability(any(BulkAvailabilityRequestDto.class));
    }

    @Test
    void getActiveAvailability_WithMixedActiveAndInactiveSlots_ShouldReturnOnlyActive() throws Exception {
        // Given - Service should only return active slots
        AvailabilityResponseDto activeSlot1 = new AvailabilityResponseDto(
                UUID.randomUUID(), doctorId, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(17, 0), true, OffsetDateTime.now()
        );

        AvailabilityResponseDto activeSlot2 = new AvailabilityResponseDto(
                UUID.randomUUID(), doctorId, DayOfWeek.TUESDAY,
                LocalTime.of(9, 0), LocalTime.of(17, 0), true, OffsetDateTime.now()
        );

        when(availabilityService.getActiveAvailability(doctorId))
                .thenReturn(List.of(activeSlot1, activeSlot2));

        // When & Then
        mockMvc.perform(get("/availability/doctor/{doctorId}/active", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].isAvailable", everyItem(is(true))));

        verify(availabilityService, times(1)).getActiveAvailability(doctorId);
    }

    @Test
    void createAvailability_WithEmptyBody_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(post("/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).createAvailability(any());
    }

    @Test
    void updateAvailability_WithEmptyBody_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(patch("/availability/{availabilityId}", availabilityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).updateAvailability(any(), any());
    }

    @Test
    void createAvailability_WithInvalidTimeFormat_ShouldReturn400() throws Exception {
        // Given
        String invalidRequest = String.format("""
                {
                    "doctorId": "%s",
                    "dayOfWeek": "MONDAY",
                    "startTime": "25:00:00",
                    "endTime": "17:00:00"
                }
                """, doctorId);

        // When & Then
        mockMvc.perform(post("/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).createAvailability(any());
    }

    @Test
    void getAllOperations_WithSameDoctorId_ShouldWorkIndependently() throws Exception {
        // Given
        when(availabilityService.createAvailability(any(AvailabilityRequestDto.class)))
                .thenReturn(responseDto);
        when(availabilityService.getDoctorAvailability(doctorId))
                .thenReturn(List.of(responseDto));
        when(availabilityService.updateAvailability(any(), any()))
                .thenReturn(responseDto);
        doNothing().when(availabilityService).deleteAvailability(any());

        // When & Then - Create
        mockMvc.perform(post("/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        // When & Then - Get
        mockMvc.perform(get("/availability/doctor/{doctorId}", doctorId))
                .andExpect(status().isOk());

        // When & Then - Update
        AvailabilityUpdateDto updateDto = new AvailabilityUpdateDto(
                LocalTime.of(10, 0), LocalTime.of(18, 0), true
        );
        mockMvc.perform(patch("/availability/{availabilityId}", availabilityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        // When & Then - Delete
        mockMvc.perform(delete("/availability/{availabilityId}", availabilityId))
                .andExpect(status().isNoContent());

        // Verify all called
        verify(availabilityService, times(1)).createAvailability(any());
        verify(availabilityService, times(1)).getDoctorAvailability(doctorId);
        verify(availabilityService, times(1)).updateAvailability(any(), any());
        verify(availabilityService, times(1)).deleteAvailability(availabilityId);
    }
}