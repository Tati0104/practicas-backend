# Guía de Contribución y Buenas Prácticas

¡Bienvenido al equipo de desarrollo! Para mantener el repositorio limpio y organizado, todos los miembros del equipo (TCA, EHS, KBM) debemos seguir estas pautas.

## 1. Nomenclatura de Ramas (Git Branching)
Nunca trabajes directamente sobre `main` o `develop`. Cada tarea o funcionalidad debe desarrollarse en su propia rama utilizando las iniciales de quien la realiza:

`[Iniciales]/sprint[Numero]-[descripcion-breve]`

*   **TCA:** `TCA/sprint1-calificaciones`
*   **EHS:** `EHS/sprint1-inicializacion`
*   **KBM:** `KBM/sprint1-reportes`

Para integrar tus cambios, crea un Pull Request (PR) hacia la rama de desarrollo principal.

## 2. Convención de Commits
Para que el historial sea legible, cada commit debe seguir esta convención:

`[tipo]([modulo]): [descripcion clara en minusculas]`

*   **Tipos válidos:**
    *   `feat`: Nueva funcionalidad (ej. `feat(configuracion): agregar controlador de facultad`).
    *   `fix`: Corrección de un error (ej. `fix(estudiante): validar formato de correo en excel`).
    *   `docs`: Cambios en la documentación (ej. `docs(shared): actualizar manual de instalacion`).
    *   `test`: Añadir o modificar pruebas (ej. `test(empresa): test unitario para registro de empresa`).

## 3. Nomenclatura de Clases y Entidades (Dominio)
De acuerdo con las reglas de negocio y los diagramas del dominio, **los nombres de las clases de entidad, servicios y controladores deben ser representados en español**:
*   *Correcto:* `Facultad.java`, `Programa.java`, `Estudiante.java`, `Empresa.java`, `TutorEmpresarial.java`.
*   *Evitar nombres en inglés para el negocio:* `Faculty.java`, `Student.java`, `Company.java`.

## 4. Resolución de Conflictos Básicos
Si hay cambios conflictivos al intentar integrar tu rama:
1. Haz checkout a tu rama local.
2. Trae los últimos cambios de main: `git pull origin main`.
3. Resuelve los conflictos manualmente en tu editor.
4. Ejecuta las pruebas locales: `./mvnw test` para asegurar que todo compila.
5. Haz commit de la resolución del conflicto y sube los cambios.
