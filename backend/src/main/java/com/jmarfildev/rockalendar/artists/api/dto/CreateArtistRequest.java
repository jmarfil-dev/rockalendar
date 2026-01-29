package com.jmarfildev.rockalendar.artists.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @author jmarfil
 *
 */
public record CreateArtistRequest(@NotBlank @Size(max = 200) String name) {}
