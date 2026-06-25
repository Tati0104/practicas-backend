package com.avh.practicas.dashboard.service;

import com.avh.practicas.asignacion.entity.Asignacion;
import com.avh.practicas.asignacion.repository.AsignacionRepository;
import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.calificacion.repository.NotaFinalRepository;
import com.avh.practicas.cierre.entity.Encuesta;
import com.avh.practicas.cierre.entity.TipoEncuesta;
import com.avh.practicas.cierre.repository.EncuestaRepository;
import com.avh.practicas.dashboard.dto.PanelEstudianteDto;
import com.avh.practicas.dashboard.dto.PracticaEstudianteResumenDto;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.exception.AccesoNoAutorizadoException;
import com.avh.practicas.vinculacion.dto.DocumentoVinculacionDto;
import com.avh.practicas.vinculacion.entity.Convenio;
import com.avh.practicas.vinculacion.entity.DocumentoPractica;
import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.empresa.repository.EmpresaRepository;
import com.avh.practicas.vacante.entity.Vacante;
import com.avh.practicas.vacante.repository.VacanteRepository;
import com.avh.practicas.vinculacion.repository.ConvenioRepository;
import com.avh.practicas.vinculacion.repository.DocumentoPracticaRepository;
import com.avh.practicas.vinculacion.support.DocumentoVinculacionSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EstudiantePanelService {

    private final AuthUsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final AsignacionRepository asignacionRepository;
    private final InstanciaPracticaRepository practicaRepository;
    private final DocumentoPracticaRepository documentoPracticaRepository;
    private final ConvenioRepository convenioRepository;
    private final EncuestaRepository encuestaRepository;
    private final NotaFinalRepository notaFinalRepository;
    private final VacanteRepository vacanteRepository;
    private final EmpresaRepository empresaRepository;

    @Transactional(readOnly = true)
    public PanelEstudianteDto obtenerPanelEstudiante() {
        Estudiante estudiante = resolverEstudianteAutenticado();

        List<PracticaEstudianteResumenDto> practicas = new ArrayList<>();
        Set<Long> practicasIncluidas = new HashSet<>();

        for (InstanciaPractica practica : practicaRepository.findByExpedienteEstudianteIdOrderByNumeroPracticaDesc(estudiante.getId())) {
            practicasIncluidas.add(practica.getId());
            Optional<Asignacion> asignacion = asignacionRepository.findFirstByInstanciaPracticaIdAndEstadoNot(
                    practica.getId(), com.avh.practicas.asignacion.entity.EstadoAsignacion.CANCELADA);
            practicas.add(asignacion.map(this::mapearPracticaResumen)
                    .orElseGet(() -> mapearPracticaResumenDesdeInstancia(practica)));
        }

        for (Asignacion asignacion : asignacionRepository.findByEstudianteId(estudiante.getId())) {
            if (asignacion.getEstado() == com.avh.practicas.asignacion.entity.EstadoAsignacion.CANCELADA) {
                continue;
            }
            Long practicaId = asignacion.getInstanciaPracticaId();
            if (practicaId == null || !practicasIncluidas.contains(practicaId)) {
                practicas.add(mapearPracticaResumen(asignacion));
            }
        }

        return new PanelEstudianteDto(
                estudiante.getNombre(),
                estudiante.getCorreo(),
                estudiante.getIdentificacion(),
                estudiante.getPrograma() != null ? estudiante.getPrograma().getNombre() : null,
                practicas
        );
    }

    private PracticaEstudianteResumenDto mapearPracticaResumen(Asignacion asignacion) {
        Long practicaId = asignacion.getInstanciaPracticaId();
        Optional<InstanciaPractica> practicaOpt = practicaId != null
                ? practicaRepository.findById(practicaId)
                : Optional.empty();

        Integer numeroPractica = practicaOpt.map(InstanciaPractica::getNumeroPractica).orElse(null);
        EstadoPractica estadoPractica = practicaOpt.map(InstanciaPractica::getEstado).orElse(null);

        List<DocumentoPractica> documentos = documentoPracticaRepository.findByAsignacionIdOrderByFechaDesc(asignacion.getId());
        if (documentos.isEmpty() && practicaId != null) {
            documentos = documentoPracticaRepository.findByInstanciaPracticaIdOrderByFechaDesc(practicaId);
        }
        Optional<Convenio> convenio = convenioRepository.findByAsignacionId(asignacion.getId());
        if (convenio.isEmpty() && practicaId != null) {
            convenio = convenioRepository.findByInstanciaPracticaId(practicaId);
        }
        List<DocumentoVinculacionDto> paneles = DocumentoVinculacionSupport.construirPaneles(documentos, convenio);

        int total = paneles.size();
        int completos = (int) paneles.stream().filter(this::documentoCompleto).count();
        boolean convenioFirmado = convenio.map(c -> c.getFirmaEstudianteAt() != null).orElse(false);

        String estadoEncuesta = practicaId != null
                ? encuestaRepository.findByInstanciaPracticaIdAndTipo(practicaId, TipoEncuesta.ESTUDIANTE)
                .map(e -> e.getEstado().name())
                .orElse("PENDIENTE")
                : "PENDIENTE";

        Double notaFinal = practicaId != null
                ? notaFinalRepository.findByInstanciaPracticaId(practicaId)
                .map(nf -> nf.getNotaFinal())
                .orElse(null)
                : null;

        boolean finalizada = estadoPractica == EstadoPractica.COMPLETADA
                || estadoPractica == EstadoPractica.REPROBADA;

        String cargo = null;
        String empresa = null;
        if (asignacion.getVacanteId() != null) {
            Optional<Vacante> vacante = vacanteRepository.findById(asignacion.getVacanteId());
            if (vacante.isPresent()) {
                cargo = vacante.get().getCargo();
                if (vacante.get().getEmpresaId() != null) {
                    empresa = empresaRepository.findById(vacante.get().getEmpresaId())
                            .map(Empresa::getRazonSocial)
                            .orElse(null);
                }
            }
        }

        return new PracticaEstudianteResumenDto(
                asignacion.getId(),
                practicaId,
                numeroPractica,
                cargo,
                empresa,
                asignacion.getEstado(),
                estadoPractica,
                completos,
                total,
                convenioFirmado,
                practicaOpt.isPresent() && estadoPractica == EstadoPractica.EN_CURSO ? "EN_CURSO" : "—",
                estadoEncuesta,
                notaFinal,
                finalizada
        );
    }

    private PracticaEstudianteResumenDto mapearPracticaResumenDesdeInstancia(InstanciaPractica practica) {
        Long practicaId = practica.getId();
        Integer numeroPractica = practica.getNumeroPractica();
        EstadoPractica estadoPractica = practica.getEstado();

        List<DocumentoPractica> documentos = documentoPracticaRepository.findByInstanciaPracticaIdOrderByFechaDesc(practicaId);
        Optional<Convenio> convenio = convenioRepository.findByInstanciaPracticaId(practicaId);
        List<DocumentoVinculacionDto> paneles = DocumentoVinculacionSupport.construirPaneles(documentos, convenio);

        int total = paneles.size();
        int completos = (int) paneles.stream().filter(this::documentoCompleto).count();
        boolean convenioFirmado = convenio.map(c -> c.getFirmaEstudianteAt() != null).orElse(false);

        String estadoEncuesta = encuestaRepository.findByInstanciaPracticaIdAndTipo(practicaId, TipoEncuesta.ESTUDIANTE)
                .map(e -> e.getEstado().name())
                .orElse("PENDIENTE");

        Double notaFinal = notaFinalRepository.findByInstanciaPracticaId(practicaId)
                .map(nf -> nf.getNotaFinal())
                .orElse(null);

        boolean finalizada = estadoPractica == EstadoPractica.COMPLETADA
                || estadoPractica == EstadoPractica.REPROBADA;

        String cargo = practica.getNombre();
        String empresa = null;
        if (practica.getEmpresaId() != null) {
            empresa = empresaRepository.findById(practica.getEmpresaId())
                    .map(Empresa::getRazonSocial)
                    .orElse(null);
        }

        return new PracticaEstudianteResumenDto(
                null,
                practicaId,
                numeroPractica,
                cargo,
                empresa,
                com.avh.practicas.asignacion.entity.EstadoAsignacion.ASIGNADA,
                estadoPractica,
                completos,
                total,
                convenioFirmado,
                estadoPractica == EstadoPractica.EN_CURSO ? "EN_CURSO" : "—",
                estadoEncuesta,
                notaFinal,
                finalizada
        );
    }

    private boolean documentoCompleto(DocumentoVinculacionDto doc) {
        if (doc == null || "PENDIENTE".equals(doc.estado())) {
            return false;
        }
        if ("CONVENIO".equals(doc.tipo())) {
            return "FIRMADO".equals(doc.estado());
        }
        return "SUBIDO".equals(doc.estado()) || "FIRMADO".equals(doc.estado());
    }

    private Estudiante resolverEstudianteAutenticado() {
        Usuario usuario = obtenerUsuarioAutenticado();
        if (usuario == null || usuario.getRol() != Rol.ESTUDIANTE) {
            throw new AccesoNoAutorizadoException("Solo estudiantes pueden acceder a este panel.");
        }
        return estudianteRepository.findByCorreoIgnoreCase(usuario.getCorreo())
                .or(() -> estudianteRepository.findByUsuario_Id(usuario.getId()))
                .orElseThrow(() -> new AccesoNoAutorizadoException("No se encontró el perfil de estudiante."));
    }

    private Usuario obtenerUsuarioAutenticado() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof String correo)) {
            return null;
        }
        return usuarioRepository.findByCorreo(correo).orElse(null);
    }
}
