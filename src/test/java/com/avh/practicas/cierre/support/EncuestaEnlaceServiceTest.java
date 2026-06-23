package com.avh.practicas.cierre.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EncuestaEnlaceServiceTest {

    @Test
    void buildEnlaceEncuesta_BaseSinSlashAgregaRutaDeEvaluaciones() {
        EncuestaEnlaceService service = new EncuestaEnlaceService("http://localhost:5173");

        assertEquals("http://localhost:5173/evaluaciones/5", service.buildEnlaceEncuesta(5L));
    }

    @Test
    void buildEnlaceEncuesta_BaseConSlashEvitaDobleSeparador() {
        EncuestaEnlaceService service = new EncuestaEnlaceService("http://localhost:5173/");

        assertEquals("http://localhost:5173/evaluaciones/5", service.buildEnlaceEncuesta(5L));
    }

    @Test
    void buildEnlaceEncuesta_PracticaNulaRetornaBaseOriginal() {
        EncuestaEnlaceService service = new EncuestaEnlaceService("http://localhost:5173/");

        assertEquals("http://localhost:5173/", service.buildEnlaceEncuesta(null));
    }
}
