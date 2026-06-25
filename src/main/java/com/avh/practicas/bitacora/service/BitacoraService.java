package com.avh.practicas.bitacora.service;

import com.avh.practicas.auth.entity.Usuario;
import com.avh.practicas.bitacora.entity.EntradaBitacora;
import com.avh.practicas.bitacora.entity.TipoAccion;
import com.avh.practicas.bitacora.repository.EntradaBitacoraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BitacoraService {

    private final EntradaBitacoraRepository repository;

    @Transactional
    public EntradaBitacora registrar(
            Long usuarioId,
            String modulo,
            TipoAccion tipoAccion,
            Long idRecurso,
            String valoresAnteriores,
            String valoresNuevos
    ) {
        String detalle = construirDetalle(idRecurso, valoresAnteriores, valoresNuevos);

        EntradaBitacora entrada = EntradaBitacora.builder()
                .usuarioId(usuarioId)
                .fecha(LocalDateTime.now())
                .tablaAfectada(modulo)
                .accion(tipoAccion != null ? tipoAccion.name() : "ACCION_NO_DEFINIDA")
                .detalle(detalle)
                .build();

        return repository.save(entrada);
    }

    @Transactional
    public EntradaBitacora registrar(
            String tablaAfectada,
            String accion,
            Long usuarioId,
            String detalle
    ) {
        EntradaBitacora entrada = EntradaBitacora.builder()
                .usuarioId(usuarioId)
                .fecha(LocalDateTime.now())
                .tablaAfectada(tablaAfectada)
                .accion(accion)
                .detalle(detalle)
                .build();

        return repository.save(entrada);
    }

    @Transactional
    public EntradaBitacora registrar(
            String tablaAfectada,
            String accion,
            Usuario usuario,
            String detalle
    ) {
        return registrar(
                tablaAfectada,
                accion,
                usuario != null ? usuario.getId() : null,
                detalle
        );
    }

    @Transactional(readOnly = true)
    public Page<EntradaBitacora> listar(
            Long usuarioId,
            String modulo,
            TipoAccion tipoAccion,
            LocalDateTime desde,
            LocalDateTime hasta,
            Pageable pageable
    ) {
        String accion = tipoAccion != null ? tipoAccion.name() : null;

        List<EntradaBitacora> filtrados = repository.findAll()
                .stream()
                .filter(e -> usuarioId == null || usuarioId.equals(e.getUsuarioId()))
                .filter(e -> modulo == null || modulo.isBlank() || modulo.equalsIgnoreCase(e.getTablaAfectada()))
                .filter(e -> accion == null || accion.equalsIgnoreCase(e.getAccion()))
                .filter(e -> desde == null || e.getFecha() == null || !e.getFecha().isBefore(desde))
                .filter(e -> hasta == null || e.getFecha() == null || !e.getFecha().isAfter(hasta))
                .sorted(Comparator.comparing(
                        EntradaBitacora::getFecha,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtrados.size());

        if (start > filtrados.size()) {
            return new PageImpl<>(List.of(), pageable, filtrados.size());
        }

        return new PageImpl<>(filtrados.subList(start, end), pageable, filtrados.size());
    }

    private String construirDetalle(Long idRecurso, String valoresAnteriores, String valoresNuevos) {
        StringBuilder detalle = new StringBuilder();

        if (idRecurso != null) {
            detalle.append("Recurso ID: ").append(idRecurso).append(". ");
        }

        if (valoresAnteriores != null && !valoresAnteriores.isBlank()) {
            detalle.append("Valores anteriores: ").append(valoresAnteriores).append(". ");
        }

        if (valoresNuevos != null && !valoresNuevos.isBlank()) {
            detalle.append("Valores nuevos: ").append(valoresNuevos).append(".");
        }

        return detalle.toString().trim();
    }
}