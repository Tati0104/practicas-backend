package com.avh.practicas.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "mail.mode", havingValue = "stub", matchIfMissing = true)
public class MailServiceStub implements IMailService {

    @Override
    public void enviarRecuperacionPassword(String destinatario, String tokenRecuperacion) {
        log.info("[MAIL STUB] Recuperación de contraseña enviada a {} con token {}", destinatario, tokenRecuperacion);
    }
}
