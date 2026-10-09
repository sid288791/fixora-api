# Local Dev Stack — Start/Stop & Memory Notes

Practical notes from running the full stack locally (fixora-ui + fixora-api + Keep + GoAlert +
fixora-rca-ai) on one machine. Not a setup guide — see `goalert-setup.md` and the root
`GETTING_STARTED.md` for that. This is "how to turn it on/off and why it ran out of memory."

## The pieces and where they run

| Component | How it runs | Port(s) | Notes |
|---|---|---|---|
| Postgres, Redis, Keep (backend/proxy/ws/frontend), GoAlert, Elastic, Kibana | `docker-compose` in `fixora-api/` | 5432, 6379, 8080, 8082, 6001/9601, 3000, 8081, 9200, 5601 | `docker compose up -d` |
| fixora-api (Spring Boot) | local, gradle | 8083 | `./gradlew bootRun` from `fixora-api/` |
| fixora-ui (Backstage) | local, node/yarn | 3001 (frontend), 7007 (backend) | `yarn` not on PATH on this machine — use `corepack yarn start` |
| fixora-rca-ai (opensre-server) | local, python venv | 8090 | needs `PYTHONIOENCODING=utf-8` on Windows |

Note: **port 3000 is Keep's own frontend**, not fixora-ui. fixora-ui (Backstage) is 3001. Easy to
mix up.

## Start commands

```bash
# 1. Docker services (from fixora-api/)
docker compose up -d

# 2. fixora-api (from fixora-api/)
./gradlew bootRun

# 3. fixora-rca-ai (from fixora-rca-ai/) — Git Bash
PYTHONIOENCODING=utf-8 ./.venv/Scripts/python.exe -m uvicorn server:app --host 0.0.0.0 --port 8090
# PowerShell:
#   $env:PYTHONIOENCODING = "utf-8"
#   .\.venv\Scripts\python.exe -m uvicorn server:app --host 0.0.0.0 --port 8090

# 4. fixora-ui (from fixora-ui/)
corepack yarn start
```

## Stop commands

```bash
# Docker services
docker compose down            # from fixora-api/ (keeps volumes/data)

# Local processes: Ctrl+C in each terminal, or find+kill by port:
netstat -ano | grep ":8083 "   # fixora-api
netstat -ano | grep ":8090 "   # fixora-rca-ai
netstat -ano | grep ":3001 "   # fixora-ui frontend
netstat -ano | grep ":7007 "   # fixora-ui backend
# then: taskkill //PID <pid> //F   (Git Bash) or Stop-Process -Id <pid> -Force (PowerShell)
```

## Memory pressure — what's actually heavy

`docker stats` on a normal run of this stack:

| Container | Typical RAM |
|---|---|
| **fixora_elastic** | **~4.7 GiB** |
| fixora_kibana | ~0.5 GiB |
| fixora_postgres | ~0.2 GiB |
| fixora_keep_backend | ~0.2 GiB |
| everything else (goalert, redis, keep-ws, keep-proxy, keep-frontend) | a few MB each |

**Elastic + Kibana alone are ~5 GB** — they only exist for the Deep Analysis / Kibana-agent RCA
path (`fixora-diagnostic-orchestrator-agent`), which we are **not** using in the normal flow (we're
using the lightweight `fixora-rca-ai` / opensre-server instead). On top of that: the fixora-api JVM,
the fixora-ui Backstage Node process (webpack + backend), and the fixora-rca-ai Python process all
add up. On an 8-16GB dev machine this is enough to trip Claude Code's background-shell
low-memory reaper (it kills background processes *it* started when system RAM is critically low —
not a bug, just a safety valve; processes started in your own terminal aren't affected by it,
though Windows itself can still OOM-kill them if RAM is genuinely exhausted).

**If you don't need Deep Analysis / Kibana right now, stop just those two to free ~5 GB:**
```bash
docker compose stop elastic kibana
```
Everything else (Keep, GoAlert, fixora-api, fixora-ui, fixora-rca-ai) keeps working — Deep Analysis
button in the UI would just fail until you bring them back with `docker compose up -d elastic kibana`.

Unrelated to this project: there are many other **exited** (not running, so not using RAM) containers
on this machine from other projects (`mcp-grafana-*`, `jobqueue-*`, `minikube`, `neo4j-learn`, etc.).
They don't cost memory while stopped, only disk. `docker system prune` would reclaim disk if that
ever matters, but has nothing to do with the memory pressure above.

## Where Twilio credentials actually live

**Not in this repo, and not in any `.env` file.** They were entered once via
`scripts/setup-goalert.sh`, which pushes them straight into GoAlert through its GraphQL API. GoAlert
then stores them **encrypted** (`GOALERT_DATA_ENCRYPTION_KEY`) as opaque `bytea` in its own Postgres
table (`goalert_db.config`, inside the `fixora_postgres` container/volume) — confirmed by inspecting
the table directly; the `data` column is encrypted binary, unreadable without going through GoAlert's
own API/UI.

This means:
- Twilio creds **persist** across `docker compose down`/`up` as long as the `postgres_data` volume
  isn't removed (matches the GoAlert/ngrok restart note — data survives, only the public tunnel URL
  doesn't).
- The only way to **check** whether they're currently set is to log into the GoAlert UI (admin
  account) → Admin → Config → Twilio section, or hit GoAlert's authenticated GraphQL config query.
  Direct SQL/psql against `config` cannot show you the values (it's ciphertext), and there is no
  plaintext Twilio SID/token sitting anywhere in the fixora-api repo.
- The GoAlert **Public URL** and Twilio's own webhook URLs (pointed at the ngrok tunnel) *do* go
  stale on every restart — see the ngrok restart steps in memory / `goalert-setup.md` steps 2-4.
  LLM key for `fixora-rca-ai` (`GROQ_API_KEY`) is unaffected by any of this — it's a plain `.env` in
  `fixora-rca-ai/` and doesn't depend on tunnels or GoAlert's encrypted config at all.
