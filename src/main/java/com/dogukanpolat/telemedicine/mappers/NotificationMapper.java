package com.dogukanpolat.telemedicine.mappers;

import com.dogukanpolat.telemedicine.dto.notification.NotificationPreferenceResponseDto;
import com.dogukanpolat.telemedicine.model.NotificationPreference;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationPreferenceResponseDto toPreferenceDto(NotificationPreference preference);
}
