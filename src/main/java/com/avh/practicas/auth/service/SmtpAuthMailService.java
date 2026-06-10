package com.avh.practicas.auth.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "mail.mode", havingValue = "smtp")
public class SmtpAuthMailService implements IMailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Value("${spring.mail.from-name:Sistema de Prácticas}")
    private String mailFromName;

    public SmtpAuthMailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviarRecuperacionPassword(String destinatario, String tokenRecuperacion) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(destinatario);
            helper.setSubject("Recuperación de contraseña");
            helper.setText(
                    "<p>Hola, recibimos una solicitud para restablecer tu contraseña.</p>" +
                            "<p>Tu token de recuperación es: <strong>" + tokenRecuperacion + "</strong></p>" +
                            "<p>Si no solicitaste esto, ignora este correo.</p>",
                    true
            );
            mailSender.send(message);
            log.info("[SMTP] Correo de recuperación enviado a {}", destinatario);
        } catch (Exception ex) {
            log.error("[SMTP] Error enviando correo de recuperación a {}: {}", destinatario, ex.getMessage());
        }
    }
}