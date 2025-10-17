package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.model.AiTriageAudit;
import com.dogukanpolat.telemedicine.model.TriageResult;
import com.dogukanpolat.telemedicine.model.enums.UrgencyLevel;
import com.dogukanpolat.telemedicine.repository.AiTriageAuditRepository;
import com.dogukanpolat.telemedicine.repository.PatientRepository;
import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AITriageServiceTest {

    @Mock
    private AiTriageAuditRepository auditRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private Client mockClient;

    @Mock
    private Models mockModels;

    @InjectMocks
    private AITriageService aiTriageService;

    @BeforeEach
    void setUp() {
        // Inject API key for initialization
        ReflectionTestUtils.setField(aiTriageService, "apiKey", "dummy-api-key");
        ReflectionTestUtils.setField(aiTriageService, "client", mockClient);

        ReflectionTestUtils.setField(mockClient, "models", mockModels);
    }

    @Test
    void analyzeSymptoms_validResponse_shouldReturnParsedResultAndSaveAudit() {
        // Arrange
        UUID patientId = UUID.randomUUID();
        String description = "Patient has mild cough and fever.";

        String jsonResponse = """
            {
                "urgency": "LOW",
                "recommendedSpecialty": "General Practitioner",
                "keyQuestions": ["How long have you had symptoms?", "Do you have a fever?"],
                "triageNotes": "Mild symptoms, likely viral.",
                "confidence": 0.85
            }
        """;

        GenerateContentResponse mockResponse = mock(GenerateContentResponse.class);
        when(mockResponse.text()).thenReturn(jsonResponse);
        when(mockModels.generateContent(any(String.class), any(String.class), any())).thenReturn(mockResponse);

        // Mock patient repository
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(new com.dogukanpolat.telemedicine.model.Patient()));

        // Act
        TriageResult result = aiTriageService.analyzeSymptoms(description, patientId);

        // Assert
        assertNotNull(result);
        assertEquals(UrgencyLevel.LOW, result.getUrgency());
        assertEquals("General Practitioner", result.getRecommendedSpecialty());
        assertTrue(result.getKeyQuestions().contains("Do you have a fever?"));
        verify(auditRepository, times(1)).save(any(AiTriageAudit.class));
    }

    @Test
    void analyzeSymptoms_whenClientThrowsException_shouldReturnFallback() {
        // Arrange
        UUID patientId = UUID.randomUUID();
        String description = "Patient has chest pain.";
        when(mockModels.generateContent(any(String.class), any(String.class), any()))
                .thenThrow(new RuntimeException("API error"));

        // Act
        TriageResult result = aiTriageService.analyzeSymptoms(description, patientId);

        // Assert
        assertNotNull(result);
        assertEquals(UrgencyLevel.MEDIUM, result.getUrgency()); // fallback
        assertTrue(result.getTriageNotes().contains("Unable to analyze symptoms"));
        verify(auditRepository, never()).save(any());
    }

    @Test
    void parseTriageResponse_validJson_shouldReturnTriageResult() {
        // Arrange
        String jsonResponse = """
            {
                "urgency": "HIGH",
                "recommendedSpecialty": "Cardiology",
                "keyQuestions": ["When did chest pain start?"],
                "triageNotes": "Possible cardiac issue.",
                "confidence": 0.9
            }
        """;

        // Act
        TriageResult result = invokeParseMethod(jsonResponse);

        // Assert
        assertEquals(UrgencyLevel.HIGH, result.getUrgency());
        assertEquals("Cardiology", result.getRecommendedSpecialty());
        assertTrue(result.getKeyQuestions().contains("When did chest pain start?"));
    }

    @Test
    void parseTriageResponse_invalidJson_shouldReturnFallback() {
        // Arrange
        String invalidResponse = "Not a JSON";

        // Act
        TriageResult result = invokeParseMethod(invalidResponse);

        // Assert
        assertEquals(UrgencyLevel.MEDIUM, result.getUrgency()); // fallback
        assertTrue(result.getTriageNotes().contains("Unable to analyze"));
    }

    /**
     * Utility method to call private parseTriageResponse via reflection.
     */
    private TriageResult invokeParseMethod(String response) {
        try {
            var method = AITriageService.class.getDeclaredMethod("parseTriageResponse", String.class);
            method.setAccessible(true);
            return (TriageResult) method.invoke(aiTriageService, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
