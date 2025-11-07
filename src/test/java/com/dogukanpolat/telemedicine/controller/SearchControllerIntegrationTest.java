// SearchControllerIntegrationTest.java
package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.appointment.AppointmentResponseDto;
import com.dogukanpolat.telemedicine.dto.search.*;
import com.dogukanpolat.telemedicine.model.enums.AppointmentStatus;
import com.dogukanpolat.telemedicine.security.JwtAuthenticationFilter;
import com.dogukanpolat.telemedicine.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SearchController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        ))
@AutoConfigureMockMvc(addFilters = false)
class SearchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SearchService searchService;

    private final UUID SAMPLE_DOCTOR_ID = UUID.randomUUID();
    private final UUID SAMPLE_PATIENT_ID = UUID.randomUUID();
    private final LocalDate SAMPLE_DATE = LocalDate.of(2024, 1, 15);
    private final LocalTime SAMPLE_TIME = LocalTime.of(14, 30);

    @Test
    void searchDoctors_WithAllParameters_ShouldReturnDoctors() throws Exception {
        // Arrange
        DoctorSearchResponseDto doctorResponse = new DoctorSearchResponseDto(
                SAMPLE_DOCTOR_ID,
                "John",
                "Doe",
                "john.doe@example.com",
                "+1234567890",
                "Cardiology",
                10,
                new BigDecimal("150.00"),
                true
        );

        when(searchService.searchDoctors(any(DoctorSearchCriteria.class)))
                .thenReturn(List.of(doctorResponse));

        // Act & Assert
        mockMvc.perform(get("/search/doctors")
                        .with(csrf())
                        .param("specialization", "Cardiology")
                        .param("minFee", "100")
                        .param("maxFee", "200")
                        .param("minExperience", "5")
                        .param("isVerified", "true")
                        .param("availableDay", "MONDAY")
                        .param("availableStartTime", "09:00")
                        .param("availableEndTime", "17:00")
                        .param("name", "John")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(SAMPLE_DOCTOR_ID.toString())))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[0].specialization", is("Cardiology")))
                .andExpect(jsonPath("$[0].consultationFee", is(150.00)))
                .andExpect(jsonPath("$[0].isVerified", is(true)));
    }

    @Test
    void searchDoctors_WithPartialParameters_ShouldReturnDoctors() throws Exception {
        // Arrange
        DoctorSearchResponseDto doctorResponse = new DoctorSearchResponseDto(
                SAMPLE_DOCTOR_ID,
                "Jane",
                "Smith",
                "jane.smith@example.com",
                "+0987654321",
                "Dermatology",
                8,
                new BigDecimal("120.00"),
                true
        );

        when(searchService.searchDoctors(any(DoctorSearchCriteria.class)))
                .thenReturn(List.of(doctorResponse));

        // Act & Assert
        mockMvc.perform(get("/search/doctors")
                        .with(csrf())
                        .param("specialization", "Dermatology")
                        .param("minExperience", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName", is("Jane")))
                .andExpect(jsonPath("$[0].specialization", is("Dermatology")));
    }

    @Test
    void searchDoctors_WithNoParameters_ShouldReturnAllDoctors() throws Exception {
        // Arrange
        DoctorSearchResponseDto doctor1 = new DoctorSearchResponseDto(
                SAMPLE_DOCTOR_ID,
                "John",
                "Doe",
                "john.doe@example.com",
                "+1234567890",
                "Cardiology",
                10,
                new BigDecimal("150.00"),
                true
        );

        DoctorSearchResponseDto doctor2 = new DoctorSearchResponseDto(
                UUID.randomUUID(),
                "Jane",
                "Smith",
                "jane.smith@example.com",
                "+0987654321",
                "Dermatology",
                8,
                new BigDecimal("120.00"),
                true
        );

        when(searchService.searchDoctors(any(DoctorSearchCriteria.class)))
                .thenReturn(List.of(doctor1, doctor2));

        // Act & Assert
        mockMvc.perform(get("/search/doctors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void searchPatients_WithAllParameters_ShouldReturnPatients() throws Exception {
        // Arrange
        PatientSearchResponseDto patientResponse = new PatientSearchResponseDto(
                SAMPLE_PATIENT_ID,
                "Alice",
                "Johnson",
                "alice.johnson@example.com",
                "+1111111111",
                "A_POSITIVE",
                List.of("Peanuts", "Pollen"),
                "Bob Johnson",
                "+1222222222",
                true,
                LocalDate.of(2023, 1, 1)
        );

        when(searchService.searchPatients(any(PatientSearchCriteria.class)))
                .thenReturn(List.of(patientResponse));

        // Act & Assert
        mockMvc.perform(get("/search/patients")
                        .with(csrf())
                        .param("name", "Alice")
                        .param("email", "alice")
                        .param("bloodType", "A_POSITIVE")
                        .param("isActive", "true")
                        .param("phoneNumber", "1111111111")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(SAMPLE_PATIENT_ID.toString())))
                .andExpect(jsonPath("$[0].firstName", is("Alice")))
                .andExpect(jsonPath("$[0].email", is("alice.johnson@example.com")))
                .andExpect(jsonPath("$[0].bloodType", is("A_POSITIVE")))
                .andExpect(jsonPath("$[0].isActive", is(true)));
    }

    @Test
    void searchPatients_WithPartialParameters_ShouldReturnPatients() throws Exception {
        // Arrange
        PatientSearchResponseDto patientResponse = new PatientSearchResponseDto(
                SAMPLE_PATIENT_ID,
                "Bob",
                "Brown",
                "bob.brown@example.com",
                "+1333333333",
                "O_POSITIVE",
                List.of(),
                "Alice Brown",
                "+1444444444",
                true,
                LocalDate.of(2023, 2, 1)
        );

        when(searchService.searchPatients(any(PatientSearchCriteria.class)))
                .thenReturn(List.of(patientResponse));

        // Act & Assert
        mockMvc.perform(get("/search/patients")
                        .with(csrf())
                        .param("name", "Bob")
                        .param("isActive", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName", is("Bob")))
                .andExpect(jsonPath("$[0].isActive", is(true)));
    }

    @Test
    void searchPatients_WithNoParameters_ShouldReturnAllPatients() throws Exception {
        // Arrange
        PatientSearchResponseDto patient1 = new PatientSearchResponseDto(
                SAMPLE_PATIENT_ID,
                "Alice",
                "Johnson",
                "alice.johnson@example.com",
                "+1111111111",
                "A_POSITIVE",
                List.of("Peanuts"),
                "Bob Johnson",
                "+1222222222",
                true,
                LocalDate.of(2023, 1, 1)
        );

        PatientSearchResponseDto patient2 = new PatientSearchResponseDto(
                UUID.randomUUID(),
                "Bob",
                "Brown",
                "bob.brown@example.com",
                "+1333333333",
                "O_POSITIVE",
                List.of(),
                "Alice Brown",
                "+1444444444",
                false,
                LocalDate.of(2023, 2, 1)
        );

        when(searchService.searchPatients(any(PatientSearchCriteria.class)))
                .thenReturn(List.of(patient1, patient2));

        // Act & Assert
        mockMvc.perform(get("/search/patients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void filterAppointments_WithAllParameters_ShouldReturnAppointments() throws Exception {
        // Arrange
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        AppointmentResponseDto appointmentResponse = new AppointmentResponseDto(
                "Alice",
                "Johnson",
                "John",
                "Doe",
                SAMPLE_DATE,
                SAMPLE_TIME,
                30,
                AppointmentStatus.SCHEDULED
        );

        when(searchService.filterAppointments(any(AppointmentFilterCriteria.class)))
                .thenReturn(List.of(appointmentResponse));

        // Act & Assert
        mockMvc.perform(get("/search/appointments")
                        .with(csrf())
                        .param("patientId", patientId.toString())
                        .param("doctorId", doctorId.toString())
                        .param("status", "SCHEDULED")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].patientFirstName", is("Alice")))
                .andExpect(jsonPath("$[0].doctorFirstName", is("John")))
                .andExpect(jsonPath("$[0].status", is("SCHEDULED")))
                .andExpect(jsonPath("$[0].scheduledDate", is("2024-01-15")))
                .andExpect(jsonPath("$[0].scheduledTime", is("14:30:00")));
    }

    @Test
    void filterAppointments_WithDateRangeOnly_ShouldReturnAppointments() throws Exception {
        // Arrange
        AppointmentResponseDto appointmentResponse = new AppointmentResponseDto(
                "Bob",
                "Brown",
                "Jane",
                "Smith",
                SAMPLE_DATE,
                SAMPLE_TIME,
                45,
                AppointmentStatus.CONFIRMED
        );

        when(searchService.filterAppointments(any(AppointmentFilterCriteria.class)))
                .thenReturn(List.of(appointmentResponse));

        // Act & Assert
        mockMvc.perform(get("/search/appointments")
                        .with(csrf())
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].patientFirstName", is("Bob")))
                .andExpect(jsonPath("$[0].status", is("CONFIRMED")));
    }

    @Test
    void filterAppointments_WithStatusOnly_ShouldReturnAppointments() throws Exception {
        // Arrange
        AppointmentResponseDto appointment1 = new AppointmentResponseDto(
                "Alice",
                "Johnson",
                "John",
                "Doe",
                SAMPLE_DATE,
                SAMPLE_TIME,
                30,
                AppointmentStatus.COMPLETED
        );

        AppointmentResponseDto appointment2 = new AppointmentResponseDto(
                "Bob",
                "Brown",
                "John",
                "Doe",
                SAMPLE_DATE.plusDays(1),
                SAMPLE_TIME.plusHours(1),
                30,
                AppointmentStatus.COMPLETED
        );

        when(searchService.filterAppointments(any(AppointmentFilterCriteria.class)))
                .thenReturn(List.of(appointment1, appointment2));

        // Act & Assert
        mockMvc.perform(get("/search/appointments")
                        .with(csrf())
                        .param("status", "COMPLETED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status", is("COMPLETED")))
                .andExpect(jsonPath("$[1].status", is("COMPLETED")));
    }

    @Test
    void filterAppointments_WithNoParameters_ShouldReturnAllAppointments() throws Exception {
        // Arrange
        AppointmentResponseDto appointment1 = new AppointmentResponseDto(
                "Alice",
                "Johnson",
                "John",
                "Doe",
                SAMPLE_DATE,
                SAMPLE_TIME,
                30,
                AppointmentStatus.SCHEDULED
        );

        AppointmentResponseDto appointment2 = new AppointmentResponseDto(
                "Bob",
                "Brown",
                "Jane",
                "Smith",
                SAMPLE_DATE.plusDays(1),
                SAMPLE_TIME.plusHours(2),
                45,
                AppointmentStatus.CONFIRMED
        );

        when(searchService.filterAppointments(any(AppointmentFilterCriteria.class)))
                .thenReturn(List.of(appointment1, appointment2));

        // Act & Assert
        mockMvc.perform(get("/search/appointments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void searchDoctors_WithInvalidDayOfWeek_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/search/doctors")
                        .with(csrf())
                        .param("availableDay", "INVALID_DAY")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filterAppointments_WithInvalidDate_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/search/appointments")
                        .with(csrf())
                        .param("startDate", "invalid-date")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}