package com.jmarfildev.rockalendar.geo.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jmarfildev.rockalendar.common.dto.ComboItemDto;
import com.jmarfildev.rockalendar.geo.domain.Province;

/**
 * @author jmarfil
 *
 */
public interface ProvinceRepository extends JpaRepository<Province, UUID> {
    Optional<Province> findByIneCode(short ineCode);

    @Query("""
           SELECT new com.jmarfildev.rockalendar.common.dto.ComboItemDto(p.id, p.name)
           FROM Province p
           ORDER BY p.name
           """)
    List<ComboItemDto> findAllForCombo();
}
