package com.avh.practicas.vinculacion.service;

import com.avh.practicas.asignacion.entity.EstadoAsignacion;
import com.avh.practicas.vinculacion.dto.ConfirmarVinculacionRequest;
import com.avh.practicas.vinculacion.dto.DocumentoCargadoResponse;
import com.avh.practicas.vinculacion.dto.DocumentosAsignacionResponse;
import com.avh.practicas.vinculacion.dto.DocumentosPorCategoriaResponse;
import com.avh.practicas.vinculacion.dto.VinculacionListadoResponse;
import com.avh.practicas.vinculacion.entity.CategoriaDocumento;
import com.avh.practicas.vinculacion.entity.RolFirmaConvenio;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface VinculacionService {

    Page<VinculacionListadoResponse> listar(
            String busqueda,
            Long programaId,
            Long empresaId,
            EstadoAsignacion estado,
            Pageable pageable
    );

    DocumentosAsignacionResponse obtenerDocumentosAsignacion(Long asignacionId);

    DocumentoCargadoResponse cargarDocumento(Long asignacionId, CategoriaDocumento categoria, MultipartFile archivo);

    DocumentoCargadoResponse cargarCarta(Long asignacionId, MultipartFile archivo);

    DocumentoCargadoResponse cargarConvenio(Long asignacionId, MultipartFile archivo);

    Resource descargarDocumento(Long documentoId);

    String nombreDescargaDocumento(Long documentoId);

    void confirmarFirma(Long convenioId, RolFirmaConvenio rol);

    void confirmarVinculacion(Long practicaId, ConfirmarVinculacionRequest request);

    DocumentosPorCategoriaResponse listarDocumentosPorPractica(Long practicaId);

    com.avh.practicas.estudiante.entity.Estudiante obtenerEstudiantePorPractica(Long practicaId);
    void asignarDocenteAsesor(Long asignacionId, Long docenteAsesorId);
}
