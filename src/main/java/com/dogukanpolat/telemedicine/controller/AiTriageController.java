package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.ai.QuickTriageRequest;
import com.dogukanpolat.telemedicine.dto.ai.TriageRequest;
import com.dogukanpolat.telemedicine.model.TriageResult;
import com.dogukanpolat.telemedicine.service.AITriageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/triage")
@RequiredArgsConstructor
@Slf4j
public class AiTriageController {

    private final AITriageService aiTriageService;

    @PostMapping("/analyze")
    public ResponseEntity<TriageResult> analyzeSymptoms(@Valid @RequestBody TriageRequest request) {
        log.info("Received triage request for patient: {}", request.patientId());

        TriageResult result = aiTriageService.analyzeSymptoms(
                request.symptomDescription(),
                request.patientId()
        );

        log.info("Triage analysis completed with urgency: {}", result.getUrgency());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/quick-analyze")
    public ResponseEntity<TriageResult> quickAnalyze(@Valid @RequestBody QuickTriageRequest request) {
        log.info("Received quick triage request (no patient ID)");

        TriageResult result = aiTriageService.analyzeSymptoms(
                request.symptomDescription(),
                null
        );

        return ResponseEntity.ok(result);
    }
}