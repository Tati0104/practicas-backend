# Gestión y Seguimiento de Prácticas Empresariales

Este es el repositorio del backend para el sistema de Gestión y Seguimiento de Prácticas Empresariales.

## Stack Tecnológico
*   **Lenguaje:** Java 17
*   **Framework:** Spring Boot 3.5.14 (Spring Web, Spring Data JPA, Spring Security)
*   **Base de Datos:** PostgreSQL
*   **Migraciones:** Flyway
*   **Gestor de Dependencias:** Maven
*   **Otras dependencias:** Lombok, Apache POI (para importaciones de Excel), iText PDF (para reportes en PDF).

## Requisitos Previos
*   Java Development Kit (JDK) 17 instalado.
*   PostgreSQL 14 o superior instalado y corriendo.
*   Maven instalado (o utilizar el wrapper `./mvnw` incluido).

## Configuración y Ejecución del Backend

### 1. Base de Datos
1. Abre tu terminal de PostgreSQL (o una herramienta como pgAdmin o DBeaver).
2. Crea una base de datos llamada `practicas_db`:
   ```sql
   CREATE DATABASE practicas_db;
   ```

### 2. Configurar Variables de Entorno (Opcional)
La aplicación utiliza por defecto los valores del perfil `dev` definidos en `src/main/resources/application-dev.yml`:
*   **Usuario:** `postgres`
*   **Contraseña:** `postgres`
*   **Puerto BD:** `5432`

Si tus credenciales locales son distintas, puedes editarlas en el archivo `application-dev.yml` o definirlas como variables de entorno.

### 3. Ejecutar la Aplicación
Ejecuta el siguiente comando en la raíz del backend:
```bash
./mvnw spring-boot:run
```
La aplicación iniciará por defecto en el puerto `8080`.

---

## Configuración y Ejecución del Frontend (React)
*(Este backend está preparado para conectarse con un frontend en React. Consulta el repositorio del frontend para las instrucciones de ejecución con npm/yarn).*
