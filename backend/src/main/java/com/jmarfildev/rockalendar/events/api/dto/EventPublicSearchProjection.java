package com.jmarfildev.rockalendar.events.api.dto;

import java.time.Instant;
import java.util.UUID;

public interface EventPublicSearchProjection {
    UUID getId();
    String getTitle();
    String getDescription();
    Instant getStartDateTime();
    Instant getEndDateTime();
    String getVenueName();
    UUID getProvinceId();
    String getProvinceName();
    String getCityName();
    String[] getArtists();
    String getSourceUrl();
}