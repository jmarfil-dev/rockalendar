package com.jmarfildev.rockalendar.notifications.api.dto;

/**
 * @author jmarfil
 */
public record UnreadCountDto(long user, long moderation, long admin) {}
