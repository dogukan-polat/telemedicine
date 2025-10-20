package com.dogukanpolat.telemedicine.mappers;

import com.dogukanpolat.telemedicine.dto.admin.AiTriageAuditDto;
import com.dogukanpolat.telemedicine.model.AiTriageAudit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AiTriageMapper {
    AiTriageAuditDto toAiTriageAuditDto(AiTriageAudit aiTriageAudit);
}
