package com.dogukanpolat.telemedicine.mappers;

import com.dogukanpolat.telemedicine.dto.user.DoctorRegistrationDto;
import com.dogukanpolat.telemedicine.dto.user.PatientRegistrationDto;
import com.dogukanpolat.telemedicine.dto.user.UserRegistrationDto;
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
}
