package com.avh.practicas.cierre.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.cierre.entity.Encuesta;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import com.avh.practicas.cierre.repository.EncuestaRepository;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.exception.AccesoNoAutorizadoException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.shared.scope.ScopePracticaResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Proxy de seguridad y control para el servicio de encuestas.
 * Mantiene la uniformidad del diseno de capas de la aplicacion.
 */
@Service
@Primary
public class EncuestaServiceProxy implements EncuestaService {

    private final EncuestaService realService;
    private final AuthUsuarioRepository usuarioRepository;
    private final EncuestaRepository encuestaRepository;
    private final InstanciaPracticaRepository practicaRepository;
    private final EstudianteRepository estudianteRepository;
    private final TutorEmpresarialRepository tutorRepository;
    private final DocenteAsesorRepository docenteRepository;
    private final ScopePracticaResolver scopePracticaResolver;

    public EncuestaServiceProxy(
            @Qualifier("encuestaServiceImpl") EncuestaService realService,
            AuthUsuarioRepository usuarioRepository,
            EncuestaRepository encuestaRepository,
            InstanciaPracticaRepository practicaRepository,
            EstudianteRepository estudianteRepository,
            TutorEmpresarialRepository tutorRepository,
            DocenteAsesorRepository docenteRepository,
            ScopePracticaResolver scopePracticaResolver) {
        this.realService = realService;
        this.usuarioRepository = usuarioRepository;
        this.encuestaRepository = encuestaRepository;
        this.practicaRepository = practicaRepository;
        this.estudianteRepository = estudianteRepository;
        this.tutorRepository = tutorRepository;
        this.docenteRepository = docenteRepository;
        this.scopePracticaResolver = scopePracticaResolver;
    }

    @Override
    public Encuesta crearEncuestaPendiente(Long practicaId, TipoEncuesta tipo) {
        validarGestionEncuesta(practicaId);
        return realService.crearEncuestaPendiente(practicaId, tipo);
    }

    @Override
    public Encuesta guardarBorrador(Long encuestaId, String respuestasJson) {
        Encuesta encuesta = obtenerEncuesta(encuestaId);
        validarRespuestaEncuesta(encuesta);
        return realService.guardarBorrador(encuestaId, respuestasJson);
    }

    @Override
    public Encuesta enviar(Long encuestaId) {
        Encuesta encuesta = obtenerEncuesta(encuestaId);
        validarRespuestaEncuesta(encuesta);
        return realService.enviar(encuestaId);
    }

    @Override
    public void enviarRecordatorio(Long practicaId, TipoEncuesta tipo) {
        validarGestionEncuesta(practicaId);
        realService.enviarRecordatorio(practicaId, tipo);
    }

    @Override
    public void validarRecordatorioDiario(Long practicaId, TipoEncuesta tipo) {
        validarGestionEncuesta(practicaId);
        realService.validarRecordatorioDiario(practicaId, tipo);
    }

    @Override
    public void registrarRecordatorioEnviado(Long practicaId, TipoEncuesta tipo) {
        validarGestionEncuesta(practicaId);
        realService.registrarRecordatorioEnviado(practicaId, tipo);
    }

    @Override
    public boolean isCompleta(Long practicaId, TipoEncuesta tipo) {
        validarConsultaEncuesta(practicaId, tipo);
        return realService.isCompleta(practicaId, tipo);
    }

    @Override
    public Optional<Encuesta> obtenerPorPracticaYTipo(Long practicaId, TipoEncuesta tipo) {
        validarConsultaEncuesta(practicaId, tipo);
        Optional<Encuesta> encuesta = realService.obtenerPorPracticaYTipo(practicaId, tipo);
        if (encuesta.isPresent()) {
            return encuesta;
        }

        Usuario usuario = obtenerUsuarioActual();
        if (usuario.getRol() == Rol.TUTOR_EMPRESARIAL
                && tipo == TipoEncuesta.TUTOR
                && esTutorAsignado(usuario, obtenerPractica(practicaId))) {
            return Optional.of(realService.crearEncuestaPendiente(practicaId, tipo));
        }
        if (usuario.getRol() == Rol.ESTUDIANTE
                && tipo == TipoEncuesta.ESTUDIANTE
                && esEstudiantePropio(usuario, obtenerPractica(practicaId))) {
            return Optional.of(realService.crearEncuestaPendiente(practicaId, tipo));
        }

        return Optional.empty();
    }

    @Override
    public Encuesta enviarInvitacion(Long practicaId, TipoEncuesta tipo) {
        validarGestionEncuesta(practicaId);
        return realService.enviarInvitacion(practicaId, tipo);
    }

    private void validarGestionEncuesta(Long practicaId) {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario.getRol() != Rol.ADMIN && usuario.getRol() != Rol.COORD_PRACTICA) {
            throw new AccesoNoAutorizadoException("Acceso denegado: no puede gestionar encuestas.");
        }
        validarVisibleParaAdministrativo(practicaId);
    }

    private void validarConsultaEncuesta(Long practicaId, TipoEncuesta tipo) {
        Usuario usuario = obtenerUsuarioActual();
        InstanciaPractica practica = obtenerPractica(practicaId);

        if (usuario.getRol() == Rol.ADMIN || usuario.getRol() == Rol.COORD_PRACTICA || usuario.getRol() == Rol.SECRETARIA) {
            validarVisibleParaAdministrativo(practica);
            return;
        }

        if (usuario.getRol() == Rol.ESTUDIANTE && tipo == TipoEncuesta.ESTUDIANTE && esEstudiantePropio(usuario, practica)) {
            return;
        }

        if (usuario.getRol() == Rol.TUTOR_EMPRESARIAL && tipo == TipoEncuesta.TUTOR && esTutorAsignado(usuario, practica)) {
            return;
        }

        if (usuario.getRol() == Rol.DOCENTE_ASESOR && esDocenteAsignado(usuario, practica)) {
            return;
        }

        throw new AccesoNoAutorizadoException("Acceso denegado: encuesta fuera de su alcance.");
    }

    private void validarRespuestaEncuesta(Encuesta encuesta) {
        Usuario usuario = obtenerUsuarioActual();
        InstanciaPractica practica = obtenerPractica(encuesta.getInstanciaPractica().getId());

        if (usuario.getRol() == Rol.ESTUDIANTE
                && encuesta.getTipo() == TipoEncuesta.ESTUDIANTE
                && esEstudiantePropio(usuario, practica)) {
            return;
        }

        if (usuario.getRol() == Rol.TUTOR_EMPRESARIAL
                && encuesta.getTipo() == TipoEncuesta.TUTOR
                && esTutorAsignado(usuario, practica)) {
            return;
        }

        throw new AccesoNoAutorizadoException("Acceso denegado: no puede responder esta encuesta.");
    }

    private void validarVisibleParaAdministrativo(Long practicaId) {
        validarVisibleParaAdministrativo(obtenerPractica(practicaId));
    }

    private void validarVisibleParaAdministrativo(InstanciaPractica practica) {
        if (!scopePracticaResolver.resolver().esVisible(practica)) {
            throw new AccesoNoAutorizadoException("Acceso denegado: practica fuera de su alcance.");
        }
    }

    private Encuesta obtenerEncuesta(Long encuestaId) {
        return encuestaRepository.findById(encuestaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Encuesta no encontrada: " + encuestaId));
    }

    private InstanciaPractica obtenerPractica(Long practicaId) {
        return practicaRepository.findByIdWithExpedienteAndEstudiante(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Practica no encontrada: " + practicaId));
    }

    private boolean esEstudiantePropio(Usuario usuario, InstanciaPractica practica) {
        Estudiante estudiante = practica.getExpediente() != null ? practica.getExpediente().getEstudiante() : null;
        if (estudiante == null) {
            return false;
        }
        return estudianteRepository.findByUsuario_Id(usuario.getId())
                .or(() -> estudianteRepository.findByCorreoIgnoreCase(usuario.getCorreo()))
                .map(e -> e.getId().equals(estudiante.getId()))
                .orElse(false);
    }

    private boolean esTutorAsignado(Usuario usuario, InstanciaPractica practica) {
        return tutorRepository.findByUsuarioId(usuario.getId())
                .or(() -> tutorRepository.findByCorreoIgnoreCase(usuario.getCorreo()))
                .map(tutor -> {
                    if (tutor.getId() == null || !tutor.getId().equals(practica.getTutorId())) {
                        return false;
                    }
                    if (tutor.getEmpresa() == null || practica.getEmpresaId() == null) {
                        return false;
                    }
                    return tutor.getEmpresa().getId().equals(practica.getEmpresaId());
                })
                .orElse(false);
    }

    private boolean esDocenteAsignado(Usuario usuario, InstanciaPractica practica) {
        return docenteRepository.findByUsuario_Id(usuario.getId())
                .or(() -> docenteRepository.findByCorreoIgnoreCase(usuario.getCorreo()))
                .map(DocenteAsesor::getId)
                .map(id -> id.equals(practica.getDocenteAsesorId()))
                .orElse(false);
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccesoNoAutorizadoException("Acceso denegado: usuario no autenticado.");
        }
        return usuarioRepository.findByCorreoIgnoreCase(auth.getName())
                .orElseThrow(() -> new AccesoNoAutorizadoException("Acceso denegado: usuario no encontrado."));
    }
}
