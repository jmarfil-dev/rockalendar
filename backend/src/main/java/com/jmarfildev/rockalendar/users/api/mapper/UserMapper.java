package com.jmarfildev.rockalendar.users.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.jmarfildev.rockalendar.users.api.dto.MeDto;
import com.jmarfildev.rockalendar.users.domain.User;

/**
 * @author jmarfil
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "promotionEligible", source = "eligible")
    MeDto toMeDto(User user, boolean eligible);
}
