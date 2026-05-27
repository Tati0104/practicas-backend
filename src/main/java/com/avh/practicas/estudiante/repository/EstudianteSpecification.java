package com.avh.practicas.estudiante.repository;

import com.avh.practicas.estudiante.entity.EstadoAptitud;
import com.avh.practicas.estudiante.entity.EstadoPractica;
import com.avh.practicas.estudiante.entity.Estudiante;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class EstudianteSpecification {

    public static Specification<Estudiante> filtrar(
            String programa,
            String facultad,
            EstadoAptitud aptitud,
            String estadoPractica) {

        return (Root<Estudiante> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction();

            if (StringUtils.hasText(programa)) {
                try {
                    Long programaId = Long.parseLong(programa);
                    predicate = cb.and(predicate, cb.equal(root.get("programa").get("id"), programaId));
                } catch (NumberFormatException e) {
                    predicate = cb.and(predicate, cb.like(cb.lower(root.get("programa").get("nombre")), "%" + programa.toLowerCase() + "%"));
                }
            }

            if (StringUtils.hasText(facultad)) {
                try {
                    Long facultadId = Long.parseLong(facultad);
                    predicate = cb.and(predicate, cb.equal(root.get("programa").get("facultad").get("id"), facultadId));
                } catch (NumberFormatException e) {
                    predicate = cb.and(predicate, cb.like(cb.lower(root.get("programa").get("facultad").get("nombre")), "%" + facultad.toLowerCase() + "%"));
                }
            }

            if (aptitud != null) {
                predicate = cb.and(predicate, cb.equal(root.get("estadoAptitud"), aptitud));
            }

            if (StringUtils.hasText(estadoPractica)) {
                Join<Object, Object> expedienteJoin = root.join("expediente", JoinType.INNER);
                Join<Object, Object> IPJoin = expedienteJoin.join("instanciasPractica", JoinType.INNER);
                try {
                    EstadoPractica ep = EstadoPractica.valueOf(estadoPractica.toUpperCase());
                    predicate = cb.and(predicate, cb.equal(IPJoin.get("estado"), ep));
                } catch (IllegalArgumentException e) {
                    // Ignora si no es un estado válido
                }
            }

            // Evitar duplicados si hay joins
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.distinct(true);
            }

            return predicate;
        };
    }
}
