package com.jmarfildev.rockalendar.contact.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.auth.application.EmailService;
import com.jmarfildev.rockalendar.contact.api.dto.ContactRequest;

/**
 * @author jmarfil
 */
@RestController
@RequiredArgsConstructor
public class ContactController implements ContactApi {

    private final EmailService emailService;

    @Override
    public ResponseEntity<Void> send(ContactRequest request) {
        emailService.sendContactEmail(request.name(), request.email(), request.message());
        return ResponseEntity.noContent().build();
    }
}
