package com.jmarfildev.rockalendar.geo.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.geo.api.dto.ProvinceDto;
import com.jmarfildev.rockalendar.geo.persistence.ProvinceRepository;

/**
 * @author jmarfil
 *
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProvinceQueryService {

    private final ProvinceRepository repository;

    public List<ProvinceDto> listCombo() {
        return repository.findAllForCombo();
    }
}
