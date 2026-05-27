package com.avh.practicas.correo.service;

import com.avh.practicas.correo.dto.PlantillaCorreoRequest;
import com.avh.practicas.correo.entity.PlantillaCorreo;
import com.avh.practicas.correo.entity.TipoEventoCorreo;
import com.avh.practicas.correo.repository.PlantillaCorreoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlantillaCorreoService {

    private final PlantillaCorreoRepository repository;

    @Transactional(readOnly = true)
    public PlantillaCorreo obtener(TipoEventoCorreo tipoEvento) {
        return repository.findByNombre(tipoEvento.name())
                .orElseThrow(() -> new IllegalArgumentException("No existe plantilla para el evento " + tipoEvento));
    }

    @Transactional
    public PlantillaCorreo guardar(TipoEventoCorreo tipoEvento, PlantillaCorreoRequest request) {
        PlantillaCorreo plantilla = repository.findByNombre(tipoEvento.name())
                .orElseGet(() -> PlantillaCorreo.builder().nombre(tipoEvento.name()).build());

        plantilla.setAsunto(request.asunto());
        plantilla.setCuerpo(request.cuerpo());
        plantilla.setActiva(request.activa());

        return repository.save(plantilla);
    }

    @Transactional(readOnly = true)
    public String procesarTemplate(TipoEventoCorreo tipoEvento, Map<String, String> variables) {
        String cuerpo = obtener(tipoEvento).getCuerpo();
        return procesarTexto(cuerpo, variables);
    }

    public String procesarTexto(String cuerpo, Map<String, String> variables) {
        String resultado = cuerpo;
        if (variables == null) {
            return resultado;
        }

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            resultado = resultado.replace("{{" + entry.getKey() + "}}", entry.getValue() == null ? "" : entry.getValue());
        }
        return resultado;
    }
}
