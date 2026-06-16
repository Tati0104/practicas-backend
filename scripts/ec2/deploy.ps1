# Despliegue completo a EC2 (Windows PowerShell)
# Uso: .\scripts\ec2\deploy.ps1
# Opcional: .\scripts\ec2\deploy.ps1 -SkipBuild

param(
    [string]$Ec2Host = "3.226.93.10",
    [string]$User = "ec2-user",
    [string]$Pem = "",
    [switch]$SkipBuild,
    [switch]$BackendOnly,
    [switch]$FrontendOnly
)

$ErrorActionPreference = "Stop"
$RepoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
if (-not $Pem) {
    $Pem = Join-Path (Split-Path $RepoRoot -Parent) "PracTI_Par_Claves.pem"
}
$JarLocal = Join-Path $RepoRoot "target\practicas-0.0.1-SNAPSHOT.jar"
$FrontendDist = Join-Path $RepoRoot "frontend\dist"
$SshTarget = "${User}@${Ec2Host}"
$SshArgs = @("-i", (Resolve-Path $Pem), "-o", "ConnectTimeout=20", "-o", "StrictHostKeyChecking=accept-new")

function Invoke-Ssh([string]$Command) {
    & ssh @SshArgs $SshTarget $Command
    if ($LASTEXITCODE -ne 0) { throw "SSH falló: $Command" }
}

function Invoke-Scp([string]$Source, [string]$RemoteDest) {
    & scp @SshArgs $Source "${SshTarget}:${RemoteDest}"
    if ($LASTEXITCODE -ne 0) { throw "SCP falló: $Source -> $RemoteDest" }
}

if (-not (Test-Path $Pem)) {
    throw "No se encontró la clave PEM: $Pem"
}

Write-Host "==> Preparando scripts en EC2..." -ForegroundColor Cyan
Invoke-Scp (Join-Path $PSScriptRoot "start-backend.sh") "~/start-backend.sh"
Invoke-Scp (Join-Path $PSScriptRoot "deploy-frontend.sh") "~/deploy-frontend.sh"
Invoke-Scp (Join-Path $PSScriptRoot "setup-nginx.sh") "~/setup-nginx.sh"
Invoke-Scp (Join-Path $PSScriptRoot "nginx-practicas.conf") "~/nginx-practicas.conf"
Invoke-Ssh "chmod +x ~/start-backend.sh ~/deploy-frontend.sh ~/setup-nginx.sh && sed -i 's/\r$//' ~/start-backend.sh ~/deploy-frontend.sh ~/setup-nginx.sh"
Invoke-Ssh "bash ~/setup-nginx.sh ~/nginx-practicas.conf"

if (-not $FrontendOnly) {
    if (-not $SkipBuild) {
        Write-Host "==> Compilando backend..." -ForegroundColor Cyan
        Push-Location $RepoRoot
        & .\mvnw.cmd -q package -DskipTests
        Pop-Location
    }
    if (-not (Test-Path $JarLocal)) { throw "No existe $JarLocal" }

    Write-Host "==> Subiendo JAR..." -ForegroundColor Cyan
    Invoke-Scp $JarLocal "~/app.jar"

    Write-Host "==> Reiniciando backend..." -ForegroundColor Cyan
    Invoke-Ssh "bash ~/start-backend.sh"
    Start-Sleep -Seconds 20
    Invoke-Ssh "grep 'Started PracticasApplication' ~/app.log | tail -n 1 || (tail -n 20 ~/app.log; exit 1)"
}

if (-not $BackendOnly) {
    if (-not $SkipBuild) {
        Write-Host "==> Compilando frontend..." -ForegroundColor Cyan
        Push-Location (Join-Path $RepoRoot "frontend")
        $env:VITE_API_URL = "http://${Ec2Host}"
        $env:VITE_USE_MOCKS = "false"
        & npm run build
        Pop-Location
    }
    if (-not (Test-Path (Join-Path $FrontendDist "index.html"))) {
        throw "No existe frontend/dist. Ejecuta npm run build."
    }

    Write-Host "==> Subiendo frontend..." -ForegroundColor Cyan
    Invoke-Ssh "rm -rf ~/frontend-dist && mkdir -p ~/frontend-dist"
    & scp @SshArgs -r (Join-Path $FrontendDist "*") "${SshTarget}:~/frontend-dist/"

    Write-Host "==> Publicando en nginx..." -ForegroundColor Cyan
    Invoke-Ssh "bash ~/deploy-frontend.sh ~/frontend-dist"
}

Write-Host ""
Write-Host "Despliegue OK" -ForegroundColor Green
Write-Host "  App:     http://${Ec2Host}/login"
Write-Host "  API:     http://${Ec2Host} (proxy nginx -> :8080)"
