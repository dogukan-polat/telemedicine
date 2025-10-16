package com.dogukanpolat.telemedicine.model;

import com.dogukanpolat.telemedicine.model.enums.UrgencyLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TriageResult {
    private UUID patientId;
    private UrgencyLevel urgency;
    private String recommendedSpecialty;
    private List<String> keyQuestions;
    private String triageNotes;
    private String disclaimer = "AI analysis for triage assistance only. Not medical advice.";

    public static TriageResult emergency(String notes) {
        return TriageResult.builder()
                .urgency(UrgencyLevel.EMERGENCY)
                .triageNotes(notes)
                .keyQuestions(List.of("When did symptoms start?", "Any chest pain or difficulty breathing?"))
                .build();
    }

    public static TriageResult high(String notes) {
        return TriageResult.builder()
                .urgency(UrgencyLevel.HIGH)
                .triageNotes(notes)
                .keyQuestions(List.of("When did symptoms start?", "Any chest pain or difficulty breathing?"))
                .build();
    }

    public static TriageResult medium(String notes) {
        return TriageResult.builder()
                .urgency(UrgencyLevel.MEDIUM)
                .triageNotes(notes)
                .keyQuestions(List.of("When did symptoms start?", "Any chest pain or difficulty breathing?"))
                .build();
    }

    public static TriageResult low(String notes) {
        return TriageResult.builder()
                .urgency(UrgencyLevel.LOW)
                .triageNotes(notes)
                .keyQuestions(List.of("When did symptoms start?", "Any chest pain or difficulty breathing?"))
                .build();
    }
}