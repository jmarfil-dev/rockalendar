package com.jmarfildev.rockalendar.geo.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author jmarfil
 *
 */
@Entity
@Table(name = "provinces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Province {

    @Id
    private UUID id;

    @Column(name = "ine_code", nullable = false, unique = true)
    private short ineCode;

    @Column(nullable = false, length = 80)
    private String name;
}
