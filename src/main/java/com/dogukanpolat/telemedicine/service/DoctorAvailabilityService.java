package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.availability.AvailabilityRequestDto;
import com.dogukanpolat.telemedicine.dto.availability.AvailabilityResponseDto;
import com.dogukanpolat.telemedicine.dto.availability.AvailabilityUpdateDto;
import com.dogukanpolat.telemedicine.dto.availability.BulkAvailabilityRequestDto;
import com.dogukanpolat.telemedicine.exception.AvailabilityException;
import com.dogukanpolat.telemedicine.mappers.AvailabilityMapper;
import com.dogukanpolat.telemedicine.model.Doctor;
import com.dogukanpolat.telemedicine.model.DoctorAvailability;
import com.dogukanpolat.telemedicine.model.enums.DayOfWeek;
import com.dogukanpolat.telemedicine.repository.DoctorAvailabilityRepository;
import com.dogukanpolat.telemedicine.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorAvailabilityService {
    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorRepository doctorRepository;
    private final AvailabilityMapper availabilityMapper;

    @Transactional
    public AvailabilityResponseDto createAvailability(AvailabilityRequestDto request) {
        log.info("Creating availability for doctor: {}", request.doctorId());

        // Validate doctor exists
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        // Validate time range
        validateTimeRange(request.startTime(), request.endTime());

        // Check for overlapping availability
        List<DoctorAvailability> overlapping = availabilityRepository.findOverlappingAvailabilities(
                request.doctorId(),
                request.dayOfWeek(),
                request.startTime(),
                request.endTime()
        );

        if (!overlapping.isEmpty()) {
            throw new AvailabilityException("Availability slot overlaps with existing slot");
        }

        DoctorAvailability availability = availabilityMapper.toEntity(request);
        availability.setDoctor(doctor);

        DoctorAvailability saved = availabilityRepository.save(availability);
        log.info("Availability created successfully: {}", saved.getId());

        return availabilityMapper.toResponseDto(saved);
    }

    @Transactional
    public List<AvailabilityResponseDto> createBulkAvailability(BulkAvailabilityRequestDto request) {
        log.info("Creating bulk availability for doctor: {}", request.doctorId());

        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        List<DoctorAvailability> availabilities = new ArrayList<>();

        for (BulkAvailabilityRequestDto.AvailabilitySlot slot : request.slots()) {
            validateTimeRange(slot.startTime(), slot.endTime());

            // Check for overlapping availability
            List<DoctorAvailability> overlapping = availabilityRepository.findOverlappingAvailabilities(
                    request.doctorId(),
                    slot.dayOfWeek(),
                    slot.startTime(),
                    slot.endTime()
            );

            if (!overlapping.isEmpty()) {
                throw new AvailabilityException("Availability slot for " + slot.dayOfWeek() +
                        " " + slot.startTime() + "-" + slot.endTime() + " overlaps with existing slot");
            }

            DoctorAvailability availability = new DoctorAvailability();
            availability.setDoctor(doctor);
            availability.setDayOfWeek(slot.dayOfWeek());
            availability.setStartTime(slot.startTime());
            availability.setEndTime(slot.endTime());
            availability.setIsAvailable(true);

            availabilities.add(availability);
        }

        List<DoctorAvailability> saved = availabilityRepository.saveAll(availabilities);
        log.info("Created {} availability slots for doctor: {}", saved.size(), request.doctorId());

        return saved.stream()
                .map(availabilityMapper::toResponseDto)
                .toList();
    }

    public List<AvailabilityResponseDto> getDoctorAvailability(UUID doctorId) {
        log.info("Fetching availability for doctor: {}", doctorId);

        List<DoctorAvailability> availabilities = availabilityRepository.findByDoctorId(doctorId);

        return availabilities.stream()
                .map(availabilityMapper::toResponseDto)
                .toList();
    }

    public List<AvailabilityResponseDto> getDoctorAvailabilityByDay(UUID doctorId, DayOfWeek dayOfWeek) {
        log.info("Fetching availability for doctor: {} on {}", doctorId, dayOfWeek);

        List<DoctorAvailability> availabilities = availabilityRepository
                .findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek);

        return availabilities.stream()
                .map(availabilityMapper::toResponseDto)
                .toList();
    }

    public List<AvailabilityResponseDto> getActiveAvailability(UUID doctorId) {
        log.info("Fetching active availability for doctor: {}", doctorId);

        List<DoctorAvailability> availabilities = availabilityRepository
                .findByDoctorIdAndIsAvailable(doctorId, true);

        return availabilities.stream()
                .map(availabilityMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public AvailabilityResponseDto updateAvailability(UUID availabilityId, AvailabilityUpdateDto updateDto) {
        log.info("Updating availability: {}", availabilityId);

        DoctorAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new IllegalArgumentException("Availability slot not found"));

        if (updateDto.startTime() != null && updateDto.endTime() != null) {
            validateTimeRange(updateDto.startTime(), updateDto.endTime());

            // Check for overlaps (excluding current slot)
            List<DoctorAvailability> overlapping = availabilityRepository.findOverlappingAvailabilities(
                    availability.getDoctor().getId(),
                    availability.getDayOfWeek(),
                    updateDto.startTime(),
                    updateDto.endTime()
            );

            overlapping.removeIf(a -> a.getId().equals(availabilityId));

            if (!overlapping.isEmpty()) {
                throw new AvailabilityException("Updated time slot overlaps with existing availability");
            }

            availability.setStartTime(updateDto.startTime());
            availability.setEndTime(updateDto.endTime());
        }

        if (updateDto.isAvailable() != null) {
            availability.setIsAvailable(updateDto.isAvailable());
        }

        DoctorAvailability updated = availabilityRepository.save(availability);
        log.info("Availability updated successfully: {}", availabilityId);

        return availabilityMapper.toResponseDto(updated);
    }

    @Transactional
    public void deleteAvailability(UUID availabilityId) {
        log.info("Deleting availability: {}", availabilityId);

        if (!availabilityRepository.existsById(availabilityId)) {
            throw new IllegalArgumentException("Availability slot not found");
        }

        availabilityRepository.deleteById(availabilityId);
        log.info("Availability deleted successfully: {}", availabilityId);
    }

    @Transactional
    public void deleteAllDoctorAvailability(UUID doctorId) {
        log.info("Deleting all availability for doctor: {}", doctorId);
        availabilityRepository.deleteByDoctorId(doctorId);
        log.info("All availability deleted for doctor: {}", doctorId);
    }

    public boolean isDoctorAvailable(UUID doctorId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        return availabilityRepository.isDoctorAvailableForSlot(doctorId, dayOfWeek, startTime, endTime);
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new AvailabilityException("Start time must be before end time");
        }
    }

}
