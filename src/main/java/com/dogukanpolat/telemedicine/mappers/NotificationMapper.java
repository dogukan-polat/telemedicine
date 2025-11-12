package com.dogukanpolat.telemedicine.mappers;

import com.dogukanpolat.telemedicine.dto.notification.NotificationLogResponseDto;
import com.dogukanpolat.telemedicine.dto.notification.NotificationPreferenceResponseDto;
import com.dogukanpolat.telemedicine.model.NotificationLog;
import com.dogukanpolat.telemedicine.model.NotificationPreference;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationPreferenceResponseDto toPreferenceDto(NotificationPreference preference);
    NotificationLogResponseDto toLogDto(NotificationLog notificationLog);
}
