package com.jmarfildev.rockalendar.users.api;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.users.api.dto.MeDto;
import com.jmarfildev.rockalendar.users.application.MeCommandService;
import com.jmarfildev.rockalendar.users.application.MeQueryService;

/**
 * @author jmarfil
 */
@RestController
@RequiredArgsConstructor
public class MeController implements MeApi {

    private final MeQueryService queryService;
    private final MeCommandService commandService;

    @Override
    public MeDto getMe() {
        return queryService.getMe();
    }

    @Override
    public MeDto requestPromotion() {
        return commandService.requestPromotion();
    }
}
