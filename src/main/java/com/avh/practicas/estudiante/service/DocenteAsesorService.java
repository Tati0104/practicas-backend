package com.avh.practicas.estudiante.service;

import com.avh.practicas.estudiante.dto.DocenteAsesorRequest;
import com.avh.practicas.estudiante.dto.DocenteAsesorResponse;
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.shared.evento.EventoSistema;
import com.avh.practicas.shared.evento.NotificadorEventos;
import com.avh.practicas.shared.evento.TipoEventoSistema;
import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.usuario.service.CorreoPersonaService;
import com.avh.practicas.usuario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocenteAsesorService {

    private final DocenteAsesorRepository repository;
    private final UsuarioService usuarioService;
    private final CorreoPersonaService correoPersonaService;
    private final NotificadorEventos notificadorEventos;

    @Transactional
    public DocenteAsesorResponse registrar(DocenteAsesorRequest request) {
        String correo = correoPersonaService.normalizar(request.correo());
        correoPersonaService.validarCorreoDisponible(correo, CorreoPersonaService.Exclusiones.ninguna());

        Usuario usuario = usuarioService.crearUsuarioDocenteAsesor(request.nombreCompleto(), correo);

        DocenteAsesor docente = DocenteAsesor.builder()
                .usuario(usuario)
                .nombre(request.nombreCompleto().trim())
                .correo(correo)
                .telefono(request.telefono())
                .programaId(request.programaId())
                .areaConocimiento(request.areaConocimiento())
                .activo(true)
                .build();

        DocenteAsesor guardado = repository.save(docente);
        notificarSeguro(TipoEventoSistema.DOCENTE_ASESOR_CREADO, guardado);
        return DocenteAsesorResponse.desdeEntidad(guardado);
    }

    @Transactional
    public DocenteAsesorResponse editar(Long id, DocenteAsesorRequest request) {
        DocenteAsesor docente = obtenerEntidad(id);
        String correo = correoPersonaService.normalizar(request.correo());

        correoPersonaService.validarCorreoDisponible(
                correo,
                new CorreoPersonaService.Exclusiones(
                        docente.getUsuario() != null ? docente.getUsuario().getId() : null,
                        docente.getId(),
                        null,
                        null
                )
        );

        docente.setNombre(request.nombreCompleto().trim());
        docente.setCorreo(correo);
        docente.setTelefono(request.telefono());
        docente.setProgramaId(request.programaId());
        docente.setAreaConocimiento(request.areaConocimiento());

        if (docente.getUsuario() != null) {
            usuarioService.sincronizarPerfil(
                    docente.getUsuario(),
                    request.nombreCompleto(),
                    correo
            );
        }

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
    public List<DocenteAsesorResponse> listarTodos() {
        return repository.findAll()
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
