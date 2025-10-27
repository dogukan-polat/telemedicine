package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.appointment.AppointmentRequestDto;
import com.dogukanpolat.telemedicine.dto.appointment.AppointmentResponseDto;
import com.dogukanpolat.telemedicine.model.Doctor;
import com.dogukanpolat.telemedicine.model.Patient;
import com.dogukanpolat.telemedicine.model.enums.AppointmentStatus;
import com.dogukanpolat.telemedicine.security.JwtAuthenticationFilter;
import com.dogukanpolat.telemedicine.service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AppointmentController.class,
excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
))
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentService appointmentService;

    private UUID patientId;
    private UUID doctorId;
    private AppointmentResponseDto testAppointmentResponse;

    @BeforeEach
    void setUp() {

        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();

        Patient patient = new Patient();
        patient.setId(patientId);

        Doctor doctor = new Doctor();
        doctor.setId(doctorId);

        testAppointmentResponse = new AppointmentResponseDto(
                "John",
                "Doe",
                "Jane",
                "Smith",
                LocalDate.now().plusDays(1),
                LocalTime.of(10,10),
                30,
                AppointmentStatus.SCHEDULED
                );
    }

    @Test
    void getAppointmentsByPatientId_ShouldReturn200() throws Exception {
        // Given
        when(appointmentService.getAppointmentsByPatientId(patientId))
                .thenReturn(List.of(testAppointmentResponse));

        // When & Then
        mockMvc.perform(get("/appointments/patient/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].patientFirstName").value("John"))
                .andExpect(jsonPath("$[0].patientLastName").value("Doe"))
                .andExpect(jsonPath("$[0].doctorFirstName").value("Jane"))
                .andExpect(jsonPath("$[0].doctorLastName").value("Smith"))
                .andExpect(jsonPath("$[0].durationInMinutes").value(30));

        verify(appointmentService).getAppointmentsByPatientId(patientId);
    }

    @Test
    void getAppointmentsByDoctorId_ShouldReturn200() throws Exception {
        // Given
        when(appointmentService.getAppointmentsByDoctorId(doctorId))
                .thenReturn(List.of(testAppointmentResponse));

        // When & Then
        mockMvc.perform(get("/appointments/doctor/{id}", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].patientFirstName").value("John"))
                .andExpect(jsonPath("$[0].patientLastName").value("Doe"))
                .andExpect(jsonPath("$[0].doctorFirstName").value("Jane"))
                .andExpect(jsonPath("$[0].doctorLastName").value("Smith"));

        verify(appointmentService).getAppointmentsByDoctorId(doctorId);
    }

    @Test
    void createAppointment_ShouldReturn200() throws Exception {
        // Given
        AppointmentRequestDto requestDto = new AppointmentRequestDto(
                patientId,
                doctorId,
                LocalDate.now().plusDays(1),
                LocalTime.of(10,10),
                30
        );

        when(appointmentService.createAppointment(any(AppointmentRequestDto.class)))
                .thenReturn(testAppointmentResponse);

        // When & Then
        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientFirstName").value("John"))
                .andExpect(jsonPath("$.patientLastName").value("Doe"))
                .andExpect(jsonPath("$.doctorFirstName").value("Jane"))
                .andExpect(jsonPath("$.doctorLastName").value("Smith"))
                .andExpect(jsonPath("$.durationInMinutes").value(30));

        verify(appointmentService).createAppointment(any(AppointmentRequestDto.class));
    }

    @Test
    void cancelAppointment_ShouldReturn200() throws Exception {
        // Given
        UUID appointmentId = UUID.randomUUID();
        when(appointmentService.changeAppointmentStatus(appointmentId, AppointmentStatus.CANCELLED))
                .thenReturn(testAppointmentResponse);

        // When & Then
        mockMvc.perform(patch("/appointments/{id}/cancel", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientFirstName").value("John"))
                .andExpect(jsonPath("$.patientLastName").value("Doe"));

        verify(appointmentService).changeAppointmentStatus(appointmentId, AppointmentStatus.CANCELLED);
    }

    @Test
    void confirmAppointment_ShouldReturn200() throws Exception {
        // Given
        UUID appointmentId = UUID.randomUUID();
        when(appointmentService.changeAppointmentStatus(appointmentId, AppointmentStatus.CONFIRMED))
                .thenReturn(testAppointmentResponse);

        // When & Then
        mockMvc.perform(patch("/appointments/{id}/confirm", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientFirstName").value("John"))
                .andExpect(jsonPath("$.patientLastName").value("Doe"));

        verify(appointmentService).changeAppointmentStatus(appointmentId, AppointmentStatus.CONFIRMED);
    }

    @Test
    void completeAppointment_ShouldReturn200() throws Exception {
        // Given
        UUID appointmentId = UUID.randomUUID();
        when(appointmentService.changeAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED))
                .thenReturn(testAppointmentResponse);

        // When & Then
        mockMvc.perform(patch("/appointments/{id}/complete", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientFirstName").value("John"))
                .andExpect(jsonPath("$.patientLastName").value("Doe"));

        verify(appointmentService).changeAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED);
    }

    @Test
    void deleteAppointment_ShouldReturn204() throws Exception {
        // Given
        UUID appointmentId = UUID.randomUUID();
        doNothing().when(appointmentService).deleteAppointment(appointmentId);

        // When & Then
        mockMvc.perform(delete("/appointments/{id}", appointmentId))
                .andExpect(status().isNoContent());

        verify(appointmentService).deleteAppointment(appointmentId);
    }

    @Test
    void getAppointmentsByPatientId_EmptyList_ShouldReturn200() throws Exception {
        // Given
        when(appointmentService.getAppointmentsByPatientId(patientId))
                .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/appointments/patient/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createAppointment_InvalidData_ShouldReturn400() throws Exception {
        // Given - missing required fields
        String invalidRequest = "{}";

        // When & Then
        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).createAppointment(any());
    }
}
