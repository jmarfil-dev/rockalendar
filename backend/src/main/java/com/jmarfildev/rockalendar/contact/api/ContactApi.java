package com.jmarfildev.rockalendar.contact.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.jmarfildev.rockalendar.contact.api.dto.ContactRequest;

/**
 * @author jmarfil
 */
@RequestMapping("/api/contact")
@Tag(name = "Contact", description = "Formulario de contacto público")
public interface ContactApi {

    @PostMapping
    @Operation(summary = "Envía un mensaje de contacto")
    @ApiResponse(responseCode = "204", description = "Mensaje enviado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    ResponseEntity<Void> send(@Valid @RequestBody ContactRequest request);
}
