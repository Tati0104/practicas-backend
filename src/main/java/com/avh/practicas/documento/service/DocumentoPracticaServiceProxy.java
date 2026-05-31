package com.avh.practicas.documento.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.auth.repository.AuthUsuarioRepository;
import com.avh.practicas.documento.dto.DocumentoDescargaDto;
import com.avh.practicas.documento.dto.DocumentosPracticaResponse;
import com.avh.practicas.documento.entity.DocumentoPractica;
import com.avh.practicas.documento.repository.DocumentoPracticaRepository;
import com.avh.practicas.estudiante.entity.Estudiante;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.EstudianteRepository;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.shared.enums.Rol;
import com.avh.practicas.shared.exception.AccesoNoAutorizadoException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import com.avh.practicas.shared.security.ScopeGuard;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Primary
public class DocumentoPracticaServiceProxy implements DocumentoPracticaService {

    private final DocumentoPracticaService realService;
    private final ScopeGuard scopeGuard;
    private final AuthUsuarioRepository usuarioRepository;
    private final InstanciaPracticaRepository instanciaPracticaRepository;
    private final DocumentoPracticaRepository documentoPracticaRepository;
    private final EstudianteRepository estudianteRepository;

    public DocumentoPracticaServiceProxy(
            @Qualifier("documentoPracticaServiceImpl") DocumentoPracticaService realService,
            ScopeGuard scopeGuard,
            AuthUsuarioRepository usuarioRepository,
            InstanciaPracticaRepository instanciaPracticaRepository,
            DocumentoPracticaRepository documentoPracticaRepository,
            EstudianteRepository estudianteRepository) {
        this.realService = realService;
        this.scopeGuard = scopeGuard;
        this.usuarioRepository = usuarioRepository;
        this.instanciaPracticaRepository = instanciaPracticaRepository;
        this.documentoPracticaRepository = documentoPracticaRepository;
        this.estudianteRepository = estudianteRepository;
    }

    @Override
    public DocumentosPracticaResponse listarPorPractica(Long practicaId) {
        InstanciaPractica practica = obtenerPractica(practicaId);
        Usuario usuario = obtenerUsuarioActual();
        verificarAccesoPractica(usuario, practica, "LISTAR");
        return realService.listarPorPractica(practicaId);
    }

    @Override
    public DocumentoDescargaDto descargar(Long practicaId, Long documentoId) {
        InstanciaPractica practica = obtenerPractica(practicaId);
        Usuario usuario = obtenerUsuarioActual();
        verificarAccesoPractica(usuario, practica, "DESCARGAR");

        DocumentoPractica documento = documentoPracticaRepository
                .findByIdAndInstanciaPracticaId(documentoId, practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el documento con id: " + documentoId + " para la práctica: " + practicaId));

        scopeGuard.verificarScope(usuario, documento, "DESCARGAR");

        return realService.descargar(practicaId, documentoId);
    }

    private InstanciaPractica obtenerPractica(Long practicaId) {
        return instanciaPracticaRepository.findByIdWithExpedienteEstudiante(practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la práctica con id: " + practicaId));
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String correo = (String) auth.getPrincipal();
            return usuarioRepository.findByCorreo(correo).orElse(null);
        }
        return null;
    }

    private void verificarAccesoPractica(Usuario usuario, InstanciaPractica practica, String accion) {
        scopeGuard.verificarScope(usuario, practica, accion);

        if (usuario == null || usuario.getRol() != Rol.ESTUDIANTE) {
            return;
        }

        Estudiante estudiante = estudianteRepository.findByCorreo(usuario.getCorreo())
                .orElseThrow(() -> new AccesoNoAutorizadoException("Acceso denegado: estudiante no encontrado"));

        if (practica.getExpediente() == null
                || practica.getExpediente().getEstudiante() == null
                || !practica.getExpediente().getEstudiante().getId().equals(estudiante.getId())) {
            throw new AccesoNoAutorizadoException("Acceso denegado: el estudiante solo puede acceder a sus propias prácticas");
        }
    }
}
