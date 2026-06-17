#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

echo "=== Ingressify Dev (hot reload) ==="

echo ">> Rodando seed..."
"$ROOT/scripts/seed.sh" --dev --seed-only

docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build --watch
