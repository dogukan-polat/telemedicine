package com.dogukanpolat.telemedicine.repository;

import com.dogukanpolat.telemedicine.model.MedicalRecord;
import com.dogukanpolat.telemedicine.model.enums.MedicalRecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {
    List<MedicalRecord> findByPatientIdAndIsActiveTrue(UUID patientId);

    List<MedicalRecord> findByPatientIdAndRecordTypeAndIsActiveTrue(UUID patientId, MedicalRecordType recordType);

    List<MedicalRecord> findByDoctorIdAndIsActiveTrue(UUID doctorId);

    List<MedicalRecord> findByAppointmentIdAndIsActiveTrue(UUID appointmentId);

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patient.id = :patientId " +
            "AND mr.recordDate BETWEEN :startDate AND :endDate " +
            "AND mr.isActive = true")
    List<MedicalRecord> findByPatientIdAndDateRange(
            UUID patientId,
            OffsetDateTime startDate,
            OffsetDateTime endDate
    );

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patient.id = :patientId " +
            "AND mr.isActive = true " +
            "AND (LOWER(mr.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(mr.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<MedicalRecord> searchByPatientIdAndTerm(UUID patientId, String searchTerm);

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.id = :recordId " +
            "AND mr.isActive = true " +
            "ORDER BY mr.version DESC")
    Optional<MedicalRecord> findLatestVersionByRecordId(UUID recordId);

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.previousVersion.id = :recordId " +
            "OR mr.id = :recordId " +
            "ORDER BY mr.version ASC")
    List<MedicalRecord> findAllVersions(UUID recordId);

    @Query("SELECT COUNT(mr) FROM MedicalRecord mr WHERE mr.patient.id = :patientId " +
            "AND mr.isActive = true")
    Long countByPatientId(UUID patientId);
}