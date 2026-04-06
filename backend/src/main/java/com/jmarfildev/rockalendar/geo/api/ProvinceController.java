package com.jmarfildev.rockalendar.geo.api;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.geo.api.dto.ProvinceDto;
import com.jmarfildev.rockalendar.geo.application.ProvinceQueryService;

/**
 * @author jmarfil
 *
 */
@RestController
@RequiredArgsConstructor
public class ProvinceController implements ProvinceApi {

    private final ProvinceQueryService service;

    @Override
    public List<ProvinceDto> listCombo() {
        return service.listCombo();
    }

}
