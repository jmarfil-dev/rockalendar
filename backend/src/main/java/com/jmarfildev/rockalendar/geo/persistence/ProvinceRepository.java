package com.jmarfildev.rockalendar.geo.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jmarfildev.rockalendar.geo.domain.Province;

/**
 * @author jmarfil
 *
 */
public interface ProvinceRepository extends JpaRepository<Province, UUID> {
    Optional<Province> findByIneCode(short ineCode);
}
