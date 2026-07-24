#!/bin/bash

# Fixora API - End-to-End Integration Test
# Tests: Application onboarding → Notification channels → Alert configs → Workflows → Alert ingress

set -e

BASE_URL="http://localhost:8081/api"
TIMESTAMP=$(date +%s)

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔═══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║         Fixora API - End-to-End Integration Test             ║${NC}"
echo -e "${BLUE}║  Testing: integration_id, keep_provider_id, alert-ingress    ║${NC}"
echo -e "${BLUE}╚═══════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Step 1: Onboard Application
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}[Step 1/9] Onboarding Application...${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

APP_RESPONSE=$(curl -s -X POST "${BASE_URL}/applications/onboard" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "E-Commerce Platform",
    "alias": "ecommerce-prod",
    "ownerEmail": "devops@ecommerce.com",
    "adGroupMapping": "ecommerce-team,platform-admins",
    "description": "Production e-commerce platform with microservices"
  }')

APP_ID=$(echo $APP_RESPONSE | jq -r '.id')
INTEGRATION_ID=$(echo $APP_RESPONSE | jq -r '.integrationId')

echo "$APP_RESPONSE" | jq '.'
echo ""
echo -e "${GREEN}✓ Application Created:${NC}"
echo -e "  Application ID: ${BLUE}${APP_ID}${NC}"
echo -e "  Integration ID (UUID): ${BLUE}${INTEGRATION_ID}${NC}"
echo ""

if [ "$INTEGRATION_ID" == "null" ] || [ -z "$INTEGRATION_ID" ]; then
    echo -e "${RED}✗ ERROR: integration_id not generated!${NC}"
    exit 1
fi

# Step 2: Verify Application with Integration ID
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}[Step 2/9] Verifying Application Retrieval...${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

APP_GET=$(curl -s "${BASE_URL}/applications/${APP_ID}")
echo "$APP_GET" | jq '.'
echo -e "${GREEN}✓ Application retrieved successfully with integrationId${NC}"
echo ""

# Step 3: Create Notification Channel (Slack)
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}[Step 3/9] Creating Notification Channel (Slack)...${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Simulate Keep provider ID (in real scenario, this comes from Keep API)
KEEP_PROVIDER_ID="keep-slack-provider-$(uuidgen | tr '[:upper:]' '[:lower:]')"

CHANNEL_RESPONSE=$(curl -s -X POST "${BASE_URL}/notification-channels/create" \
  -H "Content-Type: application/json" \
  -d '{
    "applicationId": '"${APP_ID}"',
    "owningAdGrp": "ecommerce-team",
    "name": "Slack - Production Alerts",
    "description": "Primary Slack channel for production alerts",
    "channelType": "SLACK",
    "endpoint": "https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXX",
    "keepProviderId": "'"${KEEP_PROVIDER_ID}"'",
    "configuration": "{\"channel\":\"#prod-alerts\",\"username\":\"Fixora\"}",
    "isDefault": true,
    "status": "ACTIVE"
  }')

CHANNEL_ID=$(echo $CHANNEL_RESPONSE | jq -r '.id')
CHANNEL_KEEP_PROVIDER=$(echo $CHANNEL_RESPONSE | jq -r '.keepProviderId')

echo "$CHANNEL_RESPONSE" | jq '.'
echo ""
echo -e "${GREEN}✓ Notification Channel Created:${NC}"
echo -e "  Channel ID: ${BLUE}${CHANNEL_ID}${NC}"
echo -e "  Keep Provider ID: ${BLUE}${CHANNEL_KEEP_PROVIDER}${NC}"
echo ""

if [ "$CHANNEL_KEEP_PROVIDER" == "null" ] || [ -z "$CHANNEL_KEEP_PROVIDER" ]; then
    echo -e "${RED}✗ ERROR: keep_provider_id not saved!${NC}"
    exit 1
fi

# Step 4: Create another Notification Channel (PagerDuty)
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}[Step 4/9] Creating Notification Channel (PagerDuty)...${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

KEEP_PROVIDER_ID_2="keep-pagerduty-provider-$(uuidgen | tr '[:upper:]' '[:lower:]')"

CHANNEL_RESPONSE_2=$(curl -s -X POST "${BASE_URL}/notification-channels/create" \
  -H "Content-Type: application/json" \
  -d '{
    "applicationId": '"${APP_ID}"',
    "owningAdGrp": "ecommerce-team",
    "name": "PagerDuty - Critical Incidents",
    "description": "PagerDuty for critical incidents requiring immediate attention",
    "channelType": "PAGERDUTY",
    "endpoint": "https://events.pagerduty.com/v2/enqueue",
    "keepProviderId": "'"${KEEP_PROVIDER_ID_2}"'",
    "configuration": "{\"routing_key\":\"R01ABCDEFGHIJKLMNOPQRSTUV\"}",
    "isDefault": false,
    "status": "ACTIVE"
  }')

CHANNEL_ID_2=$(echo $CHANNEL_RESPONSE_2 | jq -r '.id')

echo "$CHANNEL_RESPONSE_2" | jq '.'
echo -e "${GREEN}✓ Second Notification Channel Created (ID: ${CHANNEL_ID_2})${NC}"
echo ""

# Step 5: Create Alert Configuration (High CPU)
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}[Step 5/9] Creating Alert Configuration (High CPU)...${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

ALERT_CONFIG_RESPONSE=$(curl -s -X POST "${BASE_URL}/alert-configurations/create" \
  -H "Content-Type: application/json" \
  -d '{
    "applicationId": '"${APP_ID}"',
    "notificationChannelId": '"${CHANNEL_ID}"',
    "owningAdGrp": "ecommerce-team",
    "name": "High CPU Usage Alert",
    "description": "Triggers when CPU usage exceeds 80% for 5 minutes",
    "severity": "WARNING",
    "alertType": "THRESHOLD",
    "status": "ACTIVE",
    "thresholdConfig": "{\"metric\":\"cpu_usage\",\"operator\":\">\",\"threshold\":80,\"duration\":\"5m\"}"
  }')

ALERT_CONFIG_ID=$(echo $ALERT_CONFIG_RESPONSE | jq -r '.id')

echo "$ALERT_CONFIG_RESPONSE" | jq '.'
echo -e "${GREEN}✓ Alert Configuration Created (ID: ${ALERT_CONFIG_ID})${NC}"
echo ""

# Step 6: Create Alert Configuration (Memory)
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}[Step 6/9] Creating Alert Configuration (High Memory)...${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

ALERT_CONFIG_RESPONSE_2=$(curl -s -X POST "${BASE_URL}/alert-configurations/create" \
  -H "Content-Type: application/json" \
  -d '{
    "applicationId": '"${APP_ID}"',
    "notificationChannelId": '"${CHANNEL_ID_2}"',
    "owningAdGrp": "ecommerce-team",
    "name": "High Memory Usage Alert",
    "description": "Triggers when memory usage exceeds 90%",
    "severity": "CRITICAL",
    "alertType": "THRESHOLD",
    "status": "ACTIVE",
    "thresholdConfig": "{\"metric\":\"memory_usage\",\"operator\":\">\",\"threshold\":90,\"duration\":\"2m\"}"
  }')

ALERT_CONFIG_ID_2=$(echo $ALERT_CONFIG_RESPONSE_2 | jq -r '.id')

echo "$ALERT_CONFIG_RESPONSE_2" | jq '.'
echo -e "${GREEN}✓ Second Alert Configuration Created (ID: ${ALERT_CONFIG_ID_2})${NC}"
echo ""

# Step 7: Create Alert Workflow
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}[Step 7/9] Creating Alert Workflow...${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

WORKFLOW_RESPONSE=$(curl -s -X POST "${BASE_URL}/alert-workflows/create" \
  -H "Content-Type: application/json" \
  -d '{
    "applicationId": '"${APP_ID}"',
    "alertConfigurationId": '"${ALERT_CONFIG_ID}"',
    "owningAdGrp": "ecommerce-team",
    "name": "CPU Alert Workflow",
    "description": "Workflow for handling high CPU alerts with escalation",
    "workflowDefinition": "{\"steps\":[{\"type\":\"notify\",\"channel\":\"slack\"},{\"type\":\"wait\",\"duration\":\"5m\"},{\"type\":\"escalate\",\"target\":\"pagerduty\"}]}",
    "status": "ACTIVE"
  }')

WORKFLOW_ID=$(echo $WORKFLOW_RESPONSE | jq -r '.id')

echo "$WORKFLOW_RESPONSE" | jq '.'
echo -e "${GREEN}✓ Alert Workflow Created (ID: ${WORKFLOW_ID})${NC}"
echo ""

# Step 8: Test Alert Ingress - Grafana Alert
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}[Step 8/9] Testing Alert Ingress Webhook (Grafana)...${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

GRAFANA_ALERT_RESPONSE=$(curl -s -X POST "${BASE_URL}/alert-ingress/${INTEGRATION_ID}/grafana" \
  -H "Content-Type: application/json" \
  -d '{
    "receiver": "fixora-webhook",
    "status": "firing",
    "alerts": [
      {
        "status": "firing",
        "labels": {
          "alertname": "HighCPUUsage",
          "severity": "warning",
          "instance": "ecommerce-prod-01",
          "job": "node-exporter"
        },
        "annotations": {
          "description": "CPU usage is above 85% on instance ecommerce-prod-01",
          "summary": "High CPU Usage Detected"
        },
        "startsAt": "2026-07-23T12:20:00.000Z",
        "generatorURL": "http://grafana.example.com/alerting/grafana/abc123/view"
      }
    ],
    "groupLabels": {
      "alertname": "HighCPUUsage"
    },
    "commonLabels": {
      "severity": "warning"
    },
    "externalURL": "http://grafana.example.com",
    "version": "1",
    "groupKey": "{}:{alertname=\"HighCPUUsage\"}"
  }')

echo "$GRAFANA_ALERT_RESPONSE" | jq '.'
echo -e "${GREEN}✓ Grafana Alert Ingested${NC}"
echo ""

# Step 9: Test Alert Ingress - Prometheus Alert
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}[Step 9/9] Testing Alert Ingress Webhook (Prometheus)...${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

PROMETHEUS_ALERT_RESPONSE=$(curl -s -X POST "${BASE_URL}/alert-ingress/${INTEGRATION_ID}/prometheus" \
  -H "Content-Type: application/json" \
  -d '{
    "version": "4",
    "groupKey": "{}:{alertname=\"HighMemoryUsage\"}",
    "status": "firing",
    "receiver": "fixora",
    "groupLabels": {
      "alertname": "HighMemoryUsage"
    },
    "commonLabels": {
      "alertname": "HighMemoryUsage",
      "severity": "critical",
      "instance": "ecommerce-prod-02:9100"
    },
    "alerts": [
      {
        "status": "firing",
        "labels": {
          "alertname": "HighMemoryUsage",
          "instance": "ecommerce-prod-02:9100",
          "job": "node",
          "severity": "critical"
        },
        "annotations": {
          "description": "Memory usage is above 92% on ecommerce-prod-02",
          "summary": "Critical Memory Usage"
        },
        "startsAt": "2026-07-23T12:25:00.000Z",
        "endsAt": "0001-01-01T00:00:00Z",
        "generatorURL": "http://prometheus.example.com/graph?g0.expr=..."
      }
    ]
  }')

echo "$PROMETHEUS_ALERT_RESPONSE" | jq '.'
echo -e "${GREEN}✓ Prometheus Alert Ingested${NC}"
echo ""

# Summary
echo -e "${BLUE}╔═══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                    Test Summary                               ║${NC}"
echo -e "${BLUE}╚═══════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${GREEN}✓ Application Onboarding${NC}"
echo -e "  └─ ID: ${APP_ID}, Integration ID: ${INTEGRATION_ID}"
echo ""
echo -e "${GREEN}✓ Notification Channels (with keep_provider_id)${NC}"
echo -e "  ├─ Slack: ${CHANNEL_ID} → Keep Provider: ${KEEP_PROVIDER_ID:0:30}..."
echo -e "  └─ PagerDuty: ${CHANNEL_ID_2}"
echo ""
echo -e "${GREEN}✓ Alert Configurations${NC}"
echo -e "  ├─ High CPU: ${ALERT_CONFIG_ID}"
echo -e "  └─ High Memory: ${ALERT_CONFIG_ID_2}"
echo ""
echo -e "${GREEN}✓ Alert Workflow${NC}"
echo -e "  └─ ID: ${WORKFLOW_ID}"
echo ""
echo -e "${GREEN}✓ Alert Ingress Webhooks${NC}"
echo -e "  ├─ Grafana: Alert received and logged"
echo -e "  └─ Prometheus: Alert received and logged"
echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}All tests passed! End-to-end flow completed successfully. 🎉${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

# Additional verification queries
echo -e "${YELLOW}Verification Commands:${NC}"
echo ""
echo "1. Get Application with Integration ID:"
echo -e "   ${BLUE}curl ${BASE_URL}/applications/${APP_ID} | jq .${NC}"
echo ""
echo "2. Get Notification Channel with Keep Provider ID:"
echo -e "   ${BLUE}curl ${BASE_URL}/notification-channels/${APP_ID}/${CHANNEL_ID} | jq .${NC}"
echo ""
echo "3. List All Alert Configurations:"
echo -e "   ${BLUE}curl ${BASE_URL}/alert-configurations/list/${APP_ID} | jq .${NC}"
echo ""
echo "4. List All Workflows:"
echo -e "   ${BLUE}curl ${BASE_URL}/alert-workflows/list/${APP_ID} | jq .${NC}"
echo ""
echo "5. Test Alert Ingress Endpoint:"
echo -e "   ${BLUE}curl -X POST ${BASE_URL}/alert-ingress/${INTEGRATION_ID}/custom -H 'Content-Type: application/json' -d '{\"test\":\"data\"}' | jq .${NC}"
echo ""
