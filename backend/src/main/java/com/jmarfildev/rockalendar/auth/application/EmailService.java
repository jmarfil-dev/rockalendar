package com.jmarfildev.rockalendar.auth.application;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jmarfil
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String BUNDLE = "email_messages";
    // Idioma por defecto si el usuario no tiene preferencia guardada
    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("es");

    private final JavaMailSender mailSender;

    @Value("${rockalendar.email.from}")
    private String fromAddress;

    public void sendContactEmail(String fromName, String fromEmail, String message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromAddress);
        mail.setTo("ruido@rockalendar.es");
        mail.setReplyTo(fromEmail);
        mail.setSubject("[Contacto] " + (fromName != null && !fromName.isBlank() ? fromName : fromEmail));
        mail.setText("De: " + (fromName != null && !fromName.isBlank() ? fromName + " <" + fromEmail + ">" : fromEmail)
                + "\n\n" + message);

        mailSender.send(mail);
        log.info("contact email sent from={}", fromEmail);
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink, String preferredLanguage) {
        Locale locale = resolveLocale(preferredLanguage);
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, locale);

        String subject = bundle.getString("password.reset.subject");
        // El cuerpo usa {0} como placeholder para el enlace (MessageFormat)
        String body = MessageFormat.format(bundle.getString("password.reset.body"), resetLink);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        log.info("password reset email sent to {} locale={}", toEmail, locale);
    }

    private static Locale resolveLocale(String preferredLanguage) {
        if (preferredLanguage == null || preferredLanguage.isBlank()) {
            return DEFAULT_LOCALE;
        }
        Locale candidate = Locale.forLanguageTag(preferredLanguage);
        // Solo soportamos es/en; cualquier otra cosa cae a español
        return switch (candidate.getLanguage()) {
            case "en" -> Locale.ENGLISH;
            default -> DEFAULT_LOCALE;
        };
    }
}
