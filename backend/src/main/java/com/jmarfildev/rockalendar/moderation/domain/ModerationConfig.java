package com.jmarfildev.rockalendar.moderation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Parámetro de configuración de moderación automática (clave-valor).
 * Modificable desde administración sin reiniciar la aplicación.
 *
 * @author jmarfil
 */
@Entity
@Table(name = "moderation_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModerationConfig {

    @Id
    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "description", columnDefinition = "text")
    private String description;
}
