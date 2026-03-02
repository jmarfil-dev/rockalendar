package com.jmarfildev.rockalendar.geo.api;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.common.dto.ComboItemDto;
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
    public List<ComboItemDto> listCombo() {
        return service.listCombo();
    }

}
