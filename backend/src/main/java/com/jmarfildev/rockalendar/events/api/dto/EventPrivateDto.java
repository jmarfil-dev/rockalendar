package com.jmarfildev.rockalendar.events.api.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.jmarfildev.rockalendar.events.domain.EventStatus;

/**
 * @author jmarfil
 *
 */
public record EventPrivateDto(UUID id,
                              String title,
                              OffsetDateTime startDateTime,
                              String venueName,
                              UUID provinceId,
                              String provinceName,
                              String cityName,
                              List<String> artists,
                              String sourceUrl,
                              EventStatus status,
                              String rejectionReason,
                              OffsetDateTime createdAt) {}
