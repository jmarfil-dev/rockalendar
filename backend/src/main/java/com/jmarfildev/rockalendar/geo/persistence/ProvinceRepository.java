package com.jmarfildev.rockalendar.geo.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jmarfildev.rockalendar.geo.api.dto.ProvinceDto;
import com.jmarfildev.rockalendar.geo.domain.Province;

/**
 * @author jmarfil
 *
 */
public interface ProvinceRepository extends JpaRepository<Province, Short> {

    @Query("""
           SELECT new com.jmarfildev.rockalendar.geo.api.dto.ProvinceDto(p.ineCode, p.name)
           FROM Province p
           ORDER BY p.name
           """)
    List<ProvinceDto> findAllForCombo();
}
