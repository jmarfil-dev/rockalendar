package com.jmarfildev.rockalendar.moderation.domain;

/**
 * @author jmarfil
 *
 */
public enum ActionType {
    APPROVE,
    REJECT,
    HIDE,
    REQUEST_CHANGES,
    AUTO_REJECT,
    /** Un moderador ha editado los datos del evento mientras estaba en PENDING_MODERATION. */
    MODERATOR_EDITED,
    /** Un administrador ha forzado un cambio de estado fuera del flujo normal. */
    ADMIN_STATE_OVERRIDE,
    /** El propietario ha editado el evento mientras estaba en PENDING_MODERATION. */
    OWNER_EDITED_PENDING;
}
