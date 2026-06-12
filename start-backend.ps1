# Script para iniciar el backend de Prácticas
# Este script abre una nueva ventana de PowerShell y ejecuta el backend

$scriptBlock = {
    cd "C:\Users\ESTEFANY\Documents\practicas-backend"
    mvn spring-boot:run
}

# Ejecutar en una nueva ventana de PowerShell
Start-Process powershell -ArgumentList "-NoExit", "-Command", $scriptBlock -WindowStyle Normal

