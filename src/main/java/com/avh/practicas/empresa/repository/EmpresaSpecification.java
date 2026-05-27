package com.avh.practicas.empresa.repository;

import com.avh.practicas.empresa.entity.Empresa;
import com.avh.practicas.vacante.entity.Vacante;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class EmpresaSpecification {

    public static Specification<Empresa> filtrar(
            String sector,
            String programa,
            Boolean activo) {

        return (Root<Empresa> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction();

            if (StringUtils.hasText(sector)) {
                try {
                    Long sectorId = Long.parseLong(sector);
                    predicate = cb.and(predicate, cb.equal(root.get("sector").get("id"), sectorId));
                } catch (NumberFormatException e) {
                    predicate = cb.and(predicate, cb.like(cb.lower(root.get("sector").get("nombre")), "%" + sector.toLowerCase() + "%"));
                }
            }

            if (StringUtils.hasText(programa)) {
                // Subquery to find vacancies that reference this company and the requested program
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<Vacante> vacanteRoot = subquery.from(Vacante.class);
                subquery.select(vacanteRoot.get("empresa").get("id"));

                Predicate subqueryPredicate;
                try {
                    Long progId = Long.parseLong(programa);
                    subqueryPredicate = cb.equal(vacanteRoot.get("programa").get("id"), progId);
                } catch (NumberFormatException e) {
                    subqueryPredicate = cb.like(cb.lower(vacanteRoot.get("programa").get("nombre")), "%" + programa.toLowerCase() + "%");
                }

                subquery.where(cb.and(
                        subqueryPredicate,
                        cb.equal(vacanteRoot.get("empresa").get("id"), root.get("id"))
                ));

                predicate = cb.and(predicate, cb.exists(subquery));
            }

            if (activo != null) {
                predicate = cb.and(predicate, cb.equal(root.get("activo"), activo));
            }

            return predicate;
        };
    }
}
