package com.jmarfildev.rockalendar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

/**
 * @author jmarfil
 *
 */
@ConfigurationProperties(prefix = "rockalendar.search.public")
@Validated // Valida valores correctos o falla en arranque
public record PublicSearchProperties(@DecimalMin("0.0") @DecimalMax("1.0") double minSimilarity,
                                     @DecimalMin("0.1") double ftsWeight,
                                     @DecimalMin("0.1") double trgmWeight) {}
