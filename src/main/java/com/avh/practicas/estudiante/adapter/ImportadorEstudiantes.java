package com.avh.practicas.estudiante.adapter;

import com.avh.practicas.estudiante.dto.ErrorValidacion;
import com.avh.practicas.estudiante.dto.ResultadoImportacion;

import java.util.List;

public interface ImportadorEstudiantes {
    ResultadoImportacion importar(byte[] archivoBytes);
    List<ErrorValidacion> validar(byte[] archivoBytes);
}
