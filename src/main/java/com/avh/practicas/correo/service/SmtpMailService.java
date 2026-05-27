package com.avh.practicas.correo.service;

import com.avh.practicas.bitacora.entity.TipoAccion;
import com.avh.practicas.bitacora.service.BitacoraService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mail.mode", havingValue = "smtp")
public class SmtpMailService implements IMailService {

    private final JavaMailSender mailSender;
    private final BitacoraService bitacoraService;

    @Override
    public boolean enviar(String destinatario, String asunto, String htmlCuerpo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
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
