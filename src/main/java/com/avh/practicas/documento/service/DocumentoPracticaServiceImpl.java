package com.avh.practicas.documento.service;

import com.avh.practicas.documento.dto.DocumentoDescargaDto;
import com.avh.practicas.documento.dto.DocumentoPracticaDto;
import com.avh.practicas.documento.dto.DocumentosPracticaResponse;
import com.avh.practicas.documento.entity.CategoriaDocumento;
import com.avh.practicas.documento.entity.DocumentoPractica;
import com.avh.practicas.documento.entity.EstadoDocumento;
import com.avh.practicas.documento.repository.DocumentoPracticaRepository;
import com.avh.practicas.estudiante.entity.InstanciaPractica;
import com.avh.practicas.estudiante.repository.InstanciaPracticaRepository;
import com.avh.practicas.shared.exception.NegocioException;
import com.avh.practicas.shared.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service("documentoPracticaServiceImpl")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentoPracticaServiceImpl implements DocumentoPracticaService {

    private final DocumentoPracticaRepository documentoPracticaRepository;
    private final InstanciaPracticaRepository instanciaPracticaRepository;

    @Value("${app.documentos.storage-path:./storage/documentos}")
    private String storagePath;

    @Override
    public DocumentosPracticaResponse listarPorPractica(Long practicaId) {
        validarPracticaExiste(practicaId);

        List<DocumentoPractica> documentos = documentoPracticaRepository
                .findByInstanciaPracticaIdOrderByFechaCargaDesc(practicaId);

        Map<CategoriaDocumento, List<DocumentoPracticaDto>> agrupados = new EnumMap<>(CategoriaDocumento.class);
        for (CategoriaDocumento categoria : CategoriaDocumento.values()) {
            agrupados.put(categoria, new ArrayList<>());
        }

        for (DocumentoPractica documento : documentos) {
            if (documento.getCategoria() == null) {
                continue;
            }
            agrupados.get(documento.getCategoria()).add(toDto(documento));
        }

        return DocumentosPracticaResponse.builder()
                .vinculacion(agrupados.get(CategoriaDocumento.VINCULACION))
                .seguimiento(agrupados.get(CategoriaDocumento.SEGUIMIENTO))
                .evaluacionesEncuestas(agrupados.get(CategoriaDocumento.EVALUACIONES_ENCUESTAS))
                .actaCierre(agrupados.get(CategoriaDocumento.ACTA_CIERRE))
                .build();
    }

    @Override
    public DocumentoDescargaDto descargar(Long practicaId, Long documentoId) {
        validarPracticaExiste(practicaId);

        DocumentoPractica documento = documentoPracticaRepository
                .findByIdAndInstanciaPracticaId(documentoId, practicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el documento con id: " + documentoId + " para la práctica: " + practicaId));

        if (documento.getEstado() != EstadoDocumento.CARGADO) {
            throw new NegocioException("El documento aún no está disponible para descarga (estado: PENDIENTE)");
        }

        Resource recurso = cargarArchivo(documento.getUrl());

        return DocumentoDescargaDto.builder()
                .nombre(documento.getNombre())
                .recurso(recurso)
                .build();
    }

    private void validarPracticaExiste(Long practicaId) {
        if (!instanciaPracticaRepository.existsById(practicaId)) {
            throw new RecursoNoEncontradoException("No se encontró la práctica con id: " + practicaId);
        }
    }

    private DocumentoPracticaDto toDto(DocumentoPractica documento) {
        String cargadoPor = documento.getUsuarioCarga() != null
                ? documento.getUsuarioCarga().getNombre()
                : null;

        return DocumentoPracticaDto.builder()
                .id(documento.getId())
                .nombre(documento.getNombre())
                .fechaCarga(documento.getFechaCarga())
                .cargadoPor(cargadoPor)
                .estado(documento.getEstado())
                .build();
    }

    private Resource cargarArchivo(String url) {
        Path ruta = Paths.get(url);
        if (!ruta.isAbsolute()) {
            ruta = Paths.get(storagePath).resolve(url).normalize();
        }

        if (!Files.exists(ruta) || !Files.isRegularFile(ruta)) {
            throw new RecursoNoEncontradoException("El archivo físico del documento no está disponible");
        }

        return new FileSystemResource(ruta);
    }
}
