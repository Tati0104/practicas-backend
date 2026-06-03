package com.avh.practicas.vinculacion.alerta.service;

import com.avh.practicas.vinculacion.alerta.dto.AlertaVista;
import com.avh.practicas.vinculacion.alerta.dto.CrearAlertaRequest;

import java.util.List;
import java.util.Map;

public interface AlertaService {

    List<AlertaVista> listarActivas(Map<String, String> contextoAutoResolucion);

    AlertaVista crear(CrearAlertaRequest request);

    AlertaVista resolver(Long id);

    void evaluarAutoResolucionGlobal(Map<String, String> contexto);
}
