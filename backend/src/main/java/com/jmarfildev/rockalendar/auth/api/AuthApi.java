package com.jmarfildev.rockalendar.auth.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.jmarfildev.rockalendar.auth.api.dto.AuthTokenResponse;
import com.jmarfildev.rockalendar.auth.api.dto.LoginRequest;
import com.jmarfildev.rockalendar.auth.api.dto.RegisterRequest;

/**
 * @author jmarfil
 *
 */
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public interface AuthApi {

    @PostMapping("/login")
    @Operation(summary = "Login de usuario", description = "Autentica un usuario usando email y password, y devuelve un token JWT")
    @ApiResponse(responseCode = "200", description = "Login correcto",
            content = @Content(schema = @Schema(implementation = AuthTokenResponse.class)))
    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    AuthTokenResponse login(@Parameter(description = "Credenciales email y password",
            required = true) @Valid @RequestBody LoginRequest request);

    @PostMapping("/register")
    @Operation(summary = "Registro de usuario", description = "Crea un usuario nuevo y devuelve un JWT para iniciar sesión inmediatamente.")
    @ApiResponse(responseCode = "200", description = "Registro correcto",
            content = @Content(schema = @Schema(implementation = AuthTokenResponse.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "409", description = "Email ya existe")
    AuthTokenResponse register(@Parameter(description = "Credenciales email y password",
            required = true) @Valid @RequestBody RegisterRequest request);
}
