package com.avh.practicas.correo.factory;

import com.avh.practicas.correo.service.IMailService;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.shared.evento.EventoSistema;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
public class NotificacionVacanteFactory extends NotificacionFactory {

    private final TutorEmpresarialRepository tutorRepository;

    public NotificacionVacanteFactory(
            IMailService mailService,
            TutorEmpresarialRepository tutorRepository) {
        super(mailService);
        this.tutorRepository = tutorRepository;
    }

    @Override
    public Notificacion crearNotificacion(EventoSistema evento) {
        String correoDestino = resolverCorreoDestino(evento);
        String cargo = String.valueOf(evento.getDatos().getOrDefault("cargo", "vacante"));
        String estado = String.valueOf(evento.getDatos().getOrDefault("estado", evento.getTipo().name()));

        List<String> destinatarios = correoDestino != null ? List.of(correoDestino) : Collections.emptyList();

        return new NotificacionBase(
                evento.getTipo().name(),
                "<p>La vacante <b>" + cargo + "</b> cambió al estado <b>" + estado + "</b>.</p>",
                "Actualización de vacante",
                destinatarios,
                LocalDateTime.now()
        );
    }

    private String resolverCorreoDestino(EventoSistema evento) {
        String correoEmpresa = obtenerTexto(evento, "correoEmpresa");
        if (correoEmpresa != null) {
            return correoEmpresa;
        }

        Long empresaId = obtenerLong(evento, "empresaId");
        if (empresaId == null) {
            return null;
        }

        return tutorRepository.findByEmpresaIdAndActivoTrue(empresaId).stream()
                .map(TutorEmpresarial::getCorreo)
                .filter(correo -> correo != null && !correo.isBlank())
                .findFirst()
                .orElse(null);
    }

    private String obtenerTexto(EventoSistema evento, String clave) {
        Object valor = evento.getDatos().get(clave);
        if (valor == null) {
            return null;
        }
        String texto = String.valueOf(valor).trim();
        return texto.isBlank() ? null : texto;
    }

    private Long obtenerLong(EventoSistema evento, String clave) {
        Object valor = evento.getDatos().get(clave);
        if (valor instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}
