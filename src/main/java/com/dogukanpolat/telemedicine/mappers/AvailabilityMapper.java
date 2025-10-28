package com.dogukanpolat.telemedicine.mappers;

import com.dogukanpolat.telemedicine.dto.availability.AvailabilityRequestDto;
import com.dogukanpolat.telemedicine.dto.availability.AvailabilityResponseDto;
import com.dogukanpolat.telemedicine.model.DoctorAvailability;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AvailabilityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "isAvailable", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DoctorAvailability toEntity(AvailabilityRequestDto availabilityRequestDto);

    @Mapping(target = "doctorId", source = "doctor.id")
    AvailabilityResponseDto toResponseDto(DoctorAvailability availability);
}