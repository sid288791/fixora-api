# opensre-server

Minimal HTTP wrapper around [OpenSRE](https://github.com/Tracer-Cloud/opensre) so Keep's built-in `http`
workflow action can call it (Keep calls a URL, not a CLI — OpenSRE itself has no HTTP server mode in this
version). Fixora's Keep workflow calls this at `POST /investigate` when `trigger_ai_investigation` is on
for an alert config, then enriches the alert with the response (`ai_root_cause`, `ai_report`,
`ai_based_on`, `ai_recommended_actions`, `ai_investigated_at`).

Full background/design: `../docs/ai-rca-design.md`, `../docs/idea.md`, `../docs/FAQ.md`.

## Setup

```bash
cd opensre-server
python -m venv .venv

# Windows (Git Bash)
./.venv/Scripts/python.exe -m pip install -r requirements.txt
# macOS/Linux
# source .venv/bin/activate && pip install -r requirements.txt

cp .env.example .env
# edit .env: paste a real Groq API key into OLLAMA_API_KEY (see the comment in .env.example
# for why it's under OLLAMA_* — Groq isn't a natively-supported OpenSRE provider yet)
```

## Run

```bash
# Windows (Git Bash) — PYTHONIOENCODING is required, see "Known issues" below
PYTHONIOENCODING=utf-8 ./.venv/Scripts/python.exe -m uvicorn server:app --host 0.0.0.0 --port 8090

# macOS/Linux
python -m uvicorn server:app --host 0.0.0.0 --port 8090
```

Verify: `curl http://localhost:8090/healthcheck` → `{"status":"ok"}`

## How Keep reaches this

The Keep workflow action (`KeepIntegrationService.buildAiInvestigationAction()` in the main `fixora-api`
codebase) calls `http://host.docker.internal:8090/investigate` — that hostname only resolves from
**inside a Docker container on Docker Desktop** (Windows/Mac) back out to the host machine. This means:

- This server must run **on the same machine as Docker Desktop**, not remotely.
- If you're on native Linux Docker, `host.docker.internal` may need `--add-host=host.docker.internal:host-gateway` added to the `keep-backend` service in `../docker-compose.yml` — not yet needed/tested since dev so far has been Windows-only.

## Known issues (see `../docs/ai-rca-design.md` for more)

1. **Windows console encoding**: always set `PYTHONIOENCODING=utf-8` before running — OpenSRE's CLI
   output includes unicode characters that crash on Windows' default `cp1252` console otherwise.
2. **Groq reliability**: this workaround (routing Groq through OpenSRE's `ollama` provider path) has
   shown intermittent failures on OpenSRE's structured-output/tool-calling steps (`plan_actions`,
   `root_cause_diagnosis`) — confirmed not a code bug (same key works fine for plain chat calls
   directly), most likely Groq capacity/rate limits on this specific model for tool-calling requests.
   Retry usually works; if it doesn't, try a different `OLLAMA_MODEL` or wait a few minutes.
3. **Character encoding mangling**: enriched text sometimes shows mangled characters (e.g. `â€™`
   instead of `'`) — a known, not-yet-fixed encoding mismatch somewhere in the Keep enrichment
   round-trip.
4. **Slow + synchronous**: a real investigation takes ~3-5 minutes, during which the Keep workflow run
   blocks. Fine for a POC; would need an async/webhook pattern for production use.

## What's NOT here

`opensre-server/.venv/` is gitignored (large, machine-specific) — always run the Setup steps above after
cloning, don't expect it to be present.
