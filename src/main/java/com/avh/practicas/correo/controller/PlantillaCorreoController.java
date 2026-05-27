package com.avh.practicas.correo.controller;

import com.avh.practicas.correo.dto.PlantillaCorreoRequest;
import com.avh.practicas.correo.dto.PreviewPlantillaRequest;
import com.avh.practicas.correo.entity.PlantillaCorreo;
import com.avh.practicas.correo.entity.TipoEventoCorreo;
import com.avh.practicas.correo.service.PlantillaCorreoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/plantillas")
@RequiredArgsConstructor
public class PlantillaCorreoController {

    private final PlantillaCorreoService service;

    @GetMapping("/{tipoEvento}")
    public PlantillaCorreo obtener(@PathVariable TipoEventoCorreo tipoEvento) {
        return service.obtener(tipoEvento);
    }

    @PutMapping("/{tipoEvento}")
    public PlantillaCorreo guardar(@PathVariable TipoEventoCorreo tipoEvento,
                                   @Valid @RequestBody PlantillaCorreoRequest request) {
        return service.guardar(tipoEvento, request);
    }

    @PostMapping("/{tipoEvento}/preview")
    public Map<String, String> preview(@PathVariable TipoEventoCorreo tipoEvento,
                                       @RequestBody PreviewPlantillaRequest request) {
        return Map.of("html", service.procesarTemplate(tipoEvento, request.variables()));
    }
}
