package com.dogukanpolat.telemedicine.mappers;

import com.dogukanpolat.telemedicine.dto.appointment.AppointmentRequestDto;
import com.dogukanpolat.telemedicine.dto.appointment.AppointmentResponseDto;
import com.dogukanpolat.telemedicine.model.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    Appointment toAppointment(AppointmentRequestDto appointmentRequestDto);

    @Mapping(target = "patientFirstName", source = "patient.user.firstName")
    @Mapping(target = "patientLastName", source = "patient.user.lastName")
    @Mapping(target = "doctorFirstName", source = "doctor.user.firstName")
    @Mapping(target = "doctorLastName", source = "doctor.user.lastName")
    AppointmentResponseDto toAppointmentResponseDto(Appointment appointment);
}
