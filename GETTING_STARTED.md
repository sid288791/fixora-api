# Getting Started

A complete, in-order walkthrough for standing up Fixora from a fresh clone — three services, three
repos (see "Repos you need" below), one working critical-alert-with-AI-RCA pipeline at the end.

If you just want the architecture explanation, see the README's Documentation section instead. This
file is purely "do this, in this order."

## Repos you need

- **`fixora-api`** (this repo) — the backend, Keep, GoAlert.
- **[`fixora-ui`](https://github.com/sid288791/fixora-ui)** — the dashboard.
- **[`fixora-rca-ai`](https://github.com/sid288791/fixora-rca-ai)** — the AI root-cause-analysis
  server Keep calls into. Separate repo, not vendored here.

Clone all three as siblings, e.g.:
```
some-folder/
├── fixora-api/
├── fixora-ui/
└── fixora-rca-ai/
```

## 1. Start the base infrastructure

```bash
cd fixora-api
docker compose up -d postgres redis
```
This auto-creates the `fixora_db` database and seeds one sample application ("Sample App", id 1) —
see `scripts/init.sql`.

## 2. Start Keep

```bash
./scripts/setup-keep.sh
```
Clones Keep, fixes a Windows line-ending issue, and brings up its containers. Full details in the
comment block above the `keep-*` services in `docker-compose.yml`. Verify: `curl
http://localhost:8080/healthcheck`.

## 3. Start fixora-api

```bash
./gradlew bootRun
```
Runs on port **8083** (`http://localhost:8083/api`). If this port is already used by something else
on your machine, override with `./gradlew bootRun --args='--server.port=<port>'` — but then also
update the `BASE_URL` constants in `fixora-ui` (see step 5) to match, they're currently hardcoded.

**No API key needed here** — `fixora-api` itself doesn't call any external LLM directly.

## 4. Start the AI RCA server (separate repo)

```bash
cd ../fixora-rca-ai
python -m venv .venv
./.venv/Scripts/python.exe -m pip install -r requirements.txt   # Windows
# source .venv/bin/activate && pip install -r requirements.txt  # macOS/Linux
```

**This is where you add your API key.** Create `.env` in `fixora-rca-ai/` (gitignored, never commit
it):
```
GROQ_API_KEY=<your real Groq API key>
GROQ_MODEL=openai/gpt-oss-120b
HOST=0.0.0.0
PORT=8090
```
Get a Groq key free at [console.groq.com](https://console.groq.com) → API Keys.

Run it:
```bash
./.venv/Scripts/python.exe -m uvicorn server:app --host 0.0.0.0 --port 8090
```
Verify: `curl http://localhost:8090/healthcheck` → `{"status":"ok","service":"fixora-rca-ai",...}`.
Keep's workflow calls this at `http://host.docker.internal:8090/investigate` — the port must be
`8090` and this must run on the same machine as Docker Desktop (see `fixora-rca-ai`'s own README for
why).

## 5. Start fixora-ui

```bash
cd ../fixora-ui
corepack enable   # or: node .yarn/releases/yarn-4.13.0.cjs --version, to confirm Yarn Berry works
PORT=3001 yarn start
```
Runs on `http://localhost:3001` (backend on `7007`). Port `3001` instead of Backstage's default
`3000` because Keep's frontend already owns `3000` if you're running both on one machine.

## 6. Seed sample Alert Configurations (optional but recommended)

```bash
cd ../fixora-api
FIXORA_API_URL=http://localhost:8083 ./scripts/seed-sample-alerts.sh
```
Creates two ready-to-test Alert Configurations on the seeded "Sample App":
- **"Sample AI Investigation"** — AI toggle already on, any severity. Fire an alert against it and
  the RCA will show up in the dashboard.
- **"Sample Critical Escalation"** — severity `critical`, ready for you to add a GoAlert URL to (step
  8) and test SMS paging.

## 7. Call the alert API / enable the AI toggle

Two ways to trigger an alert and see the AI RCA:

**Via the UI**: Fixora UI → Sample App → Alert Configs → open "Sample AI Investigation" → confirm
"Trigger AI Investigation" is toggled on (it already is, from the seed script) → go to Active Alerts
→ use the "Send Test Alert" flow, or call the API below.

**Via the API directly**:
```bash
curl -X POST http://localhost:8083/api/applications/1/test-alert \
  -H "Content-Type: application/json" -H "X-AD-Group: dev-team" \
  -d '{
    "alertConfigurationId": <id from the seed script output>,
    "notificationChannelId": 1,
    "title": "My test alert",
    "message": "Something broke",
    "severity": "warning",
    "source": "MANUAL-TEST"
  }'
```
Wait ~5-10 seconds, then check the Fixora UI → Sample App → Active Alerts → click the alert → "View
RCA". You should see a real, LLM-generated root cause, report, and recommended actions.

To toggle AI on/off for any config: Fixora UI → Alert Configs → edit a config → "Trigger AI
Investigation" checkbox → Save. Takes effect immediately (regenerates the underlying Keep workflow).

## 8. Set up GoAlert (critical-alert SMS paging) — full walkthrough

This is the part with the most manual steps, because it involves a real phone number and a real
Twilio account. Full detail and the test checklist: **`docs/goalert-setup.md`**. Summary:

### 8a. Create a free Twilio account and get a phone number

1. Go to [twilio.com/try-twilio](https://www.twilio.com/try-twilio) and sign up (free trial is
   enough — trial SMS/calls just get a short "sent from trial account" disclaimer).
2. Once logged in, the Twilio Console dashboard shows your **Account SID** directly.
3. Click the eye/show icon next to it to reveal your **Auth Token** — treat this like a password,
   never share or commit it.
4. Get a phone number: Console → **Phone Numbers → Manage → Buy a Number** (trial accounts get one
   free) — this is your **From Number**, e.g. `+15551234567`.

You now have three values: Account SID, Auth Token, From Number.

### 8b. Start GoAlert + a tunnel

```bash
cd fixora-api
docker compose up -d goalert
docker run --rm -p 4040:4040 ngrok/ngrok http host.docker.internal:8081 --authtoken=<your-ngrok-authtoken>
```
Need an ngrok account too (free) — [ngrok.com](https://ngrok.com) → sign up → Dashboard → your
authtoken. Grab the `https://...ngrok-free.app` URL it prints (or `curl
http://localhost:4040/api/tunnels`).

### 8c. One script does the rest — this is where you add the Twilio values

```bash
GOALERT_URL=https://<your-tunnel>.ngrok-free.app \
GOALERT_ADMIN_PASS=<choose-a-password> \
TWILIO_ACCOUNT_SID=<from step 8a> \
TWILIO_AUTH_TOKEN=<from step 8a> \
TWILIO_FROM_NUMBER=<from step 8a> \
  ./scripts/setup-goalert.sh
```
This creates the GoAlert admin user, sets its Public URL, pushes your Twilio credentials into
GoAlert's config, and provisions a Service + Escalation Policy + Integration Key. It prints a webhook
URL at the end — copy it.

### 8d. Finish the Twilio side (their console, not ours)

In the Twilio Console → **Phone Numbers → Manage → your number**, set:
- "A MESSAGE COMES IN" webhook → `<your-tunnel-url>/api/v2/twilio/message`
- "A CALL COMES IN" webhook → `<your-tunnel-url>/api/v2/twilio/call`

### 8e. Verify your own phone in GoAlert

Open `<your-tunnel-url>` in a browser, log in (`admin` / the password from step 8c) → your avatar →
**Profile → Contact Methods → Create Contact Method** → Text Message (SMS) → your number → GoAlert
texts you a code → enter it to verify.

### 8f. Wire the webhook URL into a critical Alert Configuration

Fixora UI → Alert Configs → edit "Sample Critical Escalation" (or any config) → paste the webhook URL
from step 8c into **`goalertServiceUrl`** → make sure severity is `critical` → Save.

### 8g. Test it

```bash
curl -X POST http://localhost:8083/api/applications/1/test-alert \
  -H "Content-Type: application/json" -H "X-AD-Group: dev-team" \
  -d '{"alertConfigurationId": <id>, "notificationChannelId": 1, "title": "Critical test", "message": "Testing SMS paging", "severity": "critical", "source": "MANUAL-TEST"}'
```
You should get a real SMS within a few seconds. Full troubleshooting checklist (what to check at each
stage if it doesn't arrive) is in `docs/goalert-setup.md`.

## Summary — what's manual vs scripted

| Step | Scripted? |
|---|---|
| Postgres/Redis/Keep/fixora-api/fixora-ui startup | ✅ one command each |
| Sample data (application) | ✅ auto, via `init.sql` |
| Sample Alert Configurations | ✅ `seed-sample-alerts.sh` |
| GoAlert admin user, Public URL, Twilio config, Service/Policy/Key | ✅ `setup-goalert.sh` |
| Twilio account signup + getting SID/Token/Number | ❌ manual — real external account |
| ngrok tunnel | ❌ manual — real external account, each person's own |
| Twilio console webhook URLs | ❌ manual — Twilio's own UI |
| Verifying your own phone number in GoAlert | ❌ manual, intentionally (explicit consent) |
| Groq API key | ❌ manual — real external account (free) |
