package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.model.AiTriageAudit;
import com.dogukanpolat.telemedicine.model.TriageResult;
import com.dogukanpolat.telemedicine.model.enums.UrgencyLevel;
import com.dogukanpolat.telemedicine.repository.AiTriageAuditRepository;
import com.dogukanpolat.telemedicine.repository.PatientRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AITriageService {
    @Value("${gemini.api-key}")
    private String apiKey;
    private final Client client = Client.builder().apiKey(apiKey).build();

    private final AiTriageAuditRepository auditRepository;
    private final PatientRepository patientRepository;

    public TriageResult analyzeSymptoms(String patientDescription, UUID patientId) {
        try {
            String prompt = """
                Analyze these patient symptoms and provide a structured response.
                Focus on urgency assessment and specialty recommendation.
                
                PATIENT SYMPTOMS: %s
                
                Please respond in this exact JSON format:
                {
                    "urgency": "LOW|MEDIUM|HIGH|EMERGENCY",
                    "recommendedSpecialty": "string",
                    "keyQuestions": ["question1", "question2", "question3"],
                    "triageNotes": "string",
                    "confidence": "number between 0-1"
                }
                
                Guidelines:
                - EMERGENCY: chest pain, difficulty breathing, severe bleeding, stroke symptoms
                - HIGH: severe pain, high fever, sudden weakness
                - MEDIUM: persistent symptoms, moderate discomfort
                - LOW: mild symptoms, routine concerns
                - Always be conservative in assessment
                """.formatted(patientDescription);

            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
            String responseText = response.text();

            TriageResult result = parseTriageResponse(responseText);
            result.setPatientId(patientId);

            auditTriageInteraction(patientId, patientDescription, responseText, result);

            return result;

        } catch (Exception e) {
            log.error("AI triage failed for patient: {}", patientId, e);
            return getFallbackTriageResult();
        }
    }

    private TriageResult parseTriageResponse(String responseText) {
        try {
            // Clean the response - Gemini might wrap JSON in ```
            String cleanJson = responseText.replaceAll("```json", "").replaceAll("```", "").trim();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(cleanJson);

            return TriageResult.builder()
                    .urgency(UrgencyLevel.valueOf(jsonNode.get("urgency").asText()))
                    .recommendedSpecialty(jsonNode.get("recommendedSpecialty").asText())
                    .keyQuestions(mapper.convertValue(jsonNode.get("keyQuestions"), List.class))
                    .triageNotes(jsonNode.get("triageNotes").asText())
                    .build();

        } catch (Exception e) {
            log.warn("Failed to parse AI response, using fallback. Response: {}", responseText);
            return getFallbackTriageResult();
        }
    }

    private void auditTriageInteraction(UUID patientId, String input, String rawResponse, TriageResult result) {
        AiTriageAudit audit = new AiTriageAudit();
        audit.setPatient(patientRepository.findById(patientId).orElseThrow());
        audit.setUserInput(input);
        audit.setAiOutput(rawResponse);
        audit.setUrgencyLevel(result.getUrgency());
        audit.setCreatedAt(OffsetDateTime.now());
        auditRepository.save(audit);
    }


    private TriageResult getFallbackTriageResult() {
        return TriageResult.builder()
                .urgency(UrgencyLevel.MEDIUM)
                .triageNotes("Unable to analyze symptoms - please contact clinic directly")
                .keyQuestions(List.of("When did symptoms start?", "Have you taken any medication?"))
                .build();
    }

}