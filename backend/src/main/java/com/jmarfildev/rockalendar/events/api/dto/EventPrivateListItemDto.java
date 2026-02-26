package com.jmarfildev.rockalendar.events.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.jmarfildev.rockalendar.events.domain.EventStatus;

/**
 * @author jmarfil
 *
 */
public record EventPrivateListItemDto(UUID id,
                                      String title,
                                      OffsetDateTime startDateTime,
                                      String provinceName,
                                      String cityName,
                                      EventStatus status,
                                      String moderationMessage,
                                      OffsetDateTime createdAt) {}
