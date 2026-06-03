package com.avh.practicas.estudiante.service;

import com.avh.practicas.estudiante.dto.DocenteAsesorRequest;
import com.avh.practicas.estudiante.dto.DocenteAsesorResponse;
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.NotificadorEventos;
import com.avh.practicas.shared.evento.TipoEventoSistema;
import com.avh.practicas.usuario.entity.Usuario;
import com.avh.practicas.usuario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Servicio KBM S2 para administrar docentes asesores.
 * Se encarga de registrar, editar, activar e inactivar docentes vinculados a un programa.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocenteAsesorService {

    private final DocenteAsesorRepository repository;
    private final UsuarioService usuarioService;
    private final NotificadorEventos notificadorEventos;

    @Transactional
    public DocenteAsesorResponse registrar(DocenteAsesorRequest request) {
        // Validacion de negocio: el correo del docente asesor no se puede repetir.
        if (repository.existsByCorreo(request.correo())) {
            throw new IllegalArgumentException("Ya existe un docente asesor con este correo");
        }

        // Se crea o actualiza el usuario asociado con rol DOCENTE_ASESOR.
        Usuario usuario = usuarioService.crearUsuarioDocenteAsesor(
                request.nombreCompleto(),
                request.correo()
        );

        DocenteAsesor docente = DocenteAsesor.builder()
                .usuario(usuario)
                .nombre(request.nombreCompleto())
                .correo(request.correo())
                .telefono(request.telefono())
                .programaId(request.programaId())
                .areaConocimiento(request.areaConocimiento())
                .activo(true)
                .build();

        DocenteAsesor guardado = repository.save(docente);
        // Observer: se notifica la creacion, pero sin impedir el registro si el observador falla.
        notificarSeguro(TipoEventoSistema.DOCENTE_ASESOR_CREADO, guardado);
        return DocenteAsesorResponse.desdeEntidad(guardado);
    }

    @Transactional
    public DocenteAsesorResponse editar(Long id, DocenteAsesorRequest request) {
        DocenteAsesor docente = obtenerEntidad(id);

        docente.setNombre(request.nombreCompleto());
        docente.setCorreo(request.correo());
        docente.setTelefono(request.telefono());
        docente.setProgramaId(request.programaId());
        docente.setAreaConocimiento(request.areaConocimiento());

        DocenteAsesor guardado = repository.save(docente);

        return DocenteAsesorResponse.desdeEntidad(guardado);
    }

    @Transactional
    public DocenteAsesorResponse activar(Long id) {
        DocenteAsesor docente = obtenerEntidad(id);

        docente.setActivo(true);

        if (docente.getUsuario() != null) {
            docente.getUsuario().setActivo(true);
        }

        DocenteAsesor guardado = repository.save(docente);
        notificarSeguro(TipoEventoSistema.DOCENTE_ASESOR_ACTIVADO, guardado);

        return DocenteAsesorResponse.desdeEntidad(guardado);
    }

    @Transactional
    public DocenteAsesorResponse inactivar(Long id) {
        DocenteAsesor docente = obtenerEntidad(id);

        // Integración futura: validar que no tenga estudiantes activos asignados.
        docente.setActivo(false);

        if (docente.getUsuario() != null) {
            docente.getUsuario().setActivo(false);
        }

        DocenteAsesor guardado = repository.save(docente);
        notificarSeguro(TipoEventoSistema.DOCENTE_ASESOR_INACTIVADO, guardado);

        return DocenteAsesorResponse.desdeEntidad(guardado);
    }

    @Transactional(readOnly = true)
    public List<DocenteAsesorResponse> listarPorPrograma(Long programaId) {
        return repository.findByProgramaIdAndActivoTrue(programaId)
                .stream()
                .map(DocenteAsesorResponse::desdeEntidad)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocenteAsesorResponse> listarActivos() {
        return repository.findByActivoTrue()
                .stream()
                .map(DocenteAsesorResponse::desdeEntidad)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocenteAsesorResponse obtener(Long id) {
        return DocenteAsesorResponse.desdeEntidad(obtenerEntidad(id));
    }

    private DocenteAsesor obtenerEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Docente asesor no encontrado"));
    }

    /**
     * Proteccion del flujo principal: si falla la notificacion, no se revierte la creacion del docente.
     */
    private void notificarSeguro(TipoEventoSistema tipo, DocenteAsesor docente) {
        try {
            notificar(tipo, docente);
        } catch (Exception e) {
            log.warn(
                    "No se pudo notificar el evento {} para el docente asesor con id {}. Motivo: {}",
                    tipo,
                    docente.getId(),
                    e.getMessage()
            );
        }
    }

    private void notificar(TipoEventoSistema tipo, DocenteAsesor docente) {
        notificadorEventos.notificar(EventoSistema.crear(
                tipo,
                docente.getUsuario() == null ? null : docente.getUsuario().getId(),
                "DOCENTE_ASESOR",
                docente.getId(),
                Map.of(
                        "nombre", docente.getNombreCompleto(),
                        "correo", docente.getCorreo(),
                        "programaId", docente.getProgramaId(),
                        "areaConocimiento", docente.getAreaConocimiento() == null ? "" : docente.getAreaConocimiento()
                )
        ));
    }
}
