
package com.dogukanpolat.telemedicine.mappers;

import com.dogukanpolat.telemedicine.dto.medicalrecord.MedicalRecordResponseDto;
import com.dogukanpolat.telemedicine.model.MedicalRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicalRecordMapper {

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientName", expression = "java(record.getPatient().getUser().getFirstName() + \" \" + record.getPatient().getUser().getLastName())")
    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "doctorName", expression = "java(record.getDoctor() != null ? record.getDoctor().getUser().getFirstName() + \" \" + record.getDoctor().getUser().getLastName() : null)")
    @Mapping(target = "appointmentId", source = "appointment.id")
    @Mapping(target = "uploadedBy", source = "uploadedBy.id")
    @Mapping(target = "uploadedByName", expression = "java(record.getUploadedBy() != null ? record.getUploadedBy().getFirstName() + \" \" + record.getUploadedBy().getLastName() : null)")
    @Mapping(target = "previousVersionId", source = "previousVersion.id")
    MedicalRecordResponseDto toResponseDto(MedicalRecord record);
}