package org.simulynx.fixora.integration.keep;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.simulynx.fixora.dto.KeepProviderDTO;
import org.simulynx.fixora.util.GsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST client for the real Keep platform API (https://github.com/keephq/keep).
 * Keep's API is mounted at the service root (no "/api" prefix) and requires an
 * "x-api-key" header even when the platform is running with AUTH_TYPE=NO_AUTH.
 */
@Slf4j
@Service
public class KeepClientImpl implements KeepClient {

    private final RestTemplate restTemplate;
    private final String keepApiBaseUrl;
    private final String keepApiKey;
    private final Gson gson;

    public KeepClientImpl(RestTemplate restTemplate,
                           @Value("${keep.api.url:http://localhost:8080}") String keepApiBaseUrl,
                           @Value("${keep.api.key:fixora-api-key}") String keepApiKey) {
        this.restTemplate = restTemplate;
        this.keepApiBaseUrl = keepApiBaseUrl;
        this.keepApiKey = keepApiKey;
        this.gson = GsonUtil.getInstance();
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", keepApiKey);
        return headers;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> exchange(String path, HttpMethod method, Object body) {
        String url = keepApiBaseUrl + path;
        HttpEntity<Object> request = new HttpEntity<>(body, defaultHeaders());
        Map<?, ?> response = restTemplate.exchange(url, method, request, Map.class).getBody();
        return (Map<String, Object>) response;
    }

    @Override
    public List<KeepProviderDTO> getAllProviders() {
        try {
            log.info("Fetching all providers from Keep API");
            Map<String, Object> response = exchange("/providers", HttpMethod.GET, null);
            List<KeepProviderDTO> providers = new ArrayList<>();

            if (response != null && response.get("providers") instanceof List<?> rawProviders) {
                for (Object provider : rawProviders) {
                    providers.add(gson.fromJson(gson.toJson(provider), KeepProviderDTO.class));
                }
            }

            log.info("Successfully fetched {} providers from Keep API", providers.size());
            return providers;
        } catch (RestClientException e) {
            log.error("Error fetching providers from Keep API", e);
            throw new RuntimeException("Failed to fetch providers from Keep API", e);
        }
    }

    @Override
    public KeepProviderDTO getProvider(String providerId) {
        log.info("Fetching provider {} from Keep API", providerId);
        return getAllProviders().stream()
                .filter(p -> providerId.equals(p.getId()) || providerId.equals(p.getType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Provider not found: " + providerId));
    }

    @Override
    public Map<String, Object> installProvider(String providerId, Map<String, Object> config) {
        try {
            log.info("Installing provider {} with Keep API", providerId);
            Map<String, Object> payload = config != null ? new java.util.HashMap<>(config) : new java.util.HashMap<>();
            payload.putIfAbsent("provider_id", providerId);
            payload.putIfAbsent("provider_type", providerId);
            payload.putIfAbsent("provider_name", providerId);

            Map<String, Object> result = exchange("/providers/install", HttpMethod.POST, payload);
            log.info("Successfully installed provider: {}", providerId);
            return result;
        } catch (RestClientException e) {
            log.error("Error installing provider {} with Keep API", providerId, e);
            throw new RuntimeException("Failed to install provider with Keep API", e);
        }
    }

    @Override
    public void uninstallProvider(String providerId) {
        try {
            log.info("Uninstalling provider {} with Keep API", providerId);
            KeepProviderDTO provider = getProvider(providerId);
            String providerType = provider.getType() != null ? provider.getType() : providerId;
            exchange("/providers/" + providerType + "/" + providerId, HttpMethod.DELETE, null);
            log.info("Successfully uninstalled provider: {}", providerId);
        } catch (RestClientException e) {
            log.error("Error uninstalling provider {} with Keep API", providerId, e);
            throw new RuntimeException("Failed to uninstall provider with Keep API", e);
        }
    }

    @Override
    public Map<String, Object> updateProviderConfig(String providerId, Map<String, Object> config) {
        try {
            log.info("Updating provider {} config with Keep API", providerId);
            Map<String, Object> result = exchange("/providers/" + providerId, HttpMethod.PUT, config);
            log.info("Successfully updated provider config: {}", providerId);
            return result;
        } catch (RestClientException e) {
            log.error("Error updating provider {} config with Keep API", providerId, e);
            throw new RuntimeException("Failed to update provider config with Keep API", e);
        }
    }

    @Override
    public Map<String, Object> getProviderConfig(String providerId) {
        log.info("Fetching provider {} config from Keep API", providerId);
        KeepProviderDTO provider = getProvider(providerId);
        return gson.fromJson(gson.toJson(provider), Map.class);
    }

    @Override
    public Map<String, Object> createWorkflow(Map<String, Object> workflowDefinition) {
        try {
            log.info("Creating workflow with Keep API");
            Map<String, Object> result = exchange("/workflows/json", HttpMethod.POST, workflowDefinition);
            log.info("Successfully created workflow");
            return result;
        } catch (RestClientException e) {
            log.error("Error creating workflow with Keep API", e);
            throw new RuntimeException("Failed to create workflow with Keep API", e);
        }
    }

    @Override
    public Map<String, Object> getWorkflow(String workflowId) {
        try {
            log.info("Fetching workflow {} from Keep API", workflowId);
            Map<String, Object> result = exchange("/workflows/" + workflowId, HttpMethod.GET, null);
            log.info("Successfully fetched workflow: {}", workflowId);
            return result;
        } catch (RestClientException e) {
            log.error("Error fetching workflow {} from Keep API", workflowId, e);
            throw new RuntimeException("Failed to fetch workflow from Keep API", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getAllWorkflows() {
        try {
            log.info("Fetching all workflows from Keep API");
            String url = keepApiBaseUrl + "/workflows";
            HttpEntity<Object> request = new HttpEntity<>(defaultHeaders());
            List<?> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class).getBody();

            List<Map<String, Object>> workflows = new ArrayList<>();
            if (response != null) {
                for (Object item : response) {
                    workflows.add((Map<String, Object>) item);
                }
            }

            log.info("Successfully fetched {} workflows from Keep API", workflows.size());
            return workflows;
        } catch (RestClientException e) {
            log.error("Error fetching workflows from Keep API", e);
            throw new RuntimeException("Failed to fetch workflows from Keep API", e);
        }
    }

    @Override
    public Map<String, Object> updateWorkflow(String workflowId, Map<String, Object> workflowDefinition) {
        try {
            log.info("Updating workflow {} with Keep API", workflowId);
            Map<String, Object> result = exchange("/workflows/" + workflowId, HttpMethod.PUT, workflowDefinition);
            log.info("Successfully updated workflow: {}", workflowId);
            return result;
        } catch (RestClientException e) {
            log.error("Error updating workflow {} with Keep API", workflowId, e);
            throw new RuntimeException("Failed to update workflow with Keep API", e);
        }
    }

    @Override
    public void deleteWorkflow(String workflowId) {
        try {
            log.info("Deleting workflow {} with Keep API", workflowId);
            exchange("/workflows/" + workflowId, HttpMethod.DELETE, null);
            log.info("Successfully deleted workflow: {}", workflowId);
        } catch (RestClientException e) {
            log.error("Error deleting workflow {} with Keep API", workflowId, e);
            throw new RuntimeException("Failed to delete workflow with Keep API", e);
        }
    }

    @Override
    public Map<String, Object> triggerWorkflow(String workflowId, Map<String, Object> payload) {
        try {
            log.info("Triggering workflow {} with Keep API", workflowId);
            Map<String, Object> result = exchange("/workflows/" + workflowId + "/run", HttpMethod.POST, payload);
            log.info("Successfully triggered workflow: {}", workflowId);
            return result;
        } catch (RestClientException e) {
            log.error("Error triggering workflow {} with Keep API", workflowId, e);
            throw new RuntimeException("Failed to trigger workflow with Keep API", e);
        }
    }

    @Override
    public Map<String, Object> getWorkflowStatus(String workflowId) {
        try {
            log.info("Checking workflow {} status from Keep API", workflowId);
            Map<String, Object> result = exchange("/workflows/" + workflowId + "/runs", HttpMethod.GET, null);
            log.info("Successfully fetched workflow status: {}", workflowId);
            return result;
        } catch (RestClientException e) {
            log.error("Error fetching workflow {} status from Keep API", workflowId, e);
            throw new RuntimeException("Failed to fetch workflow status from Keep API", e);
        }
    }

    @Override
    public Map<String, Object> getWorkflowExecutionHistory(String workflowId) {
        try {
            log.info("Fetching workflow {} execution history from Keep API", workflowId);
            Map<String, Object> result = exchange("/workflows/" + workflowId + "/runs", HttpMethod.GET, null);
            log.info("Successfully fetched workflow execution history: {}", workflowId);
            return result;
        } catch (RestClientException e) {
            log.error("Error fetching workflow {} execution history from Keep API", workflowId, e);
            throw new RuntimeException("Failed to fetch workflow execution history from Keep API", e);
        }
    }

    @Override
    public Map<String, Object> sendTestAlert(String workflowId, Map<String, Object> alertPayload) {
        try {
            log.info("Sending test alert via Keep API (webhook provider)");
            String url = keepApiBaseUrl + "/alerts/event?provider_id=fixora-test";
            HttpEntity<Object> request = new HttpEntity<>(alertPayload, defaultHeaders());
            Map<?, ?> response = restTemplate.postForObject(url, request, Map.class);

            log.info("Successfully sent test alert");
            return response != null ? (Map<String, Object>) response : Map.of("status", "sent");
        } catch (RestClientException e) {
            log.error("Error sending test alert via Keep API", e);
            throw new RuntimeException("Failed to send test alert via Keep API", e);
        }
    }

    @Override
    public Boolean healthCheck() {
        try {
            String url = keepApiBaseUrl + "/healthcheck";
            log.info("Performing health check on Keep API: {}", url);

            restTemplate.getForObject(url, Map.class);
            log.info("Keep API health check result: HEALTHY");
            return true;
        } catch (RestClientException e) {
            log.warn("Keep API health check failed", e);
            return false;
        }
    }
}
