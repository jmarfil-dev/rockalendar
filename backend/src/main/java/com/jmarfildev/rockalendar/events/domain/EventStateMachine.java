package com.jmarfildev.rockalendar.events.domain;

/**
 * Centraliza todas las reglas de transición de estado de un evento según el rol del actor.
 * Todas las validaciones de estado deben pasar por aquí, nunca dispersarse en los servicios.
 *
 * @author jmarfil
 */
public final class EventStateMachine {

    private EventStateMachine() {}

    /**
     * El propietario puede editar el evento si está en alguno de estos estados.
     * Al guardar, el evento vuelve a PENDING_MODERATION (o FLAGGED si la auto-moderación lo detecta).
     */
    public static boolean canOwnerEdit(EventStatus status) {
        return status == EventStatus.DRAFT
                || status == EventStatus.PENDING_MODERATION
                || status == EventStatus.NEEDS_CHANGES
                || status == EventStatus.APPROVED;
    }

    /**
     * El propietario puede eliminar el evento si está en alguno de estos estados.
     * Los eventos APPROVED no son eliminables por el propietario (ver error EVENT_NOT_ERASABLE_APPROVED).
     */
    public static boolean canOwnerDelete(EventStatus status) {
        return status == EventStatus.PENDING_MODERATION
                || status == EventStatus.NEEDS_CHANGES
                || status == EventStatus.DRAFT;
    }

    /**
     * Un moderador puede realizar esta transición de estado sobre el evento.
     * Un moderador no puede moderar su propio evento (validación en el servicio).
     */
    public static boolean canModeratorTransition(EventStatus from, EventStatus to) {
        return switch (from) {
            case PENDING_MODERATION -> to == EventStatus.APPROVED
                    || to == EventStatus.REJECTED
                    || to == EventStatus.NEEDS_CHANGES
                    || to == EventStatus.HIDDEN;
            case APPROVED -> to == EventStatus.REJECTED
                    || to == EventStatus.NEEDS_CHANGES
                    || to == EventStatus.HIDDEN;
            default -> false;
        };
    }

    /**
     * Un administrador puede editar los datos de un evento si está en alguno de estos estados.
     */
    public static boolean canAdminEdit(EventStatus status) {
        return status == EventStatus.PENDING_MODERATION
                || status == EventStatus.NEEDS_CHANGES
                || status == EventStatus.APPROVED;
    }

    /**
     * Un administrador puede realizar cualquier transición de estado,
     * excepto las que involucran ERASED (estado terminal e irreversible).
     */
    public static boolean canAdminTransition(EventStatus from, EventStatus to) {
        return from != EventStatus.ERASED && to != EventStatus.ERASED;
    }
}
