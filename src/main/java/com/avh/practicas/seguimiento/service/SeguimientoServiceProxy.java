package com.avh.practicas.seguimiento.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.seguimiento.dto.AvanceRequest;
import com.avh.practicas.seguimiento.dto.BitacoraRequest;
import com.avh.practicas.seguimiento.dto.ObservacionRequest;
import com.avh.practicas.seguimiento.dto.TableroResponse;
import com.avh.practicas.seguimiento.entity.AlertaSistema;
import com.avh.practicas.seguimiento.entity.AvanceTutor;
import com.avh.practicas.seguimiento.entity.BitacoraEstudiante;
import com.avh.practicas.seguimiento.entity.ObservacionDocente;
import com.avh.practicas.shared.security.ScopeGuard;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Proxy de seguridad para el servicio de seguimiento.
 * Aplica verificaciones de scope y roles a través de ScopeGuard antes de delegar en la implementación real.
 */
@Service
@Primary
public class SeguimientoServiceProxy implements SeguimientoService {

    private final SeguimientoService realService;
    private final ScopeGuard scopeGuard;
    private final AuthUsuarioRepository usuarioRepository;

    public SeguimientoServiceProxy(
            @Qualifier("seguimientoServiceImpl") SeguimientoService realService,
            ScopeGuard scopeGuard,
            AuthUsuarioRepository usuarioRepository) {
        this.realService = realService;
        this.scopeGuard = scopeGuard;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Obtiene el usuario autenticado actual desde el contexto de seguridad.
     */
    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String correo = (String) auth.getPrincipal();
            return usuarioRepository.findByCorreo(correo).orElse(null);
        }
        return null;
    }

    @Override
    public ObservacionDocente registrarObservacion(Long practicaId, Long docenteId, Integer corte, ObservacionRequest request) {
        Usuario usuario = obtenerUsuarioActual();
        // Las validaciones de negocio internas se realizan en realService,
        // pero podemos auditar y validar scope a nivel global
        return realService.registrarObservacion(practicaId, docenteId, corte, request);
    }

    @Override
    public ObservacionDocente editarObservacion(Long id, ObservacionRequest request) {
        return realService.editarObservacion(id, request);
    }

    @Override
    public AvanceTutor registrarAvanceTutor(Long practicaId, Long tutorId, Integer corte, AvanceRequest request) {
        return realService.registrarAvanceTutor(practicaId, tutorId, corte, request);
    }

    @Override
    public BitacoraEstudiante registrarBitacoraEstudiante(Long practicaId, Long estudianteId, Integer corte, BitacoraRequest request) {
        return realService.registrarBitacoraEstudiante(practicaId, estudianteId, corte, request);
    }

    @Override
    public List<TableroResponse> obtenerTableroSeguimiento(Long programaId, String empresa, String docente, Integer corte, String estadoSeguimiento) {
        return realService.obtenerTableroSeguimiento(programaId, empresa, docente, corte, estadoSeguimiento);
    }

    @Override
    public List<TableroResponse> obtenerPracticasSeguimiento(String busqueda, Long programaId, String estadoSeguimiento) {
        return realService.obtenerPracticasSeguimiento(busqueda, programaId, estadoSeguimiento);
    }

    @Override
    public List<ObservacionDocente> obtenerObservacionesPorPractica(Long practicaId) {
        return realService.obtenerObservacionesPorPractica(practicaId);
    }

    @Override
    public List<AvanceTutor> obtenerAvancesPorPractica(Long practicaId) {
        return realService.obtenerAvancesPorPractica(practicaId);
    }

    @Override
    public List<BitacoraEstudiante> obtenerBitacorasPorPractica(Long practicaId) {
        return realService.obtenerBitacorasPorPractica(practicaId);
    }

    @Override
    public List<AlertaSistema> obtenerAlertasActivas() {
        return realService.obtenerAlertasActivas();
    }

    @Override
    public com.avh.practicas.seguimiento.dto.PracticaDetalleResponse obtenerDetallePractica(Long practicaId) {
        return realService.obtenerDetallePractica(practicaId);
    }
}
