# Gestión y Seguimiento de Prácticas Empresariales — AVH

Plataforma web para gestionar el ciclo completo de prácticas empresariales: aptitud del estudiante, asignación a empresas, vinculación documental, seguimiento, calificaciones y cierre.

Este repositorio es **monorepo**: contiene el **backend** (Spring Boot) en la raíz y el **frontend** (React + Vite) en la carpeta `frontend/`.

---

## Arquitectura

```
┌─────────────────────┐       HTTP        ┌─────────────────────┐       JDBC       ┌──────────────────┐
│  Frontend (React)   │  ──────────────►  │  Backend (Spring)   │  ─────────────►  │  PostgreSQL      │
│  localhost:5173     │                   │  localhost:8080     │                  │  localhost:5432  │
└─────────────────────┘                   └─────────────────────┘                  └──────────────────┘
```

| Componente | Carpeta | Puerto | Tecnología |
|------------|---------|--------|------------|
| Frontend | `frontend/` | 5173 | React 18+, Vite, TanStack Query, Zustand, Tailwind |
| Backend | raíz del repo | 8080 | Java 17+, Spring Boot 3.5, JPA, Security, Flyway |
| Base de datos | Docker (`docker-compose.yml`) | 5432 | PostgreSQL 15 |

---

## Requisitos previos

Instalar **una sola vez**:

| Herramienta | Versión mínima | Comando para verificar |
|-------------|----------------|------------------------|
| Java JDK | 17+ | `java -version` |
| Node.js | 18+ | `node -version` |
| npm | 9+ | `npm -version` |
| Docker Desktop | Reciente | `docker --version` |
| Git | Cualquiera | `git --version` |

> **Maven no es obligatorio.** El proyecto incluye el wrapper `mvnw` / `mvnw.cmd`.

---

## Configuración inicial (primera vez)

### 1. Clonar el repositorio

```powershell
git clone <URL-DEL-REPOSITORIO>
cd practicas
```

### 2. PostgreSQL con Docker

1. Abre **Docker Desktop** y espera a que indique **Running**.
2. Desde la **raíz del proyecto**:

```powershell
docker compose up -d
docker compose ps
```

Debe aparecer `practicas-postgres` en estado **running**.

**Credenciales por defecto de la BD (desarrollo):**

| Parámetro | Valor |
|-----------|-------|
| Host | `localhost` |
| Puerto | `5432` |
| Base de datos | `practicas_db` |
| Usuario | `postgres` |
| Contraseña | `postgres` |

> Flyway crea automáticamente todas las tablas al iniciar el backend. **No ejecutes scripts SQL manualmente** salvo emergencia.

#### Conflicto con PostgreSQL instalado en Windows

Si tienes PostgreSQL 17/18 instalado localmente, puede **ocupar el puerto 5432** y provocar error de contraseña al arrancar el backend.

**Solución recomendada** — detener los servicios locales mientras trabajas en este proyecto (PowerShell **como Administrador**):

```powershell
Stop-Service postgresql-x64-17
Stop-Service postgresql-x64-18
```

Para volver a usarlos en otros proyectos:

```powershell
Start-Service postgresql-x64-17
Start-Service postgresql-x64-18
```

### 3. Backend (Spring Boot)

Desde la **raíz del proyecto** (donde está `mvnw.cmd`):

```powershell
# Windows — recomendado la primera vez o tras errores de Flyway
.\mvnw.cmd clean spring-boot:run

# Linux / Mac
./mvnw clean spring-boot:run
```

Espera el mensaje:

```text
Started PracticasApplication
```

El API queda disponible en **http://localhost:8080**.

#### Variables de entorno opcionales (backend)

| Variable | Descripción | Default en dev |
|----------|-------------|----------------|
| `SPRING_DATASOURCE_USERNAME` | Usuario PostgreSQL | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña PostgreSQL | `postgres` |
| `JWT_SECRET` | Clave para firmar tokens JWT | Valor en `application-dev.yml` |
| `MAIL_MODE` | `stub` = sin envío real de correos; `smtp` = Gmail/SMTP | `stub` |
| `MAIL_PASSWORD` | Contraseña SMTP (solo si `MAIL_MODE=smtp`) | — |
| `SERVER_PORT` | Puerto del backend | `8080` |

Ejemplo en PowerShell:

```powershell
$env:MAIL_MODE="smtp"
$env:MAIL_PASSWORD="tu-app-password"
.\mvnw.cmd spring-boot:run
```

Perfil activo por defecto: **`dev`** (`src/main/resources/application-dev.yml`).

### 4. Frontend (React + Vite)

```powershell
cd frontend
copy .env.example .env    # Windows
# cp .env.example .env    # Linux / Mac

npm install
npm run dev
```

Abre **http://localhost:5173/login**

El archivo `.env` **no se sube a Git**. Cada desarrollador copia `.env.example` → `.env`.

Contenido de `.env`:

```env
VITE_API_URL=http://localhost:8080
```

Si cambias el puerto del backend, actualiza también `VITE_API_URL` y reinicia `npm run dev`.

### 5. Iniciar sesión

Usuario creado automáticamente por Flyway (`V15__seed_admin_dev.sql`):

| Campo | Valor |
|-------|-------|
| Correo | `admin@test.com` |
| Contraseña | `Admin12345` |
| Rol | `ADMIN` |

Con rol **ADMIN** puedes acceder a todos los módulos del menú lateral.

---

## Arranque diario (después de la primera configuración)

Abre **3 terminales**:

```powershell
# Terminal 1 — Base de datos (raíz del proyecto)
docker compose up -d

# Terminal 2 — Backend (raíz del proyecto)
.\mvnw.cmd spring-boot:run

# Terminal 3 — Frontend
cd frontend
npm run dev
```

---

## Después de bajar cambios (`git pull`)

Sigue este orden para evitar errores:

```powershell
# 1. Actualizar código
git pull

# 2. Si hubo cambios en docker-compose.yml
docker compose up -d

# 3. Si hubo nuevas migraciones Flyway o dependencias Java
.\mvnw.cmd clean spring-boot:run

# 4. Si hubo cambios en package.json del frontend
cd frontend
npm install
npm run dev
```

> **Importante:** Si el backend falla con errores de Flyway (versiones duplicadas), ejecuta siempre `.\mvnw.cmd clean spring-boot:run` para limpiar la carpeta `target/` antes de volver a intentar.

---

## Scripts útiles

### Backend (raíz del proyecto)

| Comando | Descripción |
|---------|-------------|
| `.\mvnw.cmd spring-boot:run` | Ejecutar en modo desarrollo |
| `.\mvnw.cmd clean spring-boot:run` | Limpiar y ejecutar (recomendado tras pull o errores) |
| `.\mvnw.cmd clean package -DskipTests` | Generar JAR para producción |
| `.\mvnw.cmd test` | Ejecutar tests |

### Frontend (`frontend/`)

| Comando | Descripción |
|---------|-------------|
| `npm run dev` | Servidor de desarrollo (puerto 5173) |
| `npm run build` | Build de producción → carpeta `dist/` |
| `npm run preview` | Previsualizar build local |
| `npm run test` | Ejecutar tests (Vitest) |
| `npm run lint` | Linter ESLint |

---

## Estructura del repositorio

```
practicas/
├── docker-compose.yml          # PostgreSQL local
├── mvnw / mvnw.cmd             # Maven wrapper
├── src/                        # Backend Spring Boot
│   └── main/resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/       # Migraciones Flyway (NO editar las ya aplicadas)
└── frontend/
    ├── .env.example            # Plantilla de variables (copiar a .env)
    ├── package.json
    └── src/
        ├── modules/            # Módulos por dominio (auth, dashboard, etc.)
        ├── shared/             # Componentes y servicios compartidos
        ├── store/              # Zustand (authStore, etc.)
        └── router/             # AppRouter.jsx
```

---

## Solución de problemas

### Error: `password authentication failed for user "postgres"`

**Causa:** PostgreSQL de Windows escuchando en 5432 con otra contraseña, o Docker no es quien responde.

**Solución:** Detener servicios locales (`Stop-Service postgresql-x64-17/18`) y verificar `docker compose ps`.

---

### Error: `Found more than one migration with version X` (Flyway)

**Causa:** Carpeta `target/` desactualizada con archivos SQL viejos.

**Solución:**

```powershell
.\mvnw.cmd clean spring-boot:run
```

---

### Error: `Port 8080 was already in use`

**Causa:** Otra instancia del backend (u otra app) usando el puerto.

**Solución:**

```powershell
netstat -ano | findstr :8080
taskkill /PID <NUMERO_PID> /F
```

O usar otro puerto:

```powershell
$env:SERVER_PORT="8081"
.\mvnw.cmd spring-boot:run
```

Y en `frontend/.env`: `VITE_API_URL=http://localhost:8081`

---

### Error: `Cannot find package 'vitest'` o `tailwindcss`

**Causa:** Dependencias del frontend sin instalar.

**Solución:**

```powershell
cd frontend
npm install
```

---

### Error: `cd frontend` dentro de `frontend`

**Causa:** Ruta duplicada (`frontend\frontend`).

**Solución:** Verifica tu ubicación con `Get-Location` (PowerShell). Debes estar en la raíz para `cd frontend`, o ya dentro de `frontend` para `npm run dev`.

---

### Docker: `failed to connect to the docker API`

**Causa:** Docker Desktop no está abierto.

**Solución:** Inicia Docker Desktop y espera a que esté en **Running**.

---

### Login falla / Network Error en el navegador

**Verificar:**

1. Backend corriendo → consola muestra `Started PracticasApplication`
2. `frontend/.env` tiene `VITE_API_URL=http://localhost:8080`
3. Reiniciar frontend tras cambiar `.env`

---

### Reiniciar la base de datos desde cero (borrar todos los datos)

```powershell
docker compose down -v
docker compose up -d
.\mvnw.cmd clean spring-boot:run
```

> Esto elimina el volumen `postgres_data`. Flyway volverá a crear tablas y el usuario admin.

---

## Despliegue en producción (referencia)

| Componente | Acción |
|------------|--------|
| **PostgreSQL** | Servicio gestionado (Railway, Render, AWS RDS, etc.) |
| **Backend** | `.\mvnw.cmd clean package -DskipTests` → ejecutar JAR con perfil `prod` |
| **Variables backend** | `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `MAIL_MODE`, `PORT` |
| **Frontend** | `npm run build` con `VITE_API_URL=https://api.tudominio.com` |
| **Estáticos** | Servir `frontend/dist/` con Nginx, Vercel, Netlify, etc. |

Configuración de producción: `src/main/resources/application-prod.yml`

Ejemplo de variables prod:

```bash
DB_URL=jdbc:postgresql://host:5432/practicas_db
DB_USER=postgres
DB_PASSWORD=********
JWT_SECRET=clave-segura-larga-y-unica
MAIL_MODE=smtp
PORT=8080
```

---

## Reglas para trabajo en equipo

1. **No commitear** `frontend/.env`, contraseñas ni secretos JWT reales.
2. **No modificar** migraciones Flyway ya mergeadas (`V{n}__*.sql`). Crear siempre una nueva versión.
3. **No hardcodear** URLs de API en el código; usar `VITE_API_URL` y variables de entorno.
4. **No cambiar** puertos ni credenciales en `application-dev.yml` sin acuerdo del equipo; preferir variables de entorno.
5. Seguir la estructura de módulos frontend: `frontend/src/modules/{modulo}/pages|components|hooks`.
6. Antes de abrir un PR: backend arranca + `npm run build` en frontend pasa sin errores.
7. Tras `git pull` con migraciones nuevas: `.\mvnw.cmd clean spring-boot:run`.

---

## Stack tecnológico

### Backend

- Java 17+, Spring Boot 3.5.14
- Spring Data JPA, Spring Security, JWT
- PostgreSQL + Flyway
- Apache POI (Excel), iText (PDF)
- Maven

### Frontend

- React 18+, Vite 5
- React Router DOM v6/v7
- TanStack Query v5, Zustand
- Axios, React Hook Form + Zod
- Tailwind CSS v3, Lucide React, React Hot Toast

---

## Contacto y soporte interno

Si un compañero no logra levantar el entorno:

1. Revisar la sección [Solución de problemas](#solución-de-problemas).
2. Confirmar que Docker Desktop está **Running**.
3. Compartir las últimas 20 líneas de la terminal del backend.

---

**Universidad Alexander Von Humboldt — Proyecto de Prácticas Empresariales**
