package com.jmarfildev.rockalendar.auth.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.jmarfildev.rockalendar.auth.api.dto.AuthTokenResponse;
import com.jmarfildev.rockalendar.auth.api.dto.ForgotPasswordRequest;
import com.jmarfildev.rockalendar.auth.api.dto.LoginRequest;
import com.jmarfildev.rockalendar.auth.api.dto.RegisterRequest;
import com.jmarfildev.rockalendar.auth.api.dto.ResetPasswordRequest;
import com.jmarfildev.rockalendar.common.annotations.ApiBadRequest;
import com.jmarfildev.rockalendar.common.annotations.ApiConflict;
import com.jmarfildev.rockalendar.common.annotations.ApiUnauthorized;

/**
 * @author jmarfil
 *
 */
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public interface AuthApi {

    @PostMapping("/login")
    @Operation(summary = "Login de usuario", description = "Autentica un usuario usando email y password, y devuelve un token JWT")
    @ApiResponse(responseCode = "200",
                 description = "Login correcto",
                 content = @Content(schema = @Schema(implementation = AuthTokenResponse.class)))
    @ApiBadRequest
    @ApiUnauthorized
    AuthTokenResponse login(@Parameter(description = "Credenciales email y password",
                                       required = true) @Valid @RequestBody LoginRequest request);

    @PostMapping("/register")
    @Operation(summary = "Registro de usuario", description = "Crea un usuario nuevo y devuelve un JWT para iniciar sesión inmediatamente.")
    @ApiResponse(responseCode = "200",
                 description = "Registro correcto",
                 content = @Content(schema = @Schema(implementation = AuthTokenResponse.class)))
    @ApiBadRequest
    @ApiConflict
    AuthTokenResponse register(@Parameter(description = "Credenciales email y password",
                                          required = true) @Valid @RequestBody RegisterRequest request);

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Solicitar reseteo de contraseña",
               description = "Envía un email con un enlace de reseteo si el email existe. Siempre responde 202 para no revelar si el email está registrado.")
    @ApiResponse(responseCode = "202", description = "Solicitud procesada")
    @ApiBadRequest
    void forgotPassword(@Parameter(description = "Email del usuario",
                                                   required = true) @Valid @RequestBody ForgotPasswordRequest request);

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Restablecer contraseña",
               description = "Establece una nueva contraseña usando el token recibido por email. El token tiene validez de 15 minutos y es de un solo uso.")
    @ApiResponse(responseCode = "204", description = "Contraseña restablecida correctamente")
    @ApiBadRequest
    void resetPassword(@Parameter(description = "Token de reseteo y nueva contraseña",
                                                  required = true) @Valid @RequestBody ResetPasswordRequest request);
}
