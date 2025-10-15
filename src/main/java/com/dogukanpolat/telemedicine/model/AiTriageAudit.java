package com.dogukanpolat.telemedicine.model;

import com.dogukanpolat.telemedicine.model.enums.UrgencyLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "ai_triage_audits")
public class AiTriageAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "user_input")
    private String userInput;

    @Column(name = "ai_output")
    private String aiOutput;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency_level")
    private UrgencyLevel urgencyLevel;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

}