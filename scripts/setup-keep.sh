#!/usr/bin/env bash
# One-time setup to get Keep (https://github.com/keephq/keep) running locally via
# docker-compose.yml's keep-backend/keep-frontend/keep-api-proxy/keep-websocket-server services.
#
# Why this script exists: docker-compose.yml's keep-backend/keep-frontend build contexts point at
# ./keep and ./keep/keep-ui, but that directory is NOT part of this repo (it's a full external
# clone, too large to vendor in git) — so a fresh checkout can't build those services until this
# runs once. See docs/keep.md for the full debugging story behind why each step below exists.
#
# Usage (from fixora-api/):
#   ./scripts/setup-keep.sh
set -euo pipefail

cd "$(dirname "$0")/.."

if [ -d "keep" ]; then
  echo "keep/ already exists, skipping clone. Delete it first if you want a fresh clone."
else
  echo "==> Cloning keephq/keep into ./keep ..."
  git clone --depth 1 https://github.com/keephq/keep.git keep
fi

echo "==> Normalizing line endings on shell scripts inside keep/ ..."
# On Windows, git checks these out with CRLF line endings, which corrupts the #!/bin/sh shebang
# line inside the Linux containers (fails as "not found" / "no such file or directory" at
# container startup or build time). This must run every time after (re-)cloning on Windows.
find keep -name "*.sh" -not -path "*/node_modules/*" -exec sed -i 's/\r$//' {} \;

echo "==> Building and starting Keep services (this can take several minutes on first run) ..."
docker compose up -d --build postgres redis keep-websocket-server keep-backend keep-api-proxy keep-frontend

echo "==> Waiting for keep-backend to become healthy ..."
until curl -sf http://localhost:8080/healthcheck > /dev/null 2>&1; do
  sleep 3
done

echo ""
echo "Done. Keep UI:      http://localhost:3000"
echo "      Keep backend: http://localhost:8080 (proxied at http://localhost:8082 with API key)"
echo ""
echo "If keep-backend keeps restarting, it's almost certainly the CRLF issue on a script this"
echo "script didn't catch (e.g. after Keep upstream adds a new .sh file) — rerun this script, or"
echo "manually: docker compose build --no-cache keep-backend && docker compose up -d keep-backend"
