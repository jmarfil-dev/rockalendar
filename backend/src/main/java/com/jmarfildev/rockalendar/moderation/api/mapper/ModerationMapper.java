package com.jmarfildev.rockalendar.moderation.api.mapper;

import org.mapstruct.Mapper;

import com.jmarfildev.rockalendar.moderation.api.dto.ModerationArchivedDto;
import com.jmarfildev.rockalendar.moderation.api.dto.ModerationPendingDto;
import com.jmarfildev.rockalendar.moderation.persistence.ModerationArchivedProjection;
import com.jmarfildev.rockalendar.moderation.persistence.ModerationPendingProjection;

/**
 * @author jmarfil
 *
 */
@Mapper(componentModel = "spring")
public interface ModerationMapper {

    ModerationPendingDto toPendingDto(ModerationPendingProjection projection);

    ModerationArchivedDto toArchivedDto(ModerationArchivedProjection projection);
}
