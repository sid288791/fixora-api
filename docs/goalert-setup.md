# GoAlert Setup & Testing Guide

How to stand up GoAlert locally for Fixora's critical-alert escalation feature, and how to verify
it's actually working end to end (not just "the container started").

Background/design (why GoAlert, how it fits with Keep/OpenSRE): see the "GoAlert" section of the
main architecture docs. This file is just the practical setup + test checklist.

## Prerequisites

- Docker Desktop running, `fixora_postgres` and `fixora_redis` already up (`docker compose up -d
  postgres redis`).
- A Twilio account (a free trial account is enough to fully test this — trial SMS/calls just get a
  short "sent from trial account" prefix). You'll need: Account SID, Auth Token, and a Twilio phone
  number (Console → Phone Numbers → get a trial number if you don't have one yet).
- A tunnel tool so Twilio can reach GoAlert's webhooks — `ngrok` is what this was built/tested with.
  Free ngrok account + authtoken (ngrok.com → your dashboard → Auth). Each person needs their own;
  tunnel URLs and authtokens aren't shareable between machines.
- `fixora-api` running somewhere reachable (default local port `8090` if you're also running GoAlert,
  see "Port conflict" below).

## Setup

```bash
# 1. Start GoAlert (from fixora-api/)
docker compose up -d goalert

# 2. Start a tunnel to GoAlert's port 8081
docker run --rm -p 4040:4040 ngrok/ngrok http host.docker.internal:8081 --authtoken=<your-ngrok-authtoken>
# grab the https:// URL it prints, or: curl -s http://localhost:4040/api/tunnels

# 3. One script does the rest -- admin user, Public URL, Twilio config, Service +
#    Escalation Policy + Integration Key. Run from fixora-api/:
GOALERT_URL=https://<your-tunnel>.ngrok-free.app \
GOALERT_ADMIN_PASS=<choose-a-password> \
TWILIO_ACCOUNT_SID=<yours> \
TWILIO_AUTH_TOKEN=<yours> \
TWILIO_FROM_NUMBER=<yours, e.g. +15551234567> \
  ./scripts/setup-goalert.sh
```

It prints a webhook URL at the end, e.g.:
```
https://<your-tunnel>.ngrok-free.app/api/v2/generic/incoming?token=<key>
```
Keep it — you'll paste it into an Alert Configuration in step 6.

```bash
# 4. In the Twilio console (their UI): Phone Numbers -> Manage -> your number ->
#    set "A MESSAGE COMES IN" webhook to   <tunnel-url>/api/v2/twilio/message
#    set "A CALL COMES IN" webhook to      <tunnel-url>/api/v2/twilio/call

# 5. Open <tunnel-url> in a browser, log in (admin / the password you chose in step 3),
#    click your avatar -> Profile -> Contact Methods -> Create Contact Method:
#      Name: anything, Destination Type: Text Message (SMS), Phone Number: your own number
#    GoAlert texts you a verification code -- enter it in the dialog to confirm.

# 6. In the Fixora UI, create or edit an Alert Configuration:
#      severity: critical
#      goalertServiceUrl: the webhook URL from step 3
#        (if GOALERT_URL was a localhost address instead of a tunnel, swap "localhost" for
#        "host.docker.internal" in this URL -- Keep's http action runs inside a container
#        and needs to reach back out to the Docker host; not needed if you used a tunnel URL)
```

### Port conflict to know about

`fixora-api`'s default `server.port` is `8081` — the same port GoAlert listens on. If you're running
both locally (not just GoAlert in Docker), start `fixora-api` on a different port:
```bash
./gradlew bootRun --args='--server.port=8090'
```

### If `docker compose up -d goalert` fails to create its database

GoAlert needs its own Postgres database (`goalert_db`/`goalert_user`), bootstrapped via
`scripts/00-create-goalert-db.sql`. Postgres only runs that init script automatically on a **fresh**
(empty) `postgres_data` volume. If your `postgres_data` volume already existed from before GoAlert
was added to this repo, the goalert container will crash-loop with `password authentication failed
for user "goalert_user"`. Fix — create the DB manually once, against the running Postgres:
```bash
docker exec -i fixora_postgres psql -U fixora_user -d fixora_db <<'EOF'
DO $$ BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'goalert_user') THEN
      CREATE USER goalert_user WITH PASSWORD 'goalert_password';
   END IF;
END $$;
SELECT 'CREATE DATABASE goalert_db OWNER goalert_user'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'goalert_db')\gexec
GRANT ALL PRIVILEGES ON DATABASE goalert_db TO goalert_user;
EOF
docker restart fixora_goalert
```

## How to test it end-to-end

This is exactly the sequence used to verify the feature actually works (not just that the pieces
compile). Do all of it, in order:

**1. Confirm GoAlert itself is healthy and your phone is verified**
```bash
curl -s -o /dev/null -w "%{http_code}\n" https://<your-tunnel>.ngrok-free.app/health   # expect 200
```
And in the GoAlert UI, Profile → Contact Methods should show your number with no "unverified" badge.

**2. Create a real critical Alert Configuration via Fixora's API** (or the UI — either works;
this is the raw API call for a quick copy-paste test):
```bash
curl -s -X POST http://localhost:8090/api/applications/1/alert-configurations \
  -H "Content-Type: application/json" \
  -H "X-AD-Group: dev-team" \
  -d '{
    "applicationId": 1,
    "owningAdGrp": "dev-team",
    "name": "GoAlert Test",
    "description": "Verify GoAlert escalation",
    "alertType": "TEST",
    "severity": "critical",
    "goalertServiceUrl": "<the webhook URL from setup step 3>",
    "triggerAiInvestigation": false,
    "enabled": true
  }'
```
Note the returned `id` — you'll need it in step 3. Also check the `fixora-api` logs for a line like:
```
Successfully created Keep workflow with id: <workflow-id>
```
If you don't see that, the Keep workflow wasn't created and nothing downstream will fire — check
Keep's own health first (`curl http://localhost:8080/healthcheck`).

**3. Confirm the Keep workflow actually contains the escalation action** (catches config typos before
you waste time waiting for an SMS that was never going to come):
```bash
curl -s "http://localhost:8080/workflows/<workflow-id>" -H "X-API-Key: fixora-api-key" | grep goalert-escalation
```
Should show the `goalert-escalation` action with your webhook URL in it. If it's missing, re-check
that `severity` was exactly `"critical"` (case-insensitive but must match) and `goalertServiceUrl`
was non-blank when the config was created/updated.

**4. Fire a real test alert through Fixora:**
```bash
curl -s -X POST http://localhost:8090/api/applications/1/test-alert \
  -H "Content-Type: application/json" \
  -H "X-AD-Group: dev-team" \
  -d '{
    "alertConfigurationId": <id from step 2>,
    "notificationChannelId": 1,
    "title": "Test critical alert",
    "message": "Verifying GoAlert escalation end to end",
    "severity": "critical",
    "source": "FIXORA-TEST"
  }'
```

**5. Confirm the Keep workflow actually ran:**
```bash
curl -s "http://localhost:8080/workflows/<workflow-id>/runs" -H "X-API-Key: fixora-api-key"
```
Look for `"status":"success"` on a recent run. If it's missing entirely, Keep's alert-trigger didn't
match — check that the alert's `severity` field matches what the workflow trigger expects.

**6. Confirm GoAlert actually received it and paged you** — check GoAlert's container logs:
```bash
docker logs fixora_goalert --tail 30 | grep -E "Alert created|notification sent"
```
You're looking for two lines: `"Alert created."` (the webhook landed) and `"notification sent"` with
`DestType=builtin-twilio-sms` (Twilio was actually asked to send it).

**7. Check your phone.** You should receive an SMS. Acknowledge it (reply per the SMS instructions,
or ack it in the GoAlert UI under Alerts) so it doesn't keep re-escalating — the default Escalation
Policy repeats 3 times if left unacked.

If steps 1-6 all pass but no SMS arrives, the issue is on Twilio's side specifically (webhook URLs in
the Twilio console not pointing at your current tunnel URL is the most common cause — tunnel URLs
change every time you restart the tunnel, so re-check that after any tunnel restart).

## Syncing closures back from GoAlert

Paging is one-way by default: Keep calls GoAlert's generic incoming API to page someone, but
GoAlert has no outgoing webhook for status changes, so closing an alert in the GoAlert UI (or via
its own API) never reaches Keep or Fixora — the alert just sits there looking active on Fixora's
side forever.

`GoAlertSyncScheduler` (in `fixora-api`) closes that gap by polling GoAlert's GraphQL API on an
interval, checking for alerts closed on the configured Service, and resolving the matching alert
in Keep by fingerprint (the fingerprint is smuggled through GoAlert inside the alert's `details`
text, since GoAlert's generic API returns no body and exposes no free-form metadata field over
GraphQL to carry it any other way).

It's off by default (a fresh checkout has no `GOALERT_SERVICE_ID` set, so the scheduler no-ops
every tick). To turn it on, set on `fixora-api`:

```bash
GOALERT_API_URL=<same tunnel/URL used for GOALERT_URL above>
GOALERT_SERVICE_ID=<printed at the end of setup-goalert.sh, or Service -> Details -> ID in the GoAlert UI>
GOALERT_SYNC_ADMIN_USER=<a GoAlert admin username -- can reuse the one from step 3>
GOALERT_SYNC_ADMIN_PASS=<its password>
```

`setup-goalert.sh` prints all four after it creates the Service. Poll interval defaults to 30
seconds (`GOALERT_SYNC_POLL_INTERVAL_MS`).

To test: fire a critical test alert (step 4 above), confirm it's active in Fixora, close it in the
GoAlert UI (Alerts -> the alert -> Close), then within ~30s confirm the same alert shows up under
Fixora/Keep's "closed" bucket. `fixora-api` logs `Resolved Keep alert with fingerprint ... following
GoAlert closure` when it works.

## Known limitations (see also `docs/known-bugs.md` in the workspace root for broader Keep/GoAlert bugs)

- ngrok's free tier gives a new URL every restart — fine for testing, but means the Twilio webhook
  URLs and `goalertServiceUrl` need updating after every tunnel restart. A paid ngrok reserved domain
  (or a real deployment) removes this.
- `setup-goalert.sh` is not idempotent for the Service/Escalation Policy/Integration Key it creates —
  re-running it creates a second set with the same names. Fine for a one-time bootstrap; change
  `GOALERT_SERVICE_NAME` etc. if you need to run it again.
- Escalation Policy steps beyond the first (manager/director/etc.) still need real GoAlert User
  accounts created and swapped in manually — not something `setup-goalert.sh` can safely do, since it
  would require entering other people's phone numbers into a script/repo.
