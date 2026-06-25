# Despliegue EC2 — PracTI

## Requisitos

- Clave PEM en la raíz del repo: `PracTI_Par_Claves.pem`
- `~/practi.env` en EC2 (copiar desde `practi.env.example`, **no commitear**)

## Despliegue rápido (Windows)

Desde la raíz del proyecto:

```powershell
.\scripts\ec2\deploy.ps1
```

Solo backend o solo frontend:

```powershell
.\scripts\ec2\deploy.ps1 -FrontendOnly
.\scripts\ec2\deploy.ps1 -BackendOnly
.\scripts\ec2\deploy.ps1 -SkipBuild
```

## Manual (SSH)

```bash
bash ~/start-backend.sh
bash ~/deploy-frontend.sh ~/frontend-dist
```

## URLs producción

| Servicio | URL |
|----------|-----|
| Frontend | http://3.226.93.10/login |
| API | http://3.226.93.10:8080 |

## Variables importantes en `~/practi.env`

- `CORS_ALLOWED_ORIGINS` — debe incluir `http://3.226.93.10`
- `FRONTEND_BASE_URL` — `http://3.226.93.10` (enlaces en correos de encuestas)
