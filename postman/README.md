# Colección Postman — AVH Prácticas Backend

## Importar

1. Abre Postman → **Import** → selecciona `AVH-Practicas-Backend.postman_collection.json`.
2. Configura variables de colección:
   - `baseUrl`: `http://localhost:8080`
   - `correo` / `password`: usuario válido en tu BD
   - IDs (`estudianteId`, `practicaId`, etc.) según tus datos de prueba
3. Ejecuta **Auth → Login** (guarda el JWT automáticamente).

## Regenerar la colección

Si agregas endpoints al proyecto:

```bash
python postman/generate_collection.py
```

## Ramas requeridas

| Carpeta Postman | Rama mínima |
|-----------------|-------------|
| Auth … Dashboard | `develop` |
| Sprint 5 - Encuestas | `EHS/sprint5-encuestas` o `EHS/sprint6-respaldo` |
| Sprint 5 - Seguimiento / Calificaciones | `EHS/sprint6-respaldo` (merge de S5) |
| Sprint 6 - Respaldo | `EHS/sprint6-respaldo` |

## Documentación relacionada

- Patrones y SOLID: [`docs/GUIA-PATRONES-Y-SOLID.md`](../docs/GUIA-PATRONES-Y-SOLID.md)
