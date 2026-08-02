#!/bin/sh
# Bootstraps GoAlert for local dev: starts the container, creates an admin user,
# sets the Public URL + Twilio config, and provisions a default Service +
# Escalation Policy (1 step -> the admin user) + a Generic API Integration Key
# -- all via GoAlert's GraphQL API. Nothing here requires touching the Admin UI.
#
# What this still does NOT (and can't) automate:
#   - Signing up for a Twilio account -- that's a real external account only you
#     can create. This script just takes the SID/token/number you already have
#     and pushes them into GoAlert's config for you.
#   - The tunnel itself (ngrok or similar) -- you still need to start one and
#     pass its URL in as GOALERT_URL, since Twilio needs a real internet-
#     reachable address to call back into GoAlert's webhooks.
#
# Usage (from fixora-api/, after `docker compose up -d goalert` and after
# starting a tunnel, e.g. `docker run --rm -p 4040:4040 ngrok/ngrok http
# host.docker.internal:8081 --authtoken=<your-ngrok-token>`):
#
#   GOALERT_URL=https://<your-tunnel>.ngrok-free.app \
#   GOALERT_ADMIN_PASS=<choose-a-password> \
#   TWILIO_ACCOUNT_SID=AC... \
#   TWILIO_AUTH_TOKEN=... \
#   TWILIO_FROM_NUMBER=+1..." \
#   ./scripts/setup-goalert.sh
#
# Every credential above is read from your own environment -- never hardcode
# them in this file or commit a .env containing them.
#
# Prints the Generic API webhook URL at the end -- paste that into an
# AlertConfiguration's goalertServiceUrl field in the Fixora UI (swap
# "localhost" for "host.docker.internal" in the URL only if GOALERT_URL was a
# localhost address, since Keep's http action runs inside a container).
set -e

GOALERT_URL="${GOALERT_URL:-http://localhost:8081}"
GOALERT_ADMIN_USER="${GOALERT_ADMIN_USER:-admin}"
GOALERT_ADMIN_PASS="${GOALERT_ADMIN_PASS:-changeme123}"
GOALERT_ADMIN_EMAIL="${GOALERT_ADMIN_EMAIL:-admin@fixora.local}"
SERVICE_NAME="${GOALERT_SERVICE_NAME:-Fixora Critical Alerts}"
POLICY_NAME="${GOALERT_POLICY_NAME:-Fixora Critical}"
KEY_NAME="${GOALERT_KEY_NAME:-Fixora Keep Webhook}"
DB_URL="${GOALERT_DB_URL:-postgres://goalert_user:goalert_password@postgres:5432/goalert_db?sslmode=disable}"

echo "==> Waiting for GoAlert to be healthy at ${GOALERT_URL}..."
until curl -sf -o /dev/null "${GOALERT_URL}/health"; do
  sleep 2
done

echo "==> Creating admin user '${GOALERT_ADMIN_USER}' (ignore 'already exists' errors)..."
docker exec fixora_goalert goalert add-user \
  --db-url "${DB_URL}" \
  --admin \
  --user "${GOALERT_ADMIN_USER}" \
  --email "${GOALERT_ADMIN_EMAIL}" \
  --pass "${GOALERT_ADMIN_PASS}" || echo "    (user likely already exists, continuing)"

echo "==> Logging in to get a session token..."
TOKEN=$(curl -s -XPOST -H "Referer: ${GOALERT_URL}" \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode "username=${GOALERT_ADMIN_USER}" \
  --data-urlencode "password=${GOALERT_ADMIN_PASS}" \
  "${GOALERT_URL}/api/v2/identity/providers/basic?noRedirect=1")

if [ -z "$TOKEN" ]; then
  echo "ERROR: login failed, no token returned. Check GOALERT_ADMIN_USER/GOALERT_ADMIN_PASS." >&2
  exit 1
fi

echo "==> Setting General.PublicURL to ${GOALERT_URL}..."
curl -s "${GOALERT_URL}/api/graphql" \
  -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" \
  -d "{\"query\":\"mutation(\$input:[ConfigValueInput!]!){ setConfig(input:\$input) }\",\"variables\":{\"input\":[{\"id\":\"General.PublicURL\",\"value\":\"${GOALERT_URL}\"}]}}" > /dev/null

if [ -n "${TWILIO_ACCOUNT_SID}" ] && [ -n "${TWILIO_AUTH_TOKEN}" ] && [ -n "${TWILIO_FROM_NUMBER}" ]; then
  echo "==> Configuring Twilio (SID/token/number from your environment)..."
  curl -s "${GOALERT_URL}/api/graphql" \
    -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" \
    -d "{\"query\":\"mutation(\$input:[ConfigValueInput!]!){ setConfig(input:\$input) }\",\"variables\":{\"input\":[{\"id\":\"Twilio.Enable\",\"value\":\"true\"},{\"id\":\"Twilio.AccountSID\",\"value\":\"${TWILIO_ACCOUNT_SID}\"},{\"id\":\"Twilio.AuthToken\",\"value\":\"${TWILIO_AUTH_TOKEN}\"},{\"id\":\"Twilio.FromNumber\",\"value\":\"${TWILIO_FROM_NUMBER}\"}]}}" > /dev/null
  echo "    Twilio enabled. Still needed manually in the Twilio console (their side, not ours):"
  echo "    set the phone number's messaging/voice webhooks to"
  echo "    ${GOALERT_URL}/api/v2/twilio/message and ${GOALERT_URL}/api/v2/twilio/call"
else
  echo "==> Skipping Twilio config (TWILIO_ACCOUNT_SID / TWILIO_AUTH_TOKEN / TWILIO_FROM_NUMBER not all set)."
fi

echo "==> Fetching admin user ID..."
USER_ID=$(curl -s "${GOALERT_URL}/api/graphql" \
  -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" \
  -d '{"query":"{ user { id } }"}' | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')

echo "==> Creating Service '${SERVICE_NAME}' + Escalation Policy '${POLICY_NAME}' + Integration Key '${KEY_NAME}'..."
RESULT=$(curl -s "${GOALERT_URL}/api/graphql" \
  -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" \
  -d "{\"query\":\"mutation(\$input: CreateServiceInput!){ createService(input:\$input){ id name integrationKeys{ id name href } escalationPolicy{ id name } } }\",\"variables\":{\"input\":{\"name\":\"${SERVICE_NAME}\",\"newEscalationPolicy\":{\"name\":\"${POLICY_NAME}\",\"repeat\":3,\"steps\":[{\"delayMinutes\":15,\"targets\":[{\"id\":\"${USER_ID}\",\"type\":\"user\"}]}]},\"newIntegrationKeys\":[{\"type\":\"generic\",\"name\":\"${KEY_NAME}\"}]}}}")

echo "$RESULT"
WEBHOOK_URL=$(echo "$RESULT" | sed -n 's/.*"href":"\([^"]*\)".*/\1/p')

echo ""
echo "==> Done. Paste this into an AlertConfiguration's goalertServiceUrl field in the Fixora UI:"
echo "    ${WEBHOOK_URL}"
echo ""
echo "    If GOALERT_URL was a localhost address, swap 'localhost' for 'host.docker.internal'"
echo "    in that URL first -- Keep's http action runs inside a container and needs to reach"
echo "    back out to the Docker host. See docs/goalert-setup.md."
echo ""
echo "==> Last manual step (your own phone, not scriptable): open ${GOALERT_URL}, log in as"
echo "    ${GOALERT_ADMIN_USER}, go to Profile -> Contact Methods, and add + verify your phone"
echo "    number -- GoAlert needs your explicit consent/verification before it will page you."
