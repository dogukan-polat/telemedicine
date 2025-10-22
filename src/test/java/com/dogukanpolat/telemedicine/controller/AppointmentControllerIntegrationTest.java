package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.appointment.AppointmentRequestDto;
import com.dogukanpolat.telemedicine.model.Appointment;
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
    private UUID appointmentId;
    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();

        Patient patient = new Patient();
        patient.setId(patientId);

        Doctor doctor = new Doctor();
        doctor.setId(doctorId);

        testAppointment = new Appointment();
        testAppointment.setId(appointmentId);
        testAppointment.setPatient(patient);
        testAppointment.setDoctor(doctor);
        testAppointment.setScheduledDate(LocalDate.now().plusDays(1));
        testAppointment.setScheduledTime(LocalTime.of(10, 0));
        testAppointment.setDurationMinutes(30);
        testAppointment.setStatus(AppointmentStatus.SCHEDULED);
    }

    @Test
    void getAppointmentsByPatientId_ShouldReturn200() throws Exception {
        // Given
        when(appointmentService.getAppointmentsByPatientId(patientId))
                .thenReturn(List.of(testAppointment));

        // When & Then
        mockMvc.perform(get("/appointments/patient/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(appointmentId.toString()))
                .andExpect(jsonPath("$[0].durationMinutes").value(30));

        verify(appointmentService).getAppointmentsByPatientId(patientId);
    }

    @Test
    void getAppointmentsByDoctorId_ShouldReturn200() throws Exception {
        // Given
        when(appointmentService.getAppointmentsByDoctorId(doctorId))
                .thenReturn(List.of(testAppointment));

        // When & Then
        mockMvc.perform(get("/appointments/doctor/{id}", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(appointmentId.toString()));

        verify(appointmentService).getAppointmentsByDoctorId(doctorId);
    }

    @Test
    void createAppointment_ShouldReturn200() throws Exception {
        // Given
        Patient patient = new Patient();
        patient.setId(patientId);

        Doctor doctor = new Doctor();
        doctor.setId(doctorId);

        AppointmentRequestDto requestDto = new AppointmentRequestDto(
                patientId,
                doctorId,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                30
        );

        when(appointmentService.createAppointment(any(AppointmentRequestDto.class)))
                .thenReturn(testAppointment);

        // When & Then
        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentId.toString()))
                .andExpect(jsonPath("$.durationMinutes").value(30));

        verify(appointmentService).createAppointment(any(AppointmentRequestDto.class));
    }

    @Test
    void cancelAppointment_ShouldReturn200() throws Exception {
        // Given
        testAppointment.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentService.changeAppointmentStatus(appointmentId, AppointmentStatus.CANCELLED))
                .thenReturn(testAppointment);

        // When & Then
        mockMvc.perform(patch("/appointments/{id}/cancel", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(appointmentService).changeAppointmentStatus(appointmentId, AppointmentStatus.CANCELLED);
    }

    @Test
    void confirmAppointment_ShouldReturn200() throws Exception {
        // Given
        testAppointment.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentService.changeAppointmentStatus(appointmentId, AppointmentStatus.CONFIRMED))
                .thenReturn(testAppointment);

        // When & Then
        mockMvc.perform(patch("/appointments/{id}/confirm", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(appointmentService).changeAppointmentStatus(appointmentId, AppointmentStatus.CONFIRMED);
    }

    @Test
    void completeAppointment_ShouldReturn200() throws Exception {
        // Given
        testAppointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentService.changeAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED))
                .thenReturn(testAppointment);

        // When & Then
        mockMvc.perform(patch("/appointments/{id}/complete", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(appointmentService).changeAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED);
    }

    @Test
    void deleteAppointment_ShouldReturn204() throws Exception {
        // Given
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
