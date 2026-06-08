#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

FRESH=false
if [[ "${1:-}" == "--fresh" ]]; then
  FRESH=true
fi

if $FRESH; then
  echo ">> Resetando banco (docker compose down -v)..."
  docker compose down -v
fi

echo ">> Subindo Postgres..."
docker compose up -d postgres

echo ">> Aguardando Postgres ficar healthy..."
until docker compose exec -T postgres pg_isready -U ingressify -d ingressify >/dev/null 2>&1; do
  sleep 1
done

echo ">> Rebuild do backend (inclui o seeder)..."
docker compose build backend

echo ">> Populando banco com dados de demonstração..."
docker compose run --rm --no-deps \
  -e SPRING_PROFILES_ACTIVE=desenvolvimento,seed \
  -e SEED_EXIT=true \
  backend

echo ">> Subindo stack completa..."
docker compose up -d

echo ""
echo "Seed concluído! Acesse:"
echo "  Frontend:  http://localhost:3000"
echo "  Backend:   http://localhost:8080"
echo ""
echo "Contas (senha: Senha123):"
echo "  admin@ingressify.local          — ADMIN"
echo "  maria@ingressify.local          — ORGANIZADOR"
echo "  joao@ingressify.local           — ORGANIZADOR"
echo "  carlos@ingressify.local         — OPERADOR_PORTA"
echo "  ana@ingressify.local            — COMPRADOR"
echo "  comprador1@ingressify.local ... comprador15@ingressify.local"
