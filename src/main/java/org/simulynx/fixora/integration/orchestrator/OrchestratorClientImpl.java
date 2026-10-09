package org.simulynx.fixora.integration.orchestrator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Deliberately does NOT reuse the shared RestTemplate bean (RestTemplateConfig): that bean has
 * no read timeout configured, which is fine for GoAlertClientImpl's quick GraphQL calls but not
 * for this one — the diagnostic loop behind /api/v1/deep-analysis makes several chained LLM
 * calls and can legitimately take tens of seconds. A dedicated instance with a bounded read
 * timeout keeps a slow/stuck orchestrator from hanging this request indefinitely without
 * affecting any other caller of the shared bean.
 */
@Slf4j
@Service
public class OrchestratorClientImpl implements OrchestratorClient {

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    // Live-tested against a real Groq-backed diagnostic loop with zero matching evidence (worst
    // case: all 3 bounded iterations run, 4 LLM calls per iteration plus the initial hypotheses
    // and final conclusion calls) -- that run alone took over 60s and tripped the original
    // timeout, surfacing as a spurious 502 even though the orchestrator was working correctly.
    // 120s covers the full bounded loop with headroom while still failing fast on a genuinely
    // stuck orchestrator.
    private static final int READ_TIMEOUT_MS = 120_000;

    private final RestTemplate restTemplate;
    private final String orchestratorUrl;

    public OrchestratorClientImpl(@Value("${orchestrator.api.url:http://localhost:8091}") String orchestratorUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);
        this.restTemplate = new RestTemplate(factory);
        this.orchestratorUrl = orchestratorUrl;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> runDeepAnalysis(String incidentId, String alertName, String message,
                                                 String severity, String appName, String existingRca) {
        String url = orchestratorUrl + "/api/v1/deep-analysis";

        Map<String, Object> body = new HashMap<>();
        body.put("incident_id", incidentId);
        body.put("alert_name", alertName);
        body.put("message", message);
        body.put("severity", severity);
        body.put("app_name", appName);
        body.put("existing_rca", existingRca);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
        if (response == null) {
            throw new IllegalStateException("Deep analysis call returned no response body");
        }
        return response;
    }
}
