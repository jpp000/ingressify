#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

FRESH=false
DEV=false
SEED_ONLY=false

for arg in "$@"; do
  case "$arg" in
    --fresh) FRESH=true ;;
    --dev) DEV=true ;;
    --seed-only) SEED_ONLY=true ;;
    *)
      echo "Uso: $0 [--fresh] [--dev] [--seed-only]"
      exit 1
      ;;
  esac
done

COMPOSE=(docker compose -f docker-compose.yml)
if $DEV; then
  COMPOSE+=(-f docker-compose.dev.yml)
fi

if $FRESH; then
  echo ">> Resetando banco (docker compose down -v)..."
  "${COMPOSE[@]}" down -v
fi

echo ">> Subindo Postgres..."
"${COMPOSE[@]}" up -d postgres

echo ">> Aguardando Postgres ficar healthy..."
until "${COMPOSE[@]}" exec -T postgres pg_isready -U ingressify -d ingressify >/dev/null 2>&1; do
  sleep 1
done

echo ">> Rebuild do backend (inclui o seeder)..."
"${COMPOSE[@]}" build backend

echo ">> Populando banco com dados de demonstração..."
"${COMPOSE[@]}" run --rm --no-deps \
  -e SPRING_PROFILES_ACTIVE=desenvolvimento,seed \
  -e SEED_EXIT=true \
  backend

if $SEED_ONLY; then
  echo ">> Seed concluído."
  exit 0
fi

echo ">> Subindo stack completa..."
"${COMPOSE[@]}" up -d

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
