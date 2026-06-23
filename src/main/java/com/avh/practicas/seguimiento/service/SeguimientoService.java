package com.avh.practicas.seguimiento.service;

import com.avh.practicas.seguimiento.dto.AvanceRequest;
import com.avh.practicas.seguimiento.dto.BitacoraRequest;
import com.avh.practicas.seguimiento.dto.ObservacionRequest;
import com.avh.practicas.seguimiento.dto.TableroResponse;
import com.avh.practicas.seguimiento.entity.AlertaSistema;
import com.avh.practicas.seguimiento.entity.AvanceTutor;
import com.avh.practicas.seguimiento.entity.BitacoraEstudiante;
import com.avh.practicas.seguimiento.entity.ObservacionDocente;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Interfaz que define las operaciones de seguimiento de las prácticas.
 */
public interface SeguimientoService {

    /**
     * Registra una observación académica por parte de un docente asesor.
     */
    ObservacionDocente registrarObservacion(Long practicaId, Long docenteId, Integer corte, ObservacionRequest request);

    /**
     * Edita una observación existente si el corte no está cerrado.
     */
    ObservacionDocente editarObservacion(Long id, ObservacionRequest request);

    /**
     * Registra un avance de seguimiento por parte de un tutor empresarial.
     */
    AvanceTutor registrarAvanceTutor(Long practicaId, Long tutorId, Integer corte, AvanceRequest request);

    /**
     * Registra una entrada en la bitácora de actividades por parte del estudiante.
     */
    BitacoraEstudiante registrarBitacoraEstudiante(Long practicaId, Long estudianteId, Integer corte, BitacoraRequest request);

    /**
     * Retorna el tablero de seguimiento para coordinadores y secretarias.
     */
    List<TableroResponse> obtenerTableroSeguimiento(Long programaId, String empresa, String docente, Integer corte, String estadoSeguimiento);

    /**
     * Retorna las prácticas visibles para el usuario autenticado según su scope
     * (ESTUDIANTE ve solo la suya, COORD_PRACTICA/COORD_ACADEMICA ven su facultad, ADMIN/
     * SECRETARIA ven todas), sin exigir programaId. Los filtros son opcionales y acotan
     * el resultado ya cargado.
     */
    List<TableroResponse> obtenerPracticasSeguimiento(
            String busqueda,
            Long programaId,
            String estadoSeguimiento,
            String estadoPractica);

    /**
     * Obtiene todas las observaciones asociadas a una práctica.
     */
    List<ObservacionDocente> obtenerObservacionesPorPractica(Long practicaId);

    /**
     * Obtiene todos los avances de tutor asociados a una práctica.
     */
    List<AvanceTutor> obtenerAvancesPorPractica(Long practicaId);

    /**
     * Obtiene la bitácora de actividades asociada a una práctica.
     */
    List<BitacoraEstudiante> obtenerBitacorasPorPractica(Long practicaId);

    /**
     * Obtiene el detalle unificado de una práctica.
     */
    com.avh.practicas.seguimiento.dto.PracticaDetalleResponse obtenerDetallePractica(Long practicaId);

    /**
     * Adjunta un archivo de soporte a una entrada de bitácora ya creada.
     * Guarda el archivo en disco y actualiza nombre_archivo / url_archivo en la entidad.
     */
    BitacoraEstudiante adjuntarArchivoBitacora(Long bitacoraId, MultipartFile archivo);

    /**
     * Devuelve el recurso (stream del archivo) para descarga.
     */
    Resource descargarArchivoBitacora(Long bitacoraId);

    /**
     * Devuelve el nombre original del archivo adjunto de la bitácora.
     */
    String nombreArchivoBitacora(Long bitacoraId);

    /**
     * Obtiene todas las alertas activas del sistema.
     */
    List<AlertaSistema> obtenerAlertasActivas();
}
