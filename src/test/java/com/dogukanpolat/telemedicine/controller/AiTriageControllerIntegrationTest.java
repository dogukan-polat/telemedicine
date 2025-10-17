package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.model.TriageResult;
import com.dogukanpolat.telemedicine.model.enums.UrgencyLevel;
import com.dogukanpolat.telemedicine.security.JwtAuthenticationFilter;
import com.dogukanpolat.telemedicine.service.AITriageService;
import com.dogukanpolat.telemedicine.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AiTriageController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        ))
@AutoConfigureMockMvc(addFilters = false)
class AiTriageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AITriageService aiTriageService;

    @MockitoBean
    private AppointmentService appointmentService;



    @Test
    void analyzeSymptoms_ShouldReturn200_WithValidRequest() throws Exception {
        // Given
        UUID patientId = UUID.randomUUID();
        TriageResult mockResult = TriageResult.builder()
                .patientId(patientId)
                .urgency(UrgencyLevel.MEDIUM)
                .recommendedSpecialty("General Practice")
                .keyQuestions(List.of("When did symptoms start?"))
                .triageNotes("Routine checkup recommended")
                .build();

        when(aiTriageService.analyzeSymptoms(any(String.class), eq(patientId)))
                .thenReturn(mockResult);

        String requestBody = """
                {
                    "patientId": "%s",
                    "symptomDescription": "Mild headache for 2 days"
                }
                """.formatted(patientId);

        // When & Then
        mockMvc.perform(post("/ai/triage/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.urgency").value("MEDIUM"))
                .andExpect(jsonPath("$.recommendedSpecialty").value("General Practice"))
                .andExpect(jsonPath("$.triageNotes").value("Routine checkup recommended"));
    }

    @Test
    void analyzeSymptoms_ShouldReturn400_WhenPatientIdMissing() throws Exception {
        // Given
        String requestBody = """
                {
                    "symptomDescription": "Mild headache"
                }
                """;

        // When & Then
        mockMvc.perform(post("/ai/triage/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void analyzeSymptoms_ShouldReturn400_WhenSymptomDescriptionBlank() throws Exception {
        // Given
        UUID patientId = UUID.randomUUID();
        String requestBody = """
                {
                    "patientId": "%s",
                    "symptomDescription": ""
                }
                """.formatted(patientId);

        // When & Then
        mockMvc.perform(post("/ai/triage/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void quickAnalyze_ShouldReturn200_WithValidRequest() throws Exception {
        // Given
        TriageResult mockResult = TriageResult.builder()
                .urgency(UrgencyLevel.LOW)
                .recommendedSpecialty("General Practice")
                .keyQuestions(List.of("Any allergies?"))
                .triageNotes("Minor symptoms")
                .build();

        when(aiTriageService.analyzeSymptoms(any(String.class), eq(null)))
                .thenReturn(mockResult);

        String requestBody = """
                {
                    "symptomDescription": "Runny nose and sneezing"
                }
                """;

        // When & Then
        mockMvc.perform(post("/ai/triage/quick-analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urgency").value("LOW"))
                .andExpect(jsonPath("$.recommendedSpecialty").value("General Practice"));
    }

    @Test
    void quickAnalyze_ShouldReturn400_WhenSymptomDescriptionMissing() throws Exception {
        // Given
        String requestBody = "{}";

        // When & Then
        mockMvc.perform(post("/ai/triage/quick-analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void analyzeSymptoms_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Given
        UUID patientId = UUID.randomUUID();
        when(aiTriageService.analyzeSymptoms(any(String.class), any(UUID.class)))
                .thenThrow(new RuntimeException("Service error"));

        String requestBody = """
                {
                    "patientId": "%s",
                    "symptomDescription": "Test symptoms"
                }
                """.formatted(patientId);

        // When & Then
        mockMvc.perform(post("/ai/triage/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void analyzeSymptoms_EmergencySymptoms_ShouldReturnEmergencyUrgency() throws Exception {
        // Given
        UUID patientId = UUID.randomUUID();
        TriageResult mockResult = TriageResult.builder()
                .patientId(patientId)
                .urgency(UrgencyLevel.EMERGENCY)
                .recommendedSpecialty("Emergency Medicine")
                .keyQuestions(List.of("Call 911 immediately"))
                .triageNotes("Severe chest pain - seek immediate care")
                .build();

        when(aiTriageService.analyzeSymptoms(any(String.class), eq(patientId)))
                .thenReturn(mockResult);

        String requestBody = """
                {
                    "patientId": "%s",
                    "symptomDescription": "Severe chest pain and difficulty breathing"
                }
                """.formatted(patientId);

        // When & Then
        mockMvc.perform(post("/ai/triage/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urgency").value("EMERGENCY"))
                .andExpect(jsonPath("$.recommendedSpecialty").value("Emergency Medicine"));
    }
}