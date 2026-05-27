package com.avh.practicas.bitacora.service;

import com.avh.practicas.bitacora.entity.EntradaBitacora;
import com.avh.practicas.bitacora.entity.TipoAccion;
import com.avh.practicas.bitacora.repository.EntradaBitacoraRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BitacoraService {

    private final EntradaBitacoraRepository repository;

    @Transactional
    public EntradaBitacora registrar(Long usuarioId,
                                     String modulo,
                                     TipoAccion tipoAccion,
                                     Long idRecurso,
                                     String valoresAnteriores,
                                     String valoresNuevos) {
        EntradaBitacora entrada = EntradaBitacora.builder()
                .usuarioId(usuarioId)
                .fechaHora(LocalDateTime.now())
                .modulo(modulo)
                .accion(tipoAccion)
                .registroAfectado(idRecurso == null ? null : String.valueOf(idRecurso))
                .valoresAnteriores(valoresAnteriores)
                .valoresNuevos(valoresNuevos)
                .build();

        return repository.save(entrada);
    }

    @Transactional(readOnly = true)
    public Page<EntradaBitacora> listar(Long usuarioId,
                                        String modulo,
                                        TipoAccion tipoAccion,
                                        LocalDateTime desde,
                                        LocalDateTime hasta,
                                        Pageable pageable) {
        return repository.findAll(conFiltros(usuarioId, modulo, tipoAccion, desde, hasta), pageable);
    }

    private Specification<EntradaBitacora> conFiltros(Long usuarioId,
                                                      String modulo,
                                                      TipoAccion tipoAccion,
                                                      LocalDateTime desde,
                                                      LocalDateTime hasta) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (usuarioId != null) {
                predicates.add(cb.equal(root.get("usuarioId"), usuarioId));
            }
            if (modulo != null && !modulo.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("modulo")), modulo.toLowerCase()));
            }
            if (tipoAccion != null) {
                predicates.add(cb.equal(root.get("accion"), tipoAccion));
            }
            if (desde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), desde));
            }
            if (hasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaHora"), hasta));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
