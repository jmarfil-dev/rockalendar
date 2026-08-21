package com.jmarfildev.rockalendar.events.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.jmarfildev.rockalendar.common.error.ErrorConstants;

/**
 * @author jmarfil
 *
 */
public record SubmitEventRequest(@NotBlank @Size(max = 200) String title,
                                 @Size(max = 5_000) String description,
                                 // No lleva @FutureOrPresent: la fecha pasada solo se permite cuando dateTbd=true
                                 // (evento con fecha por confirmar), lo que se valida en EventCommandService.validate().
                                 @NotNull LocalDate startDate,
                                 LocalTime startTime,
                                 LocalDate endDate,
                                 @NotBlank @Size(max = 200) String venueName,
                                 @NotNull Short provinceId,
                                 @NotBlank @Size(max = 120) String cityName,
                                 @NotNull @Size(min = 1, message = ErrorConstants.VALID_SIZE_LIST_EMPTY) List<
                                         @NotBlank @Size(max = 200) String> artists,
                                 @URL(protocol = "https") @Size(max = 2_048) String sourceUrl,
                                 @URL(protocol = "https") @Size(max = 2_048) String ticketUrl,
                                 @Size(max = 512) String posterKey) {}
