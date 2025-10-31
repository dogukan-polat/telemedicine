package com.dogukanpolat.telemedicine.mappers;

import com.dogukanpolat.telemedicine.dto.search.DoctorSearchResponseDto;
import com.dogukanpolat.telemedicine.dto.search.PatientSearchResponseDto;
import com.dogukanpolat.telemedicine.model.Doctor;
import com.dogukanpolat.telemedicine.model.Patient;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SearchMapper {
    DoctorSearchResponseDto toDoctorSearchResponseDto(Doctor doctor);
    PatientSearchResponseDto toPatientSearchResponseDto(Patient patient);
}
