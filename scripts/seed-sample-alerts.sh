#!/bin/sh
# Creates two sample Alert Configurations on the seeded "Sample App" (application id 1,
# see scripts/init.sql) so a fresh setup has something real to click through and test
# immediately, instead of an empty dashboard.
#
# Must run AFTER fixora-api and Keep are both up (this calls the live API, which in turn
# calls Keep to create a workflow per config -- that's why this can't just be a SQL insert
# like the Application seed in init.sql).
#
# Usage (from fixora-api/):
#   FIXORA_API_URL=http://localhost:8083 ./scripts/seed-sample-alerts.sh
set -e

FIXORA_API_URL="${FIXORA_API_URL:-http://localhost:8083}"
APP_ID="${FIXORA_SAMPLE_APP_ID:-1}"

echo "==> Waiting for fixora-api at ${FIXORA_API_URL}..."
until curl -sf -o /dev/null "${FIXORA_API_URL}/api/applications"; do
  sleep 2
done

echo "==> Creating sample Alert Configuration: 'Sample AI Investigation' (AI toggle on, any severity)..."
curl -s -X POST "${FIXORA_API_URL}/api/applications/${APP_ID}/alert-configurations" \
  -H "Content-Type: application/json" \
  -H "X-AD-Group: dev-team" \
  -d '{
    "applicationId": '"${APP_ID}"',
    "owningAdGrp": "dev-team",
    "name": "Sample AI Investigation",
    "description": "Fires the AI root-cause-analysis flow on any alert sent against this config. Requires the fixora-rca-ai server running on :8090 with a real GROQ_API_KEY -- see GETTING_STARTED.md.",
    "alertType": "APPLICATION_ERROR",
    "severity": "warning",
    "triggerAiInvestigation": true,
    "enabled": true
  }'
echo ""

echo "==> Creating sample Alert Configuration: 'Sample Critical Escalation' (severity critical, GoAlert paging)..."
echo "    NOTE: goalertServiceUrl is left blank -- paste your own webhook URL from"
echo "    setup-goalert.sh's output into this config via the Fixora UI before testing it,"
echo "    otherwise the escalation action is simply skipped (by design, see docs/goalert-setup.md)."
curl -s -X POST "${FIXORA_API_URL}/api/applications/${APP_ID}/alert-configurations" \
  -H "Content-Type: application/json" \
  -H "X-AD-Group: dev-team" \
  -d '{
    "applicationId": '"${APP_ID}"',
    "owningAdGrp": "dev-team",
    "name": "Sample Critical Escalation",
    "description": "Pages on-call via GoAlert/Twilio SMS when fired. Set goalertServiceUrl in the Fixora UI before testing -- see GETTING_STARTED.md.",
    "alertType": "SYSTEM_OUTAGE",
    "severity": "critical",
    "triggerAiInvestigation": false,
    "enabled": true
  }'
echo ""

echo "==> Done. Two sample Alert Configurations created on Sample App (application id ${APP_ID})."
echo "    Open the Fixora UI -> Sample App -> Alert Configs to see them, or fire a test alert:"
echo "    curl -X POST ${FIXORA_API_URL}/api/applications/${APP_ID}/test-alert \\"
echo "      -H 'Content-Type: application/json' -H 'X-AD-Group: dev-team' \\"
echo "      -d '{\"alertConfigurationId\": <id-from-above>, \"notificationChannelId\": 1, \"title\": \"Test\", \"message\": \"hello\", \"severity\": \"warning\", \"source\": \"MANUAL-TEST\"}'"
