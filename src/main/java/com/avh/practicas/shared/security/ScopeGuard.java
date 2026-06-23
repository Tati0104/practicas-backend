package com.avh.practicas.shared.security;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.asignacion.entity.Asignacion;
import com.avh.practicas.configuracion.entity.CatalogoPractica;
import com.avh.practicas.configuracion.entity.Facultad;
import com.avh.practicas.configuracion.entity.Programa;
import com.avh.practicas.configuracion.repository.ProgramaRepository;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.entity.TutorEmpresarial;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.empresa.repository.TutorEmpresarialRepository;
import com.avh.practicas.estudiante.entity.DocenteAsesor;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.DocenteAsesorRepository;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.seguimiento.service.BitacoraService;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.enums.Scope;
import com.avh.practicas.shared.exception.AccesoNoAutorizadoException;
import com.avh.practicas.vacante.entity.Vacante;
import com.avh.practicas.vacante.repository.VacanteRepository;
import org.springframework.stereotype.Component;

@Component
public class ScopeGuard {

    private final EstudianteRepository estudianteRepository;
    private final ProgramaRepository programaRepository;
    private final DocenteAsesorRepository docenteAsesorRepository;
    private final EmpresaRepository empresaRepository;
    private final TutorEmpresarialRepository tutorEmpresarialRepository;
    private final InstanciaPracticaRepository instanciaPracticaRepository;
    private final VacanteRepository vacanteRepository;
    private final BitacoraService bitacoraService;

    public ScopeGuard(
            EstudianteRepository estudianteRepository,
            ProgramaRepository programaRepository,
            DocenteAsesorRepository docenteAsesorRepository,
            EmpresaRepository empresaRepository,
            TutorEmpresarialRepository tutorEmpresarialRepository,
            InstanciaPracticaRepository instanciaPracticaRepository,
            VacanteRepository vacanteRepository,
            BitacoraService bitacoraService) {
        this.estudianteRepository = estudianteRepository;
        this.programaRepository = programaRepository;
        this.docenteAsesorRepository = docenteAsesorRepository;
        this.empresaRepository = empresaRepository;
        this.tutorEmpresarialRepository = tutorEmpresarialRepository;
        this.instanciaPracticaRepository = instanciaPracticaRepository;
        this.vacanteRepository = vacanteRepository;
        this.bitacoraService = bitacoraService;
    }

    public boolean verificarScope(Usuario usuario, Object recurso, String accion) {
        if (usuario == null) {
            throw new AccesoNoAutorizadoException("Acceso denegado: usuario no autenticado");
        }

        if (usuario.getRol() == Rol.ADMIN || usuario.getRol() == Rol.COORD_PRACTICA) {
            return true;
        }

        if (usuario.getRol() == Rol.DOCENTE_ASESOR) {
            verificarPropiedadDocente(usuario, recurso, accion);
            return true;
        }

        if (usuario.getRol() == Rol.EMPRESA || usuario.getRol() == Rol.TUTOR_EMPRESARIAL) {
            verificarPropiedadEmpresa(usuario, recurso, accion);
            return true;
        }

        if (usuario.getScope() == Scope.PROGRAMA) {
            if (recurso instanceof Empresa || recurso instanceof TutorEmpresarial) {
                return true;
            }

            if (usuario.getRol() == Rol.ESTUDIANTE) {
                verificarPropiedadEstudiante(usuario, recurso, accion);
                return true;
            }

            Programa programaUsuario = obtenerProgramaDelUsuario(usuario);
            Programa programaRecurso = obtenerProgramaDelRecurso(recurso);

            if (programaUsuario == null || programaRecurso == null || !programaUsuario.getId().equals(programaRecurso.getId())) {
                registrarAccesoDenegado(usuario, recurso, accion, "programa");
                throw new AccesoNoAutorizadoException("Acceso denegado: recurso fuera del scope");
            }
        }

        if (usuario.getScope() == Scope.FACULTAD) {
            if (recurso instanceof Empresa || recurso instanceof TutorEmpresarial) {
                return true;
            }

            Long facultadUsuario = obtenerFacultadIdDelUsuario(usuario);
            Long facultadRecurso = obtenerFacultadIdDelRecurso(recurso);

            if (facultadUsuario == null || facultadRecurso == null || !facultadUsuario.equals(facultadRecurso)) {
                registrarAccesoDenegado(usuario, recurso, accion, "facultad");
                throw new AccesoNoAutorizadoException("Acceso denegado: recurso fuera del scope");
            }
        }

        return true;
    }

    private void verificarPropiedadEstudiante(Usuario usuario, Object recurso, String accion) {
        Estudiante estudianteLogueado = estudianteRepository.findByCorreoIgnoreCase(usuario.getCorreo())
                .orElseThrow(() -> new AccesoNoAutorizadoException(
                        "Acceso denegado: perfil de estudiante no encontrado"));

        if (recurso instanceof Estudiante estudiante) {
            if (estudiante.getId() == null || !estudiante.getId().equals(estudianteLogueado.getId())) {
                registrarAccesoDenegado(usuario, recurso, accion, "estudiante");
                throw new AccesoNoAutorizadoException("Acceso denegado: solo puede consultar su propia información");
            }
            return;
        }

        if (recurso instanceof InstanciaPractica instanciaPractica) {
            Long propietarioId = obtenerEstudianteIdDePractica(instanciaPractica);
            if (propietarioId == null || !propietarioId.equals(estudianteLogueado.getId())) {
                registrarAccesoDenegado(usuario, recurso, accion, "estudiante");
                throw new AccesoNoAutorizadoException("Acceso denegado: solo puede consultar su propia práctica");
            }
            return;
        }

        Programa programaUsuario = estudianteLogueado.getPrograma();
        Programa programaRecurso = obtenerProgramaDelRecurso(recurso);
        if (programaUsuario == null || programaRecurso == null || !programaUsuario.getId().equals(programaRecurso.getId())) {
            registrarAccesoDenegado(usuario, recurso, accion, "programa");
            throw new AccesoNoAutorizadoException("Acceso denegado: recurso fuera del scope");
        }
    }

    private void verificarPropiedadDocente(Usuario usuario, Object recurso, String accion) {
        DocenteAsesor docente = docenteAsesorRepository.findByUsuario_Id(usuario.getId())
                .or(() -> docenteAsesorRepository.findByCorreoIgnoreCase(usuario.getCorreo()))
                .orElseThrow(() -> new AccesoNoAutorizadoException(
                        "Acceso denegado: perfil de docente asesor no encontrado"));

        if (recurso instanceof InstanciaPractica practica) {
            if (practica.getDocenteAsesorId() == null || !practica.getDocenteAsesorId().equals(docente.getId())) {
                registrarAccesoDenegado(usuario, recurso, accion, "docente asignado");
                throw new AccesoNoAutorizadoException("Acceso denegado: práctica no asignada al docente.");
            }
            return;
        }

        if (recurso instanceof Estudiante estudiante) {
            boolean asignado = instanciaPracticaRepository.existsByExpedienteEstudianteIdAndDocenteAsesorId(
                    estudiante.getId(), docente.getId());
            if (!asignado) {
                registrarAccesoDenegado(usuario, recurso, accion, "docente asignado");
                throw new AccesoNoAutorizadoException("Acceso denegado: estudiante no asignado al docente.");
            }
            return;
        }

        if (recurso instanceof Asignacion asignacion) {
            boolean asignado = asignacion.getInstanciaPracticaId() != null
                    ? instanciaPracticaRepository.existsByIdAndDocenteAsesorId(asignacion.getInstanciaPracticaId(), docente.getId())
                    : instanciaPracticaRepository.existsByExpedienteEstudianteIdAndDocenteAsesorId(asignacion.getEstudianteId(), docente.getId());
            if (!asignado) {
                registrarAccesoDenegado(usuario, recurso, accion, "docente asignado");
                throw new AccesoNoAutorizadoException("Acceso denegado: asignación no relacionada con el docente.");
            }
            return;
        }

        throw new AccesoNoAutorizadoException("Acceso denegado: recurso fuera del alcance del docente.");
    }

    private void verificarPropiedadEmpresa(Usuario usuario, Object recurso, String accion) {
        Long empresaId = resolverEmpresaIdAsignada(usuario);

        if (recurso instanceof Empresa empresa) {
            if (!empresaId.equals(empresa.getId())) {
                registrarAccesoDenegado(usuario, recurso, accion, "empresa asignada");
                throw new AccesoNoAutorizadoException("Acceso denegado: solo puede consultar su empresa.");
            }
            return;
        }

        if (recurso instanceof TutorEmpresarial tutor) {
            Long tutorEmpresaId = tutor.getEmpresa() != null ? tutor.getEmpresa().getId() : null;
            if (!empresaId.equals(tutorEmpresaId)) {
                registrarAccesoDenegado(usuario, recurso, accion, "empresa asignada");
                throw new AccesoNoAutorizadoException("Acceso denegado: tutor fuera de su empresa.");
            }
            return;
        }

        if (recurso instanceof Vacante vacante) {
            if (!empresaId.equals(vacante.getEmpresaId())) {
                registrarAccesoDenegado(usuario, recurso, accion, "empresa asignada");
                throw new AccesoNoAutorizadoException("Acceso denegado: vacante fuera de su empresa.");
            }
            return;
        }

        if (recurso instanceof InstanciaPractica practica) {
            if (!empresaId.equals(practica.getEmpresaId())) {
                registrarAccesoDenegado(usuario, recurso, accion, "empresa asignada");
                throw new AccesoNoAutorizadoException("Acceso denegado: práctica fuera de su empresa.");
            }
            if (usuario.getRol() == Rol.TUTOR_EMPRESARIAL) {
                Long tutorId = tutorEmpresarialRepository.findByUsuarioId(usuario.getId())
                        .or(() -> tutorEmpresarialRepository.findByCorreoIgnoreCase(usuario.getCorreo()))
                        .map(t -> t.getId())
                        .orElse(null);
                if (tutorId == null || !tutorId.equals(practica.getTutorId())) {
                    registrarAccesoDenegado(usuario, recurso, accion, "tutor asignado");
                    throw new AccesoNoAutorizadoException("Acceso denegado: práctica no asignada a este tutor.");
                }
            }
            return;
        }

        if (recurso instanceof Asignacion asignacion) {
            boolean propia = vacanteRepository.findById(asignacion.getVacanteId())
                    .map(vacante -> empresaId.equals(vacante.getEmpresaId()))
                    .orElse(false);
            if (!propia) {
                registrarAccesoDenegado(usuario, recurso, accion, "empresa asignada");
                throw new AccesoNoAutorizadoException("Acceso denegado: asignación fuera de su empresa.");
            }
            return;
        }

        throw new AccesoNoAutorizadoException("Acceso denegado: recurso fuera del alcance de empresa.");
    }

    private Long resolverEmpresaIdAsignada(Usuario usuario) {
        if (usuario.getRol() == Rol.TUTOR_EMPRESARIAL) {
            return tutorEmpresarialRepository.findByUsuarioId(usuario.getId())
                    .or(() -> tutorEmpresarialRepository.findByCorreoIgnoreCase(usuario.getCorreo()))
                    .map(t -> t.getEmpresa().getId())
                    .orElseThrow(() -> new AccesoNoAutorizadoException(
                            "Acceso denegado: perfil de tutor empresarial no encontrado"));
        }
        return empresaRepository.findByUsuarioId(usuario.getId())
                .map(Empresa::getId)
                .orElseThrow(() -> new AccesoNoAutorizadoException(
                        "Acceso denegado: empresa asociada no encontrada"));
    }

    private Long obtenerEstudianteIdDePractica(InstanciaPractica instanciaPractica) {
        if (instanciaPractica.getExpediente() == null || instanciaPractica.getExpediente().getEstudiante() == null) {
            return null;
        }
        return instanciaPractica.getExpediente().getEstudiante().getId();
    }

    private Programa obtenerProgramaDelUsuario(Usuario usuario) {
        if (usuario.getRol() == Rol.ESTUDIANTE) {
            return estudianteRepository.findByCorreoIgnoreCase(usuario.getCorreo())
                    .map(Estudiante::getPrograma)
                    .orElse(null);
        }

        return null;
    }

    private Long obtenerFacultadIdDelUsuario(Usuario usuario) {
        if (usuario.getFacultad() != null) {
            return usuario.getFacultad().getId();
        }
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
            Long programaId = ((Vacante) recurso).getProgramaId();

            if (programaId == null) {
                return null;
            }

            Programa programa = new Programa();
            programa.setId(programaId);
            return programa;
        }

        if (recurso instanceof InstanciaPractica) {
            InstanciaPractica instanciaPractica = (InstanciaPractica) recurso;

            if (instanciaPractica.getExpediente() != null
                    && instanciaPractica.getExpediente().getEstudiante() != null) {
                return instanciaPractica.getExpediente().getEstudiante().getPrograma();
            }
        }

        if (recurso instanceof CatalogoPractica) {
            return ((CatalogoPractica) recurso).getPrograma();
        }

        return null;
    }

    private Long obtenerFacultadIdDelRecurso(Object recurso) {
        if (recurso instanceof Facultad facultad) {
            return facultad.getId();
        }

        if (recurso instanceof Programa programa) {
            return programa.getFacultad() != null ? programa.getFacultad().getId() : null;
        }

        if (recurso instanceof Estudiante estudiante) {
            Programa programa = estudiante.getPrograma();
            return programa != null && programa.getFacultad() != null ? programa.getFacultad().getId() : null;
        }

        if (recurso instanceof Vacante vacante && vacante.getProgramaId() != null) {
            return programaRepository.findById(vacante.getProgramaId())
                    .map(Programa::getFacultad)
                    .map(Facultad::getId)
                    .orElse(null);
        }

        if (recurso instanceof InstanciaPractica instanciaPractica
                && instanciaPractica.getExpediente() != null
                && instanciaPractica.getExpediente().getEstudiante() != null) {
            Programa programa = instanciaPractica.getExpediente().getEstudiante().getPrograma();
            return programa != null && programa.getFacultad() != null ? programa.getFacultad().getId() : null;
        }

        if (recurso instanceof CatalogoPractica catalogoPractica) {
            Programa programa = catalogoPractica.getPrograma();
            return programa != null && programa.getFacultad() != null ? programa.getFacultad().getId() : null;
        }

        return null;
    }

    private void registrarAccesoDenegado(Usuario usuario, Object recurso, String accion, String tipoScope) {
        String tablaAfectada = obtenerNombreTabla(recurso);
        String detalle = String.format(
                "Acceso denegado: recurso fuera del scope del %s. Usuario: %s, Recurso ID: %s",
                tipoScope,
                usuario.getCorreo(),
                obtenerIdRecurso(recurso)
        );
        bitacoraService.registrar(tablaAfectada, accion, usuario, detalle);
    }

    private String obtenerNombreTabla(Object recurso) {
        if (recurso == null) {
            return "DESCONOCIDO";
        }

        if (recurso instanceof Estudiante) return "estudiantes";
        if (recurso instanceof Programa) return "programas";
        if (recurso instanceof Facultad) return "facultades";
        if (recurso instanceof Vacante) return "vacantes";
        if (recurso instanceof InstanciaPractica) return "instancias_practica";
        if (recurso instanceof CatalogoPractica) return "catalogo_practicas";
        if (recurso instanceof Empresa) return "empresas";
        if (recurso instanceof TutorEmpresarial) return "tutores_empresariales";

        return recurso.getClass().getSimpleName().toLowerCase();
    }

    private String obtenerIdRecurso(Object recurso) {
        if (recurso == null) {
            return "null";
        }

        try {
            return String.valueOf(recurso.getClass().getMethod("getId").invoke(recurso));
        } catch (Exception e) {
            return "unknown";
        }
    }
}
