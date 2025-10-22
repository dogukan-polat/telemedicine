package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.ai.QuickTriageRequest;
import com.dogukanpolat.telemedicine.dto.ai.TriageRequest;
import com.dogukanpolat.telemedicine.model.TriageResult;
import com.dogukanpolat.telemedicine.service.AITriageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/triage")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Triage", description = "AI-powered symptom analysis and triage recommendations")
@SecurityRequirement(name = "bearerAuth")
public class AiTriageController {

    private final AITriageService aiTriageService;

    @Operation(
            summary = "Analyze patient symptoms with AI",
            description = """
                    Analyze patient symptoms using AI to determine urgency level and recommended specialty.
                    This endpoint is restricted to doctors only and creates an audit trail.
                    
                    The AI evaluates symptoms and provides:
                    - Urgency level (LOW, MEDIUM, HIGH, EMERGENCY)
                    - Recommended medical specialty
                    - Key follow-up questions
                    - Triage notes
                    
                    ⚠️ This is for triage assistance only, not medical diagnosis.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Patient symptoms and identification",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TriageRequest.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Triage analysis completed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TriageResult.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only doctors can use this endpoint."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "AI Service error - fallback response provided."
            )
    })
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

    @Operation(
            summary = "Quick triage analysis(Without patient identity)",
            description = """
                    Analyze patient symptoms using AI to determine urgency level and recommended specialty.
                    This endpoint is restricted to doctors only and creates an audit trail. It doesn't save to database.
                    
                    The AI evaluates symptoms and provides:
                    - Urgency level (LOW, MEDIUM, HIGH, EMERGENCY)
                    - Recommended medical specialty
                    - Key follow-up questions
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Symptom description for quick-analysis",
                    required = true,
                    content = @Content(schema = @Schema(implementation = QuickTriageRequest.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Quick triage analysis completed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TriageResult.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - symptom description required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only doctors can use this endpoint."
            )
    })
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