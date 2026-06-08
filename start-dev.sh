#!/usr/bin/env bash
set -euo pipefail

echo "=== Ingressify Dev (hot reload) ==="
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build --watch
