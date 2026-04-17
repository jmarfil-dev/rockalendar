package com.jmarfildev.rockalendar.notifications.api.mapper;

import org.mapstruct.Mapper;

import com.jmarfildev.rockalendar.notifications.api.dto.NotificationDto;
import com.jmarfildev.rockalendar.notifications.domain.Notification;

/**
 * @author jmarfil
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationDto toDto(Notification notification);
}
