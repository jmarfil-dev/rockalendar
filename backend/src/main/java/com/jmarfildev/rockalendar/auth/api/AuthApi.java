package com.jmarfildev.rockalendar.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.jmarfildev.rockalendar.auth.api.dto.LoginRequest;
import com.jmarfildev.rockalendar.auth.api.dto.LoginResponse;

/**
 * @author jmarfil
 *
 */
@Tag(name = "Authentication")
public interface AuthApi {

    @Operation(summary = "Login de usuario",
            description = "Autentica un usuario usando email y password, y devuelve un token JWT")
    @ApiResponse(responseCode = "200", description = "Login correcto",
            content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    LoginResponse login(@Parameter(description = "Credenciales email y password", required = true) LoginRequest request);
}
