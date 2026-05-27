# RF-03-06 — Habilitación de práctica siguiente

## Descripción

La lógica RF-03-06 valida que un estudiante solo pueda iniciar una nueva práctica si la práctica anterior se encuentra en estado COMPLETADA.

Este flujo evita que se creen prácticas consecutivas sin haber finalizado correctamente la práctica anterior.

## Flujo esperado

1. La práctica actual finaliza mediante el proceso de cierre.
2. El sistema dispara el evento PRACTICA_COMPLETADA.
3. El ObservadorPanel notifica a Coordinación Académica.
4. Coordinación Académica revisa al estudiante.
5. Al marcar nuevamente al estudiante como apto, EstudianteService valida que la práctica N-1 esté en estado COMPLETADA.
6. Si la validación es correcta, se crea la instancia de práctica N+1.
7. Si la práctica anterior no está completada, el sistema no permite crear la siguiente práctica.

## Diagrama de secuencia textual

FachadaCierrePractica
→ dispara evento PRACTICA_COMPLETADA
→ ObservadorPanel actualiza notificación para Coordinación Académica
→ Coordinación Académica ejecuta marcarApto
→ EstudianteService valida práctica anterior COMPLETADA
→ EstudianteService crea la siguiente InstanciaPractica

## Estado actual

La documentación del flujo queda registrada para integración con el módulo de cierre en sprints posteriores.
