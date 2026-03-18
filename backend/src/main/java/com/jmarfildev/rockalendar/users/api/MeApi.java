package com.jmarfildev.rockalendar.users.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.jmarfildev.rockalendar.common.annotations.ApiConflict;
import com.jmarfildev.rockalendar.common.annotations.ApiUnauthorized;
import com.jmarfildev.rockalendar.users.api.dto.MeDto;

/**
 * @author jmarfil
 */
@RequestMapping("/api/me")
@Tag(name = "Me", description = "Perfil del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public interface MeApi {

    @GetMapping
    @Operation(summary = "Obtener mi perfil",
               description = "Devuelve los datos del usuario autenticado, incluyendo si es elegible para solicitar el ascenso a moderador.")
    @ApiResponse(responseCode = "200", description = "Perfil del usuario")
    @ApiUnauthorized
    MeDto getMe();

    @PostMapping("/promotion-request")
    @Operation(summary = "Solicitar ascenso a moderador",
               description = "Si el usuario cumple todos los requisitos internos, el rol cambia a MODERATOR de forma inmediata.")
    @ApiResponse(responseCode = "200", description = "Ascenso realizado correctamente")
    @ApiUnauthorized
    @ApiConflict
    MeDto requestPromotion();
}
