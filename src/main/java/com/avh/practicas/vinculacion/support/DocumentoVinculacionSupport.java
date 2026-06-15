package com.avh.practicas.vinculacion.support;

import com.avh.practicas.vinculacion.dto.DocumentoVinculacionDto;
import com.avh.practicas.vinculacion.dto.FirmaConvenioDto;
import com.avh.practicas.vinculacion.entity.CategoriaDocumento;
import com.avh.practicas.vinculacion.entity.Convenio;
import com.avh.practicas.vinculacion.entity.DocumentoPractica;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DocumentoVinculacionSupport {

    private static final List<CategoriaDocumento> ORDEN_CATEGORIAS = List.of(
            CategoriaDocumento.HOJA_VIDA,
            CategoriaDocumento.CARTA_PRESENTACION,
            CategoriaDocumento.PROYECTO_PRACTICA,
            CategoriaDocumento.CONVENIO_PRACTICA
    );

    private DocumentoVinculacionSupport() {
    }

    public static List<DocumentoVinculacionDto> construirPaneles(
            List<DocumentoPractica> documentos,
            Optional<Convenio> convenio
    ) {
        Map<CategoriaDocumento, DocumentoPractica> ultimos = new EnumMap<>(CategoriaDocumento.class);
        for (DocumentoPractica documento : documentos) {
            if (documento.getCategoria() != null) {
                ultimos.putIfAbsent(documento.getCategoria(), documento);
            }
        }

        List<FirmaConvenioDto> firmasConvenio = convenio.map(DocumentoVinculacionSupport::firmasDesdeConvenio)
                .orElse(firmasVacias());

        List<DocumentoVinculacionDto> paneles = new ArrayList<>();
        for (CategoriaDocumento categoria : ORDEN_CATEGORIAS) {
            DocumentoPractica documento = ultimos.get(categoria);
            if (categoria == CategoriaDocumento.CONVENIO_PRACTICA) {
                paneles.add(mapearConvenio(documento, firmasConvenio));
            } else {
                paneles.add(mapearSimple(categoria, documento));
            }
        }
        return paneles;
    }

    public static String tipoFrontend(CategoriaDocumento categoria) {
        return switch (categoria) {
            case HOJA_VIDA -> "HOJA_VIDA";
            case CARTA_PRESENTACION -> "CARTA";
            case PROYECTO_PRACTICA -> "PROYECTO";
            case CONVENIO_PRACTICA -> "CONVENIO";
        };
    }

    public static CategoriaDocumento categoriaDesdeTipo(String tipo) {
        return switch (tipo.toUpperCase()) {
            case "HOJA_VIDA" -> CategoriaDocumento.HOJA_VIDA;
            case "CARTA", "CARTA_PRESENTACION" -> CategoriaDocumento.CARTA_PRESENTACION;
            case "PROYECTO", "PROYECTO_PRACTICA" -> CategoriaDocumento.PROYECTO_PRACTICA;
            case "CONVENIO", "CONVENIO_PRACTICA" -> CategoriaDocumento.CONVENIO_PRACTICA;
            default -> CategoriaDocumento.valueOf(tipo.toUpperCase());
        };
    }

    public static AlmacenCategoria almacenPara(CategoriaDocumento categoria) {
        return switch (categoria) {
            case HOJA_VIDA -> AlmacenCategoria.HOJA_VIDA;
            case CARTA_PRESENTACION -> AlmacenCategoria.CARTA_PRESENTACION;
            case PROYECTO_PRACTICA -> AlmacenCategoria.PROYECTO_PRACTICA;
            case CONVENIO_PRACTICA -> AlmacenCategoria.CONVENIO_PRACTICA;
        };
    }

    public enum AlmacenCategoria {
        HOJA_VIDA,
        CARTA_PRESENTACION,
        PROYECTO_PRACTICA,
        CONVENIO_PRACTICA;

        public com.avh.practicas.vinculacion.port.AlmacenArchivosPort.CategoriaAlmacen toPort() {
            return com.avh.practicas.vinculacion.port.AlmacenArchivosPort.CategoriaAlmacen.valueOf(name());
        }
    }

    private static DocumentoVinculacionDto mapearSimple(CategoriaDocumento categoria, DocumentoPractica documento) {
        if (documento == null) {
            return new DocumentoVinculacionDto(null, tipoFrontend(categoria), categoria, null, "PENDIENTE", List.of());
        }
        return new DocumentoVinculacionDto(
                documento.getId(),
                tipoFrontend(categoria),
                categoria,
                documento.getNombre(),
                "SUBIDO",
                List.of()
        );
    }

    private static DocumentoVinculacionDto mapearConvenio(
            DocumentoPractica documento,
            List<FirmaConvenioDto> firmas
    ) {
        CategoriaDocumento categoria = CategoriaDocumento.CONVENIO_PRACTICA;
        if (documento == null) {
            return new DocumentoVinculacionDto(null, tipoFrontend(categoria), categoria, null, "PENDIENTE", firmas);
        }

        boolean todasFirmadas = firmas.stream().allMatch(FirmaConvenioDto::firmado);
        String estado = todasFirmadas ? "FIRMADO" : "SUBIDO";

        return new DocumentoVinculacionDto(
                documento.getId(),
                tipoFrontend(categoria),
                categoria,
                documento.getNombre(),
                estado,
                firmas
        );
    }

    private static List<FirmaConvenioDto> firmasDesdeConvenio(Convenio convenio) {
        return List.of(
                new FirmaConvenioDto("COORDINADOR", convenio.getFirmaCoordinadorAt() != null, convenio.getFirmaCoordinadorAt()),
                new FirmaConvenioDto("TUTOR", convenio.getFirmaTutorAt() != null, convenio.getFirmaTutorAt()),
                new FirmaConvenioDto("ESTUDIANTE", convenio.getFirmaEstudianteAt() != null, convenio.getFirmaEstudianteAt())
        );
    }

    private static List<FirmaConvenioDto> firmasVacias() {
        return List.of(
                new FirmaConvenioDto("COORDINADOR", false, null),
                new FirmaConvenioDto("TUTOR", false, null),
                new FirmaConvenioDto("ESTUDIANTE", false, null)
        );
    }
}
