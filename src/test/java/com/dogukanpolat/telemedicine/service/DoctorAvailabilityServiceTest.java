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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorAvailabilityServiceTest {

    @Mock
    private DoctorAvailabilityRepository availabilityRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AvailabilityMapper availabilityMapper;

    @InjectMocks
    private DoctorAvailabilityService availabilityService;

    private UUID doctorId;
    private Doctor doctor;
    private DoctorAvailability availability;
    private AvailabilityRequestDto requestDto;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        UUID availabilityId = UUID.randomUUID();

        doctor = new Doctor();
        doctor.setId(doctorId);

        availability = new DoctorAvailability();
        availability.setId(availabilityId);
        availability.setDoctor(doctor);
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        availability.setStartTime(LocalTime.of(9,0));
        availability.setEndTime(LocalTime.of(17,0));
        availability.setIsAvailable(true);

        requestDto = new AvailabilityRequestDto(
                doctorId,
                DayOfWeek.MONDAY,
                LocalTime.of(9,0),
                LocalTime.of(17,0)
        );
    }

    @Test
    void createAvailability_ShouldCreateAvailability() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findOverlappingAvailabilities(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(availabilityMapper.toEntity(requestDto)).thenReturn(availability);
        when(availabilityRepository.save(any(DoctorAvailability.class))).thenReturn(availability);

        AvailabilityResponseDto responseDto = new AvailabilityResponseDto(
                availability.getId(),
                doctorId,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                true,
                null
        );
        when(availabilityMapper.toResponseDto(availability)).thenReturn(responseDto);

        // When
        AvailabilityResponseDto result = availabilityService.createAvailability(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.doctorId()).isEqualTo(doctorId);
        assertThat(result.dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        verify(availabilityRepository).save(any(DoctorAvailability.class));
    }

    @Test
    void createAvailability_ShouldThrowException_WhenDoctorNotFound() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> availabilityService.createAvailability(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Doctor not found");

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void createAvailability_ShouldThrowException_WhenDoctorIsAlreadyAssigned() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findOverlappingAvailabilities(any(), any(), any(), any()))
                .thenReturn(List.of(availability));

        assertThatThrownBy(() -> availabilityService.createAvailability(requestDto))
                .isInstanceOf(AvailabilityException.class)
                .hasMessage("Availability slot overlaps with existing slot");

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void createAvailability_ShouldThrowException_WhenInvalidTimeRange() {
        AvailabilityRequestDto invalidRequestDto = new AvailabilityRequestDto(
                doctorId,
                DayOfWeek.MONDAY,
                LocalTime.of(17,0),
                LocalTime.of(9,0)
        );

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> availabilityService.createAvailability(invalidRequestDto))
                .isInstanceOf(AvailabilityException.class)
                .hasMessage("Start time must be before end time");

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void getDoctorAvailability_Success() {
        // Given
        when(availabilityRepository.findByDoctorId(doctorId))
                .thenReturn(List.of(availability));

        AvailabilityResponseDto responseDto = new AvailabilityResponseDto(
                availability.getId(),
                doctorId,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                true,
                null
        );
        when(availabilityMapper.toResponseDto(availability)).thenReturn(responseDto);

        // When
        List<AvailabilityResponseDto> result = availabilityService.getDoctorAvailability(doctorId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().doctorId()).isEqualTo(doctorId);
        verify(availabilityRepository).findByDoctorId(doctorId);
    }

    @Test
    void updateAvailability_Success() {
        // Given
        UUID availabilityId = availability.getId();
        AvailabilityUpdateDto updateDto = new AvailabilityUpdateDto(
                LocalTime.of(10, 0),
                LocalTime.of(18, 0),
                true
        );

        when(availabilityRepository.findById(availabilityId)).thenReturn(Optional.of(availability));
        when(availabilityRepository.findOverlappingAvailabilities(any(), any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(availabilityRepository.save(any(DoctorAvailability.class))).thenReturn(availability);

        AvailabilityResponseDto responseDto = new AvailabilityResponseDto(
                availabilityId,
                doctorId,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(18, 0),
                true,
                null
        );
        when(availabilityMapper.toResponseDto(availability)).thenReturn(responseDto);

        // When
        AvailabilityResponseDto result = availabilityService.updateAvailability(availabilityId, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(availabilityRepository).save(availability);
    }

    @Test
    void deleteAvailability_Success() {
        // Given
        UUID availabilityId = availability.getId();
        when(availabilityRepository.existsById(availabilityId)).thenReturn(true);
        doNothing().when(availabilityRepository).deleteById(availabilityId);

        // When
        availabilityService.deleteAvailability(availabilityId);

        // Then
        verify(availabilityRepository).deleteById(availabilityId);
    }

    @Test
    void deleteAvailability_NotFound_ThrowsException() {
        // Given
        UUID availabilityId = UUID.randomUUID();
        when(availabilityRepository.existsById(availabilityId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> availabilityService.deleteAvailability(availabilityId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Availability slot not found");

        verify(availabilityRepository, never()).deleteById(any());
    }

    @Test
    void isDoctorAvailable_ReturnsTrue() {
        // Given
        when(availabilityRepository.isDoctorAvailableForSlot(any(), any(), any(), any()))
                .thenReturn(true);

        // When
        boolean result = availabilityService.isDoctorAvailable(
                doctorId,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void createBulkAvailability_Success() {
        // Given
        BulkAvailabilityRequestDto bulkRequest = new BulkAvailabilityRequestDto(
                doctorId,
                List.of(
                        new BulkAvailabilityRequestDto.AvailabilitySlot(
                                DayOfWeek.MONDAY,
                                LocalTime.of(9, 0),
                                LocalTime.of(12, 0)
                        ),
                        new BulkAvailabilityRequestDto.AvailabilitySlot(
                                DayOfWeek.TUESDAY,
                                LocalTime.of(9, 0),
                                LocalTime.of(12, 0)
                        )
                )
        );

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findOverlappingAvailabilities(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(availabilityRepository.saveAll(any())).thenReturn(List.of(availability, availability));

        AvailabilityResponseDto responseDto = new AvailabilityResponseDto(
                availability.getId(),
                doctorId,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                true,
                null
        );
        when(availabilityMapper.toResponseDto(any())).thenReturn(responseDto);

        // When
        List<AvailabilityResponseDto> result = availabilityService.createBulkAvailability(bulkRequest);

        // Then
        assertThat(result).hasSize(2);
        verify(availabilityRepository).saveAll(any());
    }
}