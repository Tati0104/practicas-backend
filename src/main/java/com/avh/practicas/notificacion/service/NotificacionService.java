package com.avh.practicas.notificacion.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.seguimiento.entity.AlertaSistema;
import com.avh.practicas.seguimiento.repository.AlertaSistemaRepository;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.exception.AccesoNoAutorizadoException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final AlertaSistemaRepository alertaRepository;
    private final AuthUsuarioRepository usuarioRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public List<AlertaSistema> listarParaUsuarioActual(Long practicaId) {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null) {
            throw new AccesoNoAutorizadoException("Debe iniciar sesión para ver notificaciones.");
        }

        List<AlertaSistema> alertas = listarPendientes(usuario);

        if (practicaId != null) {
            verificarAccesoPractica(usuario, practicaId);
            return alertas.stream()
                    .filter(a -> practicaId.equals(a.getInstanciaPracticaId()))
                    .toList();
        }

        return alertas;
    }

    @Transactional
    public void marcarLeida(Long alertaId) {
        Usuario usuario = obtenerUsuarioActual();
        AlertaSistema alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación no encontrada: " + alertaId));

        if (!puedeVerAlerta(usuario, alerta)) {
            throw new AccesoNoAutorizadoException("No puede modificar notificaciones de otro usuario.");
        }

        alerta.setLeida(true);
        alertaRepository.save(alerta);
    }

    private List<AlertaSistema> listarPendientes(Usuario usuario) {
        Rol rol = usuario.getRol();

        if (rol == Rol.ADMIN || rol == Rol.DIRECCION) {
            return alertaRepository.findByLeidaFalseAndResueltaFalseOrderByPrioritariaDescFechaDesc();
        }

        Set<Long> practicasVisibles = obtenerPracticasVisibles(usuario);
        if (practicasVisibles.isEmpty()) {
            return alertaRepository.findPendientesPersonales(usuario.getCorreo());
        }

        return alertaRepository.findPendientesParaUsuario(usuario.getCorreo(), practicasVisibles);
    }

    private boolean puedeVerAlerta(Usuario usuario, AlertaSistema alerta) {
        if (usuario == null) {
            return false;
        }
        Rol rol = usuario.getRol();
        if (rol == Rol.ADMIN || rol == Rol.DIRECCION) {
            return true;
        }
        if (alerta.getDestinatarioCorreo() != null) {
            return alerta.getDestinatarioCorreo().equalsIgnoreCase(usuario.getCorreo());
        }
        if (alerta.getInstanciaPracticaId() == null) {
            return false;
        }
        return obtenerPracticasVisibles(usuario).contains(alerta.getInstanciaPracticaId());
    }

    private void verificarAccesoPractica(Usuario usuario, Long practicaId) {
        if (usuario.getRol() == Rol.ADMIN || usuario.getRol() == Rol.DIRECCION) {
            return;
        }
        if (!obtenerPracticasVisibles(usuario).contains(practicaId)) {
            throw new AccesoNoAutorizadoException("No tiene acceso a las notificaciones de esta práctica.");
        }
    }

    private Set<Long> obtenerPracticasVisibles(Usuario usuario) {
        if (usuario == null || usuario.getCorreo() == null) {
            return Collections.emptySet();
        }

        return switch (usuario.getRol()) {
            case ESTUDIANTE -> consultarIds("""
                    SELECT ip.id FROM instancias_practica ip
                    JOIN expedientes e ON e.id = ip.expediente_id
                    JOIN estudiantes est ON est.id = e.estudiante_id
                    WHERE LOWER(est.correo) = LOWER(?)
                    """, usuario.getCorreo());
            case DOCENTE_ASESOR -> consultarIds("""
                    SELECT ip.id FROM instancias_practica ip
                    JOIN docentes_asesores d ON d.id = ip.docente_asesor_id
                    WHERE LOWER(d.correo) = LOWER(?)
                    """, usuario.getCorreo());
            case TUTOR_EMPRESARIAL -> consultarIds("""
                    SELECT ip.id FROM instancias_practica ip
                    JOIN tutores_empresariales t ON t.id = ip.tutor_id
                    WHERE LOWER(t.correo) = LOWER(?)
                    """, usuario.getCorreo());
            case COORD_PRACTICA, SECRETARIA, COORD_ACADEMICA -> {
                if (usuario.getFacultad() == null) {
                    yield Collections.emptySet();
                }
                yield consultarIds("""
                        SELECT ip.id FROM instancias_practica ip
                        JOIN expedientes e ON e.id = ip.expediente_id
                        JOIN estudiantes est ON est.id = e.estudiante_id
                        JOIN programas p ON p.id = est.programa_id
                        WHERE p.facultad_id = ?
                        """, usuario.getFacultad().getId());
            }
            default -> Collections.emptySet();
        };
    }

    private Set<Long> consultarIds(String sql, Object param) {
        List<Long> ids = jdbcTemplate.queryForList(sql, Long.class, param);
        return new HashSet<>(ids);
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String correo = (String) auth.getPrincipal();
        return usuarioRepository.findByCorreo(correo).orElse(null);
    }
}
