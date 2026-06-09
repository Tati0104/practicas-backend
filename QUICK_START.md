# 🚀 Guía Rápida - Ejecutar Backend Practicas-Backend

## Prerrequisitos
✅ Docker Desktop instalado y corriendo  
✅ PostgreSQL 15 en contenedor Docker activo  
✅ Java 17+ instalado  
✅ Maven 3.8+ instalado

---

## 1️⃣ Iniciar PostgreSQL en Docker

Si aún no está corriendo:

```bash
cd C:\Users\ESTEFANY\Documents\practicas-backend
docker compose up -d
```

Verificar que está corriendo:
```bash
docker ps
```

Deberías ver: `practicas_postgres ... Up ... 0.0.0.0:5432->5432/tcp`

---

## 2️⃣ Ejecutar el Backend

### Opción A: Comando directo (recomendado)
```bash
cd C:\Users\ESTEFANY\Documents\practicas-backend
mvn spring-boot:run
```

### Opción B: Usar script Batch
```bash
start-backend.bat
```

### Opción C: Usar script PowerShell
```powershell
.\start-backend.ps1
```

---

## 3️⃣ Verificar que el backend está corriendo

Espera ~50 segundos a que termine la compilación y arranque.

Cuando veas esto en la terminal, ¡está listo!:
```
Tomcat started on port(s): 8080 (http)
Started PracticasApplication in X.XXX seconds
```

---

## 4️⃣ Probar el Backend

Abre tu navegador o Postman:

```
GET http://localhost:8080/actuator/health
```

Deberías recibir:
```json
{
  "status": "UP"
}
```

---

## 📋 Puertos en uso
- **Backend**: `http://localhost:8080`
- **PostgreSQL**: `localhost:5432`
- **Actuator**: `http://localhost:8080/actuator`
- **Health**: `http://localhost:8080/actuator/health`

---

## 🐛 Troubleshooting

### Error: "Port 8080 is already in use"
```powershell
# Encuentra el proceso
netstat -ano | findstr :8080

# Mata el proceso (reemplaza XXXXX con el PID)
taskkill /PID XXXXX /F
```

### Error: "Connection to localhost:5432 refused"
Asegúrate que Docker está corriendo:
```bash
docker compose up -d
docker ps
```

### Error de compilación
Limpia el cache de Maven:
```bash
mvn clean install
mvn spring-boot:run
```

---

## 📚 Estructura del Proyecto

```
practicas-backend/
├── src/main/java/com/avh/practicas/
│   ├── auth/              # Autenticación (Kevin)
│   ├── usuario/           # Usuarios (Tatiana)
│   ├── configuracion/     # Configuración (Estefany - S1)
│   ├── estudiante/        # Estudiantes (Estefany - S2)
│   ├── empresa/           # Empresas (Estefany - S3)
│   ├── seguimiento/       # Seguimiento (Estefany - S5)
│   ├── calificacion/      # Calificaciones (Estefany - S5)
│   └── ...
├── src/main/resources/
│   ├── db/migration/      # Scripts Flyway (Estefany - S0)
│   └── application-dev.yml
├── docker-compose.yml     # PostgreSQL container
└── pom.xml
```

---

## ✨ Cambios Realizados (9 Jun 2026)

1. ✅ Corregido error de inyección de dependencias en `ImportadorExcelAdapter`
2. ✅ Corregida expresión Cron en `AlertaInactividadJob` (6 campos en lugar de 5)
3. ✅ Agregado método `importarAsync` a interfaz `ImportadorEstudiantes`
4. ✅ PostgreSQL corriendo exitosamente en Docker
5. ✅ Backend listo para iniciar

---

## 💡 Comandos Útiles

```bash
# Compilar sin ejecutar
mvn clean compile

# Ejecutar tests
mvn test

# Ver logs
mvn spring-boot:run -e

# Detener el backend
# Ctrl + C en la terminal

# Verificar estado de Docker
docker ps -a

# Ver logs de PostgreSQL
docker logs practicas_postgres
```

---

**Creado**: 9 de Junio de 2026  
**Versión del Backend**: 0.0.1-SNAPSHOT  
**Estado**: ✅ Listo para usar

