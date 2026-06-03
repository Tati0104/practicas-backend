package com.avh.practicas.vinculacion.service;

import com.avh.practicas.vinculacion.dto.ConfirmarVinculacionRequest;
import com.avh.practicas.vinculacion.dto.DocumentoCargadoResponse;
import com.avh.practicas.vinculacion.dto.DocumentosPorCategoriaResponse;
import com.avh.practicas.vinculacion.entity.RolFirmaConvenio;
import org.springframework.web.multipart.MultipartFile;

public interface VinculacionService {

    DocumentoCargadoResponse cargarCarta(Long asignacionId, MultipartFile archivo);

    DocumentoCargadoResponse cargarConvenio(Long asignacionId, MultipartFile archivo);

    void confirmarFirma(Long convenioId, RolFirmaConvenio rol);

    void confirmarVinculacion(Long practicaId, ConfirmarVinculacionRequest request);

    DocumentosPorCategoriaResponse listarDocumentosPorPractica(Long practicaId);
}
