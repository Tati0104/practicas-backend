package com.avh.practicas.correo.service;

import com.avh.practicas.bitacora.entity.TipoAccion;
import com.avh.practicas.bitacora.service.BitacoraService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "mail.mode", havingValue = "smtp")
public class SmtpMailService implements IMailService {

    private final JavaMailSender mailSender;
    private final BitacoraService bitacoraService;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Value("${spring.mail.from-name:Sistema de Prácticas}")
    private String mailFromName;

    public SmtpMailService(JavaMailSender mailSender, BitacoraService bitacoraService) {
        this.mailSender = mailSender;
        this.bitacoraService = bitacoraService;
    }

    @Override
    public boolean enviar(String destinatario, String asunto, String htmlCuerpo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(htmlCuerpo, true);
            mailSender.send(message);

            bitacoraService.registrar(null, "CORREO", TipoAccion.ENVIO_CORREO, null, null,
                    "Correo SMTP enviado a " + destinatario + " con asunto: " + asunto);
            return true;
        } catch (Exception ex) {
            bitacoraService.registrar(null, "CORREO", TipoAccion.ENVIO_CORREO, null, null,
                    "Fallo al enviar correo SMTP a " + destinatario + ": " + ex.getMessage());
            return false;
        }
    }
}
