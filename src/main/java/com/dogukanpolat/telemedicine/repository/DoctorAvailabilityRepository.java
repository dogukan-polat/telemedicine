package com.dogukanpolat.telemedicine.repository;

import com.dogukanpolat.telemedicine.model.DoctorAvailability;
import com.dogukanpolat.telemedicine.model.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, UUID> {

    List<DoctorAvailability> findByDoctorId(UUID doctorId);

    List<DoctorAvailability> findByDoctorIdAndDayOfWeek(UUID doctorId, DayOfWeek dayOfWeek);

    @Query("SELECT da FROM DoctorAvailability da WHERE da.doctor.id = :doctorId " +
            "AND da.dayOfWeek = :dayOfWeek AND da.startTime <= :time AND da.endTime >= :time")
    Optional<DoctorAvailability> findAvailabilityForTime(
            @Param("doctorId") UUID doctorId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("time") LocalTime time
    );

    @Query("SELECT da FROM DoctorAvailability da WHERE da.doctor.id = :doctorId " +
            "AND da.dayOfWeek = :dayOfWeek AND da.isAvailable = true " +
            "AND ((da.startTime <= :startTime AND da.endTime > :startTime) " +
            "OR (da.startTime < :endTime AND da.endTime >= :endTime) " +
            "OR (da.startTime >= :startTime AND da.endTime <= :endTime))")
    List<DoctorAvailability> findOverlappingAvailabilities(
            @Param("doctorId") UUID doctorId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("SELECT CASE WHEN COUNT(da) > 0 THEN true ELSE false END " +
            "FROM DoctorAvailability da WHERE da.doctor.id = :doctorId " +
            "AND da.dayOfWeek = :dayOfWeek AND da.isAvailable = true " +
            "AND da.startTime <= :startTime AND da.endTime >= :endTime")
    boolean isDoctorAvailableForSlot(
            @Param("doctorId") UUID doctorId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    void deleteByDoctorId(UUID doctorId);
}
