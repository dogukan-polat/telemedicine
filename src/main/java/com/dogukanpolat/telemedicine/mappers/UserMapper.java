package com.dogukanpolat.telemedicine.mappers;

import com.dogukanpolat.telemedicine.dto.admin.UserManagementDto;
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

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "phoneNumber", source = "user.phoneNumber")
    PatientResponseDto toPatientResponse(Patient patient);

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "phoneNumber", source = "user.phoneNumber")
    DoctorResponseDto toDoctorResponse(Doctor doctor);

    UserManagementDto toUserManagementDto(UserModel user);
}
