# Ingressify - Script de desenvolvimento local
# Uso: .\start-dev.ps1

$DOCKER = "C:\Program Files\Docker\Docker\resources\bin\docker.exe"
$NODE_PATH = "C:\nvm4w\nodejs"

Write-Host "=== Ingressify Dev ===" -ForegroundColor Cyan

# 1. Sobe PostgreSQL + Backend via Docker
Write-Host "`n[1/2] Subindo backend (Docker Compose)..." -ForegroundColor Yellow
& $DOCKER compose up -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "Erro ao subir Docker. Verifique se o Docker Desktop esta rodando." -ForegroundColor Red
    exit 1
}

Write-Host "Aguardando backend inicializar (20s)..." -ForegroundColor Gray
Start-Sleep -Seconds 20

$logs = & $DOCKER logs ingressify-backend-1 2>&1 | Select-Object -Last 5
if ($logs -match "Started IngressifyApplication") {
    Write-Host "Backend OK - http://localhost:8080" -ForegroundColor Green
} else {
    Write-Host "Backend ainda iniciando ou com erro. Verifique:" -ForegroundColor Yellow
    Write-Host "  $DOCKER logs ingressify-backend-1" -ForegroundColor Gray
}

# 2. Sobe Frontend
Write-Host "`n[2/2] Subindo frontend (npm run dev)..." -ForegroundColor Yellow
$env:PATH = "$NODE_PATH;" + $env:PATH
Start-Process -FilePath "cmd.exe" -ArgumentList "/c","set PATH=$NODE_PATH;%PATH% && cd apresentacao-frontend && npm run dev" -WindowStyle Normal

Write-Host "`n=== Stack iniciada ===" -ForegroundColor Cyan
Write-Host "  Frontend:  http://localhost:3000" -ForegroundColor Green
Write-Host "  Backend:   http://localhost:8080" -ForegroundColor Green
Write-Host "  Banco:     localhost:5432 (ingressify/ingressify)" -ForegroundColor Green
Write-Host "`nPara parar: docker compose down" -ForegroundColor Gray
