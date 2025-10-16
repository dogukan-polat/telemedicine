package com.dogukanpolat.telemedicine.repository;

import com.dogukanpolat.telemedicine.model.AiTriageAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AiTriageAuditRepository extends JpaRepository<AiTriageAudit, UUID> {
}
