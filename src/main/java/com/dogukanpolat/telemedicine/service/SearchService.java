package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.appointment.AppointmentResponseDto;
import com.dogukanpolat.telemedicine.dto.search.*;
import com.dogukanpolat.telemedicine.mappers.AppointmentMapper;
import com.dogukanpolat.telemedicine.mappers.SearchMapper;
import com.dogukanpolat.telemedicine.model.Appointment;
import com.dogukanpolat.telemedicine.model.Doctor;
import com.dogukanpolat.telemedicine.model.Patient;
import com.dogukanpolat.telemedicine.repository.DoctorAvailabilityRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final DoctorAvailabilityRepository availabilityRepository;
    private final EntityManager entityManager;
    private final AppointmentMapper appointmentMapper;
    private final SearchMapper searchMapper;

    public List<DoctorSearchResponseDto> searchDoctors(DoctorSearchCriteria criteria) {
        log.info("Searching doctors with criteria: {}", criteria);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Doctor> query = cb.createQuery(Doctor.class);
        Root<Doctor> doctor = query.from(Doctor.class);
        doctor.fetch("user", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        if (criteria.specialization() != null && !criteria.specialization().isBlank()) {
            predicates.add(cb.like(doctor.get("specialization"), "%" + criteria.specialization() + "%"));
        }

        if (criteria.minFee() != null) {
            predicates.add(cb.greaterThanOrEqualTo(doctor.get("consultationFee"), criteria.minFee()));
        }

        if (criteria.maxFee() != null) {
            predicates.add(cb.lessThanOrEqualTo(doctor.get("consultationFee"), criteria.maxFee()));
        }

        if (criteria.minExperience() != null) {
            predicates.add(cb.greaterThanOrEqualTo(doctor.get("yearsOfExperience"), criteria.minExperience()));
        }

        if (criteria.isVerified() != null) {
            predicates.add(cb.equal(doctor.get("isVerified"), criteria.isVerified()));
        }

        if (criteria.name() != null && !criteria.name().isBlank()) {
            String namePattern = "%" + criteria.name().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(doctor.get("user").get("firstName")), namePattern),
                    cb.like(cb.lower(doctor.get("user").get("lastName")), namePattern)
            ));
        }

        query.where(predicates.toArray(new Predicate[0]));
        List<Doctor> doctors = entityManager.createQuery(query).getResultList();

        if (criteria.availableDay() != null && criteria.availableStartTime() != null && criteria.availableEndTime() != null) {
            doctors = doctors.stream().filter(d -> availabilityRepository
                    .isDoctorAvailableForSlot(
                            d.getId(),
                            criteria.availableDay(),
                            criteria.availableStartTime(),
                            criteria.availableEndTime())).toList();
        }

        return doctors.stream().map(searchMapper::toDoctorSearchResponseDto).toList();
    }

    public List<PatientSearchResponseDto> searchPatients(PatientSearchCriteria criteria) {
        log.info("Searching patients with criteria: {}", criteria);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Patient> query = cb.createQuery(Patient.class);
        Root<Patient> patient = query.from(Patient.class);
        patient.fetch("user", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        if (criteria.name() != null && !criteria.name().isBlank()) {
            String namePattern = "%" + criteria.name().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(patient.get("user").get("firstName")), namePattern),
                    cb.like(cb.lower(patient.get("user").get("lastName")), namePattern)
            ));
        }

        if (criteria.email() != null && !criteria.email().isBlank()) {
            predicates.add(cb.like(cb.lower(patient.get("user").get("email")),
                    "%" + criteria.email().toLowerCase() + "%"));
        }

        if (criteria.bloodType() != null && !criteria.bloodType().isBlank()) {
            predicates.add(cb.equal(patient.get("bloodType"), criteria.bloodType()));
        }

        if (criteria.isActive() != null) {
            predicates.add(cb.equal(patient.get("user").get("isActive"), criteria.isActive()));
        }

        if (criteria.phoneNumber() != null && !criteria.phoneNumber().isBlank()) {
            predicates.add(cb.like(patient.get("user").get("phoneNumber"),
                    "%" + criteria.phoneNumber() + "%"));
        }

        query.where(predicates.toArray(new Predicate[0]));
        List<Patient> patients = entityManager.createQuery(query).getResultList();

        return patients.stream().map(searchMapper::toPatientSearchResponseDto).toList();
    }

    public List<AppointmentResponseDto> filterAppointments(AppointmentFilterCriteria criteria) {
        log.info("Filtering appointments with criteria: {}", criteria);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Appointment> query = cb.createQuery(Appointment.class);
        Root<Appointment> appointment = query.from(Appointment.class);
        appointment.fetch("patient", JoinType.LEFT).fetch("user", JoinType.LEFT);
        appointment.fetch("doctor", JoinType.LEFT).fetch("user", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        if (criteria.patientId() != null) {
            predicates.add(cb.equal(appointment.get("patient").get("id"), criteria.patientId()));
        }

        if (criteria.doctorId() != null) {
            predicates.add(cb.equal(appointment.get("doctor").get("id"), criteria.doctorId()));
        }

        if (criteria.status() != null) {
            predicates.add(cb.equal(appointment.get("status"), criteria.status()));
        }

        if (criteria.startDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(appointment.get("scheduledDate"), criteria.startDate()));
        }

        if (criteria.endDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(appointment.get("scheduledDate"), criteria.endDate()));
        }

        query.where(predicates.toArray(new Predicate[0]));
        List<Appointment> appointments = entityManager.createQuery(query).getResultList();

        return appointments.stream().map(appointmentMapper::toAppointmentResponseDto).toList();
    }
}
