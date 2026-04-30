package com.jmarfildev.rockalendar.notifications.domain;

import java.util.Arrays;
import java.util.List;

/**
 * Tipos de notificación in-app que el sistema puede emitir.
 * Cada tipo pertenece a una {@link Bandeja} que determina en qué sección
 * de la UI se muestra y qué roles la reciben.
 *
 * @author jmarfil
 */
public enum NotificationType {
    EVENT_PENDING_MODERATION(Bandeja.MODERATION),
    EVENT_FLAGGED(Bandeja.MODERATION),
    EVENT_APPROVED(Bandeja.USER),
    EVENT_REJECTED(Bandeja.USER),
    EVENT_NEEDS_CHANGES(Bandeja.USER),
    EVENT_COMMENT(Bandeja.MODERATION),
    POSSIBLE_DUPLICATE_DETECTED(Bandeja.MODERATION),
    PROMOTION_REQUEST(Bandeja.ADMIN),
    USER_AUTOBANNED(Bandeja.ADMIN);

    public enum Bandeja { USER, MODERATION, ADMIN }

    private final Bandeja bandeja;

    NotificationType(Bandeja bandeja) {
        this.bandeja = bandeja;
    }

    public Bandeja getBandeja() {
        return bandeja;
    }

    public static List<NotificationType> ofBandeja(Bandeja b) {
        return Arrays.stream(values()).filter(t -> t.bandeja == b).toList();
    }
}
