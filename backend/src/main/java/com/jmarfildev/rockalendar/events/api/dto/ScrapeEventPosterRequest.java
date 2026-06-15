package com.jmarfildev.rockalendar.events.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @author jmarfil
 *
 */
public record ScrapeEventPosterRequest(
        @NotBlank @Size(max = 2048) String sourceUrl) {}
