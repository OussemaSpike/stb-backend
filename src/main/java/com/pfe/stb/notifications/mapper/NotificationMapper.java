package com.pfe.stb.notifications.mapper;

import com.pfe.stb.notifications.dto.NotificationDto;
import com.pfe.stb.notifications.model.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class NotificationMapper {

  public abstract NotificationDto toDto(Notification notification);
}
