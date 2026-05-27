package com.avh.practicas.shared.security;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.configuracion.entity.CatalogoPractica;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.seguimiento.service.BitacoraService;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import com.avh.practicas.shared.exception.AccesoNoAutorizadoException;
import com.avh.practicas.vacante.entity.Vacante;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScopeGuard {

    private final EstudianteRepository estudianteRepository;
    private final BitacoraService bitacoraService;

    public boolean verificarScope(Usuario usuario, Object recurso, String accion) {
        if (usuario == null) {
            throw new AccesoNoAutorizadoException("Acceso denegado: usuario no autenticado");
        }

        // ADMIN siempre pasa
        if (usuario.getRol() == Rol.ADMIN) {
            return true;
        }

        // Si el scope es PROGRAMA, verificar correspondencia de programa
        if (usuario.getScope() == Scope.PROGRAMA) {
            if (recurso instanceof Empresa || recurso instanceof TutorEmpresarial) {
                return true;
            }
            Programa programaUsuario = obtenerProgramaDelUsuario(usuario);
            Programa programaRecurso = obtenerProgramaDelRecurso(recurso);

            if (programaUsuario == null || programaRecurso == null || !programaUsuario.getId().equals(programaRecurso.getId())) {
                String tablaAfectada = obtenerNombreTabla(recurso);
                String detalle = String.format("Acceso denegado: recurso fuera del scope del programa. Usuario: %s, Recurso ID: %s", 
                        usuario.getCorreo(), obtenerIdRecurso(recurso));
                
                // Registra intentos fallidos en BitacoraService
                bitacoraService.registrar(tablaAfectada, accion, usuario, detalle);
                
                throw new AccesoNoAutorizadoException("Acceso denegado: recurso fuera del scope");
            }
        }

        return true;
    }

    private Programa obtenerProgramaDelUsuario(Usuario usuario) {
        if (usuario.getRol() == Rol.ESTUDIANTE) {
            return estudianteRepository.findByCorreo(usuario.getCorreo())
                    .map(Estudiante::getPrograma)
                    .orElse(null);
        }
        // Nota: para docentes/coordinadores no hay relación de programa en BD actualmente.
        return null;
    }

    private Programa obtenerProgramaDelRecurso(Object recurso) {
        if (recurso == null) {
            return null;
        }
        if (recurso instanceof Programa) {
            return (Programa) recurso;
        }
        if (recurso instanceof Estudiante) {
            return ((Estudiante) recurso).getPrograma();
        }
        if (recurso instanceof Vacante) {
            return ((Vacante) recurso).getPrograma();
        }
        if (recurso instanceof InstanciaPractica) {
            InstanciaPractica ip = (InstanciaPractica) recurso;
            if (ip.getExpediente() != null && ip.getExpediente().getEstudiante() != null) {
                return ip.getExpediente().getEstudiante().getPrograma();
            }
        }
        if (recurso instanceof CatalogoPractica) {
            return ((CatalogoPractica) recurso).getPrograma();
        }
        return null;
    }

    private String obtenerNombreTabla(Object recurso) {
        if (recurso == null) {
            return "DESCONOCIDO";
        }
        if (recurso instanceof Estudiante) return "estudiantes";
        if (recurso instanceof Programa) return "programas";
        if (recurso instanceof Vacante) return "vacantes";
        if (recurso instanceof InstanciaPractica) return "instancias_practica";
        if (recurso instanceof CatalogoPractica) return "catalogo_practicas";
        if (recurso instanceof Empresa) return "empresas";
        if (recurso instanceof TutorEmpresarial) return "tutores_empresariales";
        
        return recurso.getClass().getSimpleName().toLowerCase();
    }

    private String obtenerIdRecurso(Object recurso) {
        if (recurso == null) return "null";
        try {
            // Intentar invocar getId() si existe por reflexión
            return String.valueOf(recurso.getClass().getMethod("getId").invoke(recurso));
        } catch (Exception e) {
            return "unknown";
        }
    }
}
