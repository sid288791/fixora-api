"""
Minimal HTTP wrapper around the OpenSRE CLI so Keep's built-in "http" workflow
action can call it (Keep calls a URL, not a CLI). OpenSRE itself has no HTTP
server mode we found in this alpha version, so this shells out to `opensre
investigate` per request and returns its JSON output.

POC-grade only: synchronous, no auth, no queueing. A real investigation run
takes ~3-5 minutes (verified in POC-RESULTS.md), so the caller (Keep's http
action) needs a generous timeout.

Setup: see README.md in this folder.
"""
import json
import subprocess
import sys
import tempfile
import os
from datetime import datetime, timezone
from pathlib import Path

import httpx
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

APP_DIR = Path(__file__).parent
load_dotenv(APP_DIR / ".env")

# venv layout differs by OS: Scripts/*.exe on Windows, bin/* elsewhere.
_venv_scripts = APP_DIR / ".venv" / ("Scripts" if sys.platform == "win32" else "bin")
OPENSRE_BIN = _venv_scripts / ("opensre.exe" if sys.platform == "win32" else "opensre")

# Reused from the main investigation's LLM config (Groq via the ollama-provider workaround,
# see .env / POC-RESULTS.md) for a second, much cheaper follow-up call.
GROQ_API_KEY = os.environ.get("OLLAMA_API_KEY", "")
GROQ_MODEL = os.environ.get("OLLAMA_MODEL", "openai/gpt-oss-120b")
GROQ_CHAT_URL = "https://api.groq.com/openai/v1/chat/completions"

app = FastAPI(title="opensre-http-wrapper")


def extract_recommended_actions(report: str) -> list[str]:
    """
    OpenSRE's own CLI output has no structured "next steps" field — the recommendations are
    buried as prose inside the report text. This makes one short, cheap follow-up LLM call
    (not a re-investigation) to pull them out as a clean list. Best-effort: returns an empty
    list on any failure rather than breaking the whole response.
    """
    if not GROQ_API_KEY or not report:
        return []
    try:
        resp = httpx.post(
            GROQ_CHAT_URL,
            headers={"Authorization": f"Bearer {GROQ_API_KEY}", "Content-Type": "application/json"},
            json={
                "model": GROQ_MODEL,
                "messages": [{
                    "role": "user",
                    "content": (
                        "Extract 3-5 concrete, actionable next steps an engineer should take, based "
                        "on this SRE investigation report. Return ONLY a JSON array of short strings "
                        "(no markdown, no other text, no explanation).\n\nReport:\n" + report
                    ),
                }],
                "max_tokens": 400,
                "temperature": 0,
            },
            timeout=30,
        )
        resp.raise_for_status()
        content = resp.json()["choices"][0]["message"]["content"].strip()
        if content.startswith("```"):
            content = content.strip("`")
            if content.startswith("json"):
                content = content[4:]
        actions = json.loads(content)
        if isinstance(actions, list):
            return [str(a) for a in actions]
    except Exception:
        pass
    return []


class InvestigateRequest(BaseModel):
    alert_name: str
    message: str = ""
    severity: str = "info"
    pipeline_name: str = "unknown"
    correlation_id: str = ""


@app.post("/investigate")
def investigate(req: InvestigateRequest):
    payload = {
        "alert_name": req.alert_name,
        "pipeline_name": req.pipeline_name,
        "severity": req.severity,
        "alert_source": "keep",
        "message": req.message,
        "commonAnnotations": {
            "summary": req.message,
            "correlation_id": req.correlation_id,
        },
    }

    with tempfile.TemporaryDirectory() as tmpdir:
        input_path = Path(tmpdir) / "alert.json"
        output_path = Path(tmpdir) / "result.json"
        input_path.write_text(json.dumps(payload), encoding="utf-8")

        env = os.environ.copy()
        env["PYTHONIOENCODING"] = "utf-8"

        result = subprocess.run(
            [str(OPENSRE_BIN), "--yes", "investigate", "-i", str(input_path), "-o", str(output_path)],
            cwd=str(APP_DIR),
            env=env,
            capture_output=True,
            text=True,
            timeout=600,
        )

        if not output_path.exists():
            stdout_tail = (result.stdout or "")[-2000:]
            stderr_tail = (result.stderr or "")[-2000:]
            raise HTTPException(
                status_code=502,
                detail=f"opensre produced no output (exit={result.returncode}). "
                       f"stdout={stdout_tail} stderr={stderr_tail}",
            )

        parsed = json.loads(output_path.read_text(encoding="utf-8"))
        parsed["investigated_at"] = datetime.now(timezone.utc).isoformat()
        # OpenSRE runs a single LLM call over the whole templated alert text — it doesn't
        # attribute conclusions to individual input fields internally. Being transparent about
        # that: this lists exactly what was actually sent, rather than claiming a per-field
        # attribution the tool doesn't really do.
        parsed["based_on"] = (
            "This analysis was generated from the following alert data only "
            "(no logs, metrics, or other telemetry were available):\n\n"
            f"- **alert_name**: {req.alert_name}\n"
            f"- **message**: {req.message or '(none provided)'}\n"
            f"- **severity**: {req.severity}\n"
            f"- **pipeline_name**: {req.pipeline_name}\n"
            f"- **correlation_id**: {req.correlation_id or '(none provided)'}\n"
        )
        parsed["recommended_actions"] = extract_recommended_actions(parsed.get("report", ""))
        return parsed


@app.get("/healthcheck")
def healthcheck():
    return {"status": "ok"}
