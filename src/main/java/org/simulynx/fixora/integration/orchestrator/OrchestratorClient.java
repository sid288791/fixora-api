package org.simulynx.fixora.integration.orchestrator;

import java.util.Map;

/**
 * Client for fixora-orchestrator-agent's phase-1 (Temporal-free) Deep Analysis endpoint.
 * Throws RestClientException on any downstream failure — callers decide how to translate
 * that into a user-facing response (see KeepIntegrationService.runDeepAnalysis).
 */
public interface OrchestratorClient {
    Map<String, Object> runDeepAnalysis(String incidentId, String alertName, String message,
                                          String severity, String appName, String existingRca);
}
