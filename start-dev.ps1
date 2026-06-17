# Ingressify - Script de desenvolvimento local
# Uso: .\start-dev.ps1

$DOCKER = "C:\Program Files\Docker\Docker\resources\bin\docker.exe"
$COMPOSE = @("-f", "docker-compose.yml", "-f", "docker-compose.dev.yml")

Write-Host "=== Ingressify Dev (hot reload) ===" -ForegroundColor Cyan

# 1. Seed do banco
Write-Host "`n[1/2] Populando banco (seed)..." -ForegroundColor Yellow
& bash ./scripts/seed.sh --dev --seed-only
if ($LASTEXITCODE -ne 0) {
    Write-Host "Erro ao rodar seed." -ForegroundColor Red
    exit 1
}

# 2. Stack com hot reload
Write-Host "`n[2/2] Subindo stack com hot reload..." -ForegroundColor Yellow
& $DOCKER compose @COMPOSE up --build --watch
if ($LASTEXITCODE -ne 0) {
    Write-Host "Erro ao subir Docker. Verifique se o Docker Desktop esta rodando." -ForegroundColor Red
    exit 1
}
