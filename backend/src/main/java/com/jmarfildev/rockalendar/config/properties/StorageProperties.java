package com.jmarfildev.rockalendar.config.properties;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author jmarfil
 *
 */
@ConfigurationProperties(prefix = "rockalendar.storage")
@Validated
public record StorageProperties(@NotBlank String endpoint,
                                @NotBlank String bucket,
                                @NotBlank String accessKey,
                                @NotBlank String secretKey,
                                @NotBlank String publicUrlBase) {
}
