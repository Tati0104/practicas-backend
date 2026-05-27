package com.avh.practicas.correo.service;

import com.avh.practicas.bitacora.entity.TipoAccion;
import com.avh.practicas.bitacora.service.BitacoraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mail.mode", havingValue = "stub", matchIfMissing = true)
public class StubMailService implements IMailService {

    private final BitacoraService bitacoraService;

    @Override
    public boolean enviar(String destinatario, String asunto, String htmlCuerpo) {
        log.info("[STUB MAIL] Para: {} | Asunto: {} | Cuerpo: {}", destinatario, asunto, htmlCuerpo);
        bitacoraService.registrar(null, "CORREO", TipoAccion.ENVIO_CORREO, null, null,
                "Correo simulado enviado a " + destinatario + " con asunto: " + asunto);
        return true;
    }
}
