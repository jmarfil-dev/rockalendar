package com.jmarfildev.rockalendar.notifications.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.jmarfildev.rockalendar.notifications.api.dto.NotificationDto;
import com.jmarfildev.rockalendar.notifications.domain.Notification;

/**
 * @author jmarfil
 *
 * El campo {@code isRead} del entity genera getter {@code isRead()} via Lombok, que JavaBeans
 * interpreta como propiedad {@code read}. El mapping explícito corrige la discrepancia con
 * el record component {@code isRead} del DTO.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "read", target = "isRead")
    NotificationDto toDto(Notification notification);
}
