package com.jmarfildev.rockalendar.events.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @author jmarfil
 */
public record PostCommentRequest(@Email @Size(max = 255) String authorEmail,
                                 @Size(max = 200) String authorName,
                                 @NotBlank @Size(max = 2000) String body) {}
