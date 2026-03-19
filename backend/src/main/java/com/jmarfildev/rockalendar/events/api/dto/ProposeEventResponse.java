package com.jmarfildev.rockalendar.events.api.dto;

/**
 * Respuesta al proponer un evento.
 * {@code possibleDuplicate} es nulo si no se detectó ningún evento similar.
 */
public record ProposeEventResponse(EventPrivateDto event, PossibleDuplicateDto possibleDuplicate) {}
