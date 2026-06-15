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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import com.jmarfildev.rockalendar.auth.api.dto.AuthSessionResponse;
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
    @Operation(summary = "Login de usuario",
               description = "Autentica un usuario con email y password. Establece la sesión mediante una cookie HttpOnly y devuelve la fecha de expiración.")
    @ApiResponse(responseCode = "200",
                 description = "Login correcto",
                 content = @Content(schema = @Schema(implementation = AuthSessionResponse.class)))
    @ApiBadRequest
    @ApiUnauthorized
    AuthSessionResponse login(@Parameter(description = "Credenciales email y password", required = true) @Valid @RequestBody LoginRequest request,
                              HttpServletResponse response);

    @PostMapping("/register")
    @Operation(summary = "Registro de usuario",
               description = "Crea un usuario nuevo e inicia sesión inmediatamente mediante una cookie HttpOnly y devuelve la fecha de expiración.")
    @ApiResponse(responseCode = "200",
                 description = "Registro correcto",
                 content = @Content(schema = @Schema(implementation = AuthSessionResponse.class)))
    @ApiBadRequest
    @ApiConflict
    AuthSessionResponse register(@Parameter(description = "Credenciales email y password", required = true) @Valid @RequestBody RegisterRequest request,
                                 HttpServletResponse response);

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Logout", description = "Cierra la sesión eliminando la cookie de autenticación.")
    @ApiResponse(responseCode = "204", description = "Sesión cerrada correctamente")
    void logout(HttpServletResponse response);

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Solicitar reseteo de contraseña",
               description = "Envía un email con un enlace de reseteo si el email existe. Siempre responde 202 para no revelar si el email está registrado.")
    @ApiResponse(responseCode = "202", description = "Solicitud procesada")
    @ApiBadRequest
    void forgotPassword(@Parameter(description = "Email del usuario", required = true) @Valid @RequestBody ForgotPasswordRequest request);

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Restablecer contraseña",
               description = "Establece una nueva contraseña usando el token recibido por email. El token tiene validez de 15 minutos y es de un solo uso.")
    @ApiResponse(responseCode = "204", description = "Contraseña restablecida correctamente")
    @ApiBadRequest
    void resetPassword(@Parameter(description = "Token de reseteo y nueva contraseña", required = true) @Valid @RequestBody ResetPasswordRequest request);
}
