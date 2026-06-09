package com.avh.practicas.estudiante.adapter;

import com.avh.practicas.estudiante.dto.ErrorValidacion;
import com.avh.practicas.estudiante.dto.ResultadoImportacion;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ImportadorEstudiantes {
    ResultadoImportacion importar(byte[] archivoBytes);
    List<ErrorValidacion> validar(byte[] archivoBytes);
    CompletableFuture<ResultadoImportacion> importarAsync(byte[] archivoBytes);
}
