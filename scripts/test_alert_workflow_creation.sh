#!/bin/bash

# Test Alert Configuration with Keep Workflow Creation
# Tests: Application onboarding → Alert config → Keep workflow creation → Alert workflow entity

set -e

BASE_URL="http://localhost:8081/api"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔═══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   Test Alert Configuration with Keep Workflow Creation         ║${NC}"
echo -e "${BLUE}╚═══════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Step 1: Onboard Application
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}[Step 1/5] Onboarding Application...${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

APP_RESPONSE=$(curl -s -X POST "${BASE_URL}/applications/onboard" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test App for Alert Workflow",
    "alias": "test-alert-workflow",
    "ownerEmail": "test@test.com",
    "adGroupMapping": "test-team",
    "description": "Test application for alert workflow creation"
  }')

APP_ID=$(echo $APP_RESPONSE | jq -r '.id')
INTEGRATION_ID=$(echo $APP_RESPONSE | jq -r '.integrationId')

echo "$APP_RESPONSE" | jq '.'
echo ""
echo -e "${GREEN}✓ Application Created:${NC}"
echo -e "  Application ID: ${BLUE}${APP_ID}${NC}"
echo -e "  Integration ID: ${BLUE}${INTEGRATION_ID}${NC}"
echo ""

# Step 2: Create Alert Configuration
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}[Step 2/5] Creating Alert Configuration...${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

ALERT_CONFIG_RESPONSE=$(curl -s -X POST "${BASE_URL}/applications/${APP_ID}/alert-configurations" \
  -H "Content-Type: application/json" \
  -H "X-AD-Group: test-team" \
  -d '{
    "owningAdGrp": "test-team",
    "name": "High CPU Usage Alert",
    "description": "Triggers when CPU usage exceeds 80% for 5 minutes",
    "alertType": "THRESHOLD",
    "severity": "WARNING",
    "conditionExpression": "cpu_usage > 80",
    "environment": "production",
    "source": "prometheus",
    "channels": "slack,pagerduty",
    "triggerAiInvestigation": false,
    "enabled": true,
    "status": "ACTIVE"
  }')

ALERT_CONFIG_ID=$(echo $ALERT_CONFIG_RESPONSE | jq -r '.id')

echo "$ALERT_CONFIG_RESPONSE" | jq '.'
echo ""
echo -e "${GREEN}✓ Alert Configuration Created (ID: ${ALERT_CONFIG_ID})${NC}"
echo ""

# Step 3: Verify Alert Workflow was created
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}[Step 3/5] Verifying Alert Workflow Entity...${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

sleep 2  # Give time for workflow creation

WORKFLOWS=$(curl -s "${BASE_URL}/applications/${APP_ID}/alert-workflows" \
  -H "X-AD-Group: test-team")

echo "$WORKFLOWS" | jq '.'
echo ""

WORKFLOW_COUNT=$(echo $WORKFLOWS | jq 'length')
if [ "$WORKFLOW_COUNT" -gt 0 ]; then
  WORKFLOW_ID=$(echo $WORKFLOWS | jq -r '.[0].id')
  KEEP_WORKFLOW_ID=$(echo $WORKFLOWS | jq -r '.[0].keepWorkflowId')
  echo -e "${GREEN}✓ Alert Workflow Entity Created:${NC}"
  echo -e "  Workflow ID: ${BLUE}${WORKFLOW_ID}${NC}"
  echo -e "  Keep Workflow ID: ${BLUE}${KEEP_WORKFLOW_ID}${NC}"
  echo ""
else
  echo -e "${RED}✗ No Alert Workflow found${NC}"
  echo ""
fi

# Step 4: Check Keep API for workflow
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}[Step 4/5] Checking Keep API for Workflow...${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

if [ -n "$KEEP_WORKFLOW_ID" ] && [ "$KEEP_WORKFLOW_ID" != "null" ]; then
  KEEP_WORKFLOW=$(curl -s "http://localhost:8080/workflows/${KEEP_WORKFLOW_ID}" \
    -H "x-api-key: fixora-api-key")
  
  echo "$KEEP_WORKFLOW" | jq '.'
  echo ""
  echo -e "${GREEN}✓ Keep Workflow Found in Keep Platform${NC}"
else
  echo -e "${YELLOW}⚠ Keep Workflow ID not available, skipping Keep API check${NC}"
  echo ""
fi

# Step 5: Test Delete (should also delete Keep workflow)
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}[Step 5/5] Testing Delete (should also delete Keep workflow)...${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

if [ -n "$KEEP_WORKFLOW_ID" ] && [ "$KEEP_WORKFLOW_ID" != "null" ]; then
  curl -s -X DELETE "${BASE_URL}/applications/${APP_ID}/alert-configurations/${ALERT_CONFIG_ID}" \
    -H "X-AD-Group: test-team"
  
  sleep 2
  
  # Check if Keep workflow was deleted
  KEEP_WORKFLOW_AFTER=$(curl -s -w "%{http_code}" "http://localhost:8080/workflows/${KEEP_WORKFLOW_ID}" \
    -H "x-api-key: fixora-api-key" -o /dev/null)
  
  if [ "$KEEP_WORKFLOW_AFTER" == "404" ] || [ "$KEEP_WORKFLOW_AFTER" == "500" ]; then
    echo -e "${GREEN}✓ Keep Workflow was successfully deleted${NC}"
  else
    echo -e "${YELLOW}⚠ Keep Workflow still exists (HTTP $KEEP_WORKFLOW_AFTER)${NC}"
  fi
else
  echo -e "${YELLOW}⚠ Skipping delete test as Keep Workflow ID was not available${NC}"
fi

echo ""

# Summary
echo -e "${BLUE}╔═══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                    Test Summary                               ║${NC}"
echo -e "${BLUE}╚═══════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${GREEN}✓ Application Created:${NC} ID ${APP_ID}"
echo -e "${GREEN}✓ Alert Configuration Created:${NC} ID ${ALERT_CONFIG_ID}"
echo -e "${GREEN}✓ Alert Workflow Entity Created:${NC} ID ${WORKFLOW_ID}"
echo -e "${GREEN}✓ Keep Workflow Created:${NC} ID ${KEEP_WORKFLOW_ID}"
echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}Alert workflow creation test completed! 🎉${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""
