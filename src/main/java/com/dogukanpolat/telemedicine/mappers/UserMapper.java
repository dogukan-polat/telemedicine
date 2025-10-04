package com.dogukanpolat.telemedicine.mappers;

import com.dogukanpolat.telemedicine.dto.user.*;
import com.dogukanpolat.telemedicine.model.Doctor;
import com.dogukanpolat.telemedicine.model.Patient;
import com.dogukanpolat.telemedicine.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "password", source = "password.password")
    UserModel toUser(UserRegistrationDto userRegistrationDto);

    Patient toPatient(PatientRegistrationDto patientRegistrationDto);

    Doctor toDoctor(DoctorRegistrationDto doctorRegistrationDto);

    PatientResponseDto toPatientResponse(Patient patient);

    DoctorResponseDto toDoctorResponse(Doctor doctor);
}
