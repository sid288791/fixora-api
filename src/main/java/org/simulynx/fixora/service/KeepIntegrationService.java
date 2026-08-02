package org.simulynx.fixora.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.simulynx.fixora.dto.TestAlertRequest;
import org.simulynx.fixora.dto.TestAlertResponse;
import org.simulynx.fixora.entity.Application;
import org.simulynx.fixora.integration.keep.KeepClient;
import org.simulynx.fixora.repository.ApplicationRepository;
import org.simulynx.fixora.util.GsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@Transactional
public class KeepIntegrationService {

    private static final Set<String> ACTIVE_STATUSES = Set.of("firing", "acknowledged");
    private static final Set<String> CLOSED_STATUSES = Set.of("resolved");

    @Autowired
    private KeepClient keepClient;

    @Autowired
    private AlertWorkflowService alertWorkflowService;

    @Autowired
    private ApplicationRepository applicationRepository;

    private final Gson gson = GsonUtil.getInstance();
    
    public Boolean checkKeepHealth() {
        try {
            log.info("Checking Keep platform health");
            return keepClient.healthCheck();
        } catch (Exception e) {
            log.error("Error checking Keep platform health", e);
            return false;
        }
    }
    
    public Map<String, Object> createKeepProvider(String providerId, Map<String, Object> config) {
        try {
            log.info("Installing Keep provider: {}", providerId);
            Map<String, Object> result = keepClient.installProvider(providerId, config);
            log.info("Successfully installed Keep provider: {}", providerId);
            return result;
        } catch (Exception e) {
            log.error("Error installing Keep provider: {}", providerId, e);
            throw new RuntimeException("Failed to install Keep provider", e);
        }
    }
    
    public void deleteKeepProvider(String providerId) {
        try {
            log.info("Uninstalling Keep provider: {}", providerId);
            keepClient.uninstallProvider(providerId);
            log.info("Successfully uninstalled Keep provider: {}", providerId);
        } catch (Exception e) {
            log.error("Error uninstalling Keep provider: {}", providerId, e);
            throw new RuntimeException("Failed to uninstall Keep provider", e);
        }
    }
    
    public Map<String, Object> updateKeepProvider(String providerId, Map<String, Object> config) {
        try {
            log.info("Updating Keep provider: {}", providerId);
            Map<String, Object> result = keepClient.updateProviderConfig(providerId, config);
            log.info("Successfully updated Keep provider: {}", providerId);
            return result;
        } catch (Exception e) {
            log.error("Error updating Keep provider: {}", providerId, e);
            throw new RuntimeException("Failed to update Keep provider", e);
        }
    }
    
    public Map<String, Object> getKeepProvider(String providerId) {
        try {
            log.info("Fetching Keep provider: {}", providerId);
            Map<String, Object> config = keepClient.getProviderConfig(providerId);
            log.info("Successfully fetched Keep provider: {}", providerId);
            return config;
        } catch (Exception e) {
            log.error("Error fetching Keep provider: {}", providerId, e);
            throw new RuntimeException("Failed to fetch Keep provider", e);
        }
    }
    
    public Map<String, Object> createKeepWorkflow(Map<String, Object> workflowDefinition) {
        try {
            log.info("Creating Keep workflow");
            Map<String, Object> result = keepClient.createWorkflow(workflowDefinition);
            log.info("Successfully created Keep workflow");
            return result;
        } catch (Exception e) {
            log.error("Error creating Keep workflow", e);
            throw new RuntimeException("Failed to create Keep workflow", e);
        }
    }
    
    public Map<String, Object> getKeepWorkflow(String workflowId) {
        try {
            log.info("Fetching Keep workflow: {}", workflowId);
            Map<String, Object> workflow = keepClient.getWorkflow(workflowId);
            log.info("Successfully fetched Keep workflow: {}", workflowId);
            return workflow;
        } catch (Exception e) {
            log.error("Error fetching Keep workflow: {}", workflowId, e);
            throw new RuntimeException("Failed to fetch Keep workflow", e);
        }
    }
    
    public List<Map<String, Object>> getAllKeepWorkflows() {
        try {
            log.info("Fetching all Keep workflows");
            List<Map<String, Object>> workflows = keepClient.getAllWorkflows();
            log.info("Successfully fetched {} Keep workflows", workflows.size());
            return workflows;
        } catch (Exception e) {
            log.error("Error fetching Keep workflows", e);
            throw new RuntimeException("Failed to fetch Keep workflows", e);
        }
    }
    
    public Map<String, Object> updateKeepWorkflow(String workflowId, Map<String, Object> workflowDefinition) {
        try {
            log.info("Updating Keep workflow: {}", workflowId);
            Map<String, Object> result = keepClient.updateWorkflow(workflowId, workflowDefinition);
            log.info("Successfully updated Keep workflow: {}", workflowId);
            return result;
        } catch (Exception e) {
            log.error("Error updating Keep workflow: {}", workflowId, e);
            throw new RuntimeException("Failed to update Keep workflow", e);
        }
    }
    
    public void deleteKeepWorkflow(String workflowId) {
        try {
            log.info("Deleting Keep workflow: {}", workflowId);
            keepClient.deleteWorkflow(workflowId);
            log.info("Successfully deleted Keep workflow: {}", workflowId);
        } catch (Exception e) {
            log.error("Error deleting Keep workflow: {}", workflowId, e);
            throw new RuntimeException("Failed to delete Keep workflow", e);
        }
    }
    
    public Map<String, Object> checkWorkflowStatus(String workflowId) {
        try {
            log.info("Checking Keep workflow status: {}", workflowId);
            Map<String, Object> status = keepClient.getWorkflowStatus(workflowId);
            log.info("Successfully fetched Keep workflow status: {}", workflowId);
            return status;
        } catch (Exception e) {
            log.error("Error checking Keep workflow status: {}", workflowId, e);
            throw new RuntimeException("Failed to check workflow status", e);
        }
    }
    
    public TestAlertResponse sendTestAlert(Long applicationId, TestAlertRequest request) {
        try {
            log.info("Sending test alert for workflow config from alert configuration {}", request.getAlertConfigurationId());

            Application application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));

            Map<String, Object> payload = new HashMap<>();
            payload.put("name", request.getTitle() != null ? request.getTitle() : "Test Alert");
            payload.put("status", "firing");
            payload.put("message", request.getMessage() != null ? request.getMessage() : "This is a test alert");
            payload.put("severity", request.getSeverity() != null ? request.getSeverity().toLowerCase() : "info");
            payload.put("source", List.of(request.getSource() != null ? request.getSource() : "FIXORA"));
            payload.put("lastReceived", LocalDateTime.now().toString());
            payload.put("fingerprint", "fixora-test-" + request.getAlertConfigurationId());
            // Keep has no native "application" concept, so this is how we tie an alert back
            // to the Fixora application it belongs to when listing active/closed alerts later.
            payload.put("labels", Map.of(
                    "fixora_application_id", String.valueOf(applicationId),
                    "fixora_application_alias", application.getAlias()
            ));

            Map<String, Object> response = keepClient.sendTestAlert(
                    "test-workflow-" + request.getAlertConfigurationId(), 
                    payload
            );
            
            TestAlertResponse result = new TestAlertResponse();
            result.setSuccess(true);
            result.setMessage("Test alert sent successfully");
            result.setAlertId(String.valueOf(request.getAlertConfigurationId()));
            result.setChannelId(String.valueOf(request.getNotificationChannelId()));
            result.setSentAt(LocalDateTime.now());
            result.setStatus("SENT");
            
            log.info("Successfully sent test alert");
            return result;
        } catch (Exception e) {
            log.error("Error sending test alert", e);
            
            TestAlertResponse result = new TestAlertResponse();
            result.setSuccess(false);
            result.setMessage("Failed to send test alert: " + e.getMessage());
            result.setSentAt(LocalDateTime.now());
            result.setStatus("FAILED");
            
            return result;
        }
    }

    /**
     * Fetches alerts belonging to the given application (via the fixora_application_id label
     * stamped at alert-creation time) and buckets them into "active" (firing/acknowledged) or
     * "closed" (resolved). Alerts sent before this labeling existed won't match anything.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getAlertsForApplication(Long applicationId, String bucket) {
        applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));

        Set<String> allowedStatuses = "closed".equalsIgnoreCase(bucket) ? CLOSED_STATUSES : ACTIVE_STATUSES;
        String applicationIdStr = String.valueOf(applicationId);

        List<Map<String, Object>> allAlerts = keepClient.getAlerts();
        List<Map<String, Object>> matched = new ArrayList<>();

        for (Map<String, Object> alert : allAlerts) {
            Object labelsObj = alert.get("labels");
            if (!(labelsObj instanceof Map<?, ?> labels)) {
                continue;
            }
            Object appIdLabel = labels.get("fixora_application_id");
            if (appIdLabel == null || !applicationIdStr.equals(String.valueOf(appIdLabel))) {
                continue;
            }
            String status = String.valueOf(alert.getOrDefault("status", "")).toLowerCase();
            if (allowedStatuses.contains(status)) {
                matched.add(alert);
                enrichWithRecentOccurrences(alert);
            }
        }

        return matched;
    }

    /**
     * firingCounter on the alert object is a lifetime count since the alert was first created —
     * an alert that fired 100 times over 20 days looks identical in that field to one that fired
     * 100 times in the last hour. Adds an "occurrencesLast24h" field computed from the alert's
     * actual history (each entry has its own timestamp) so the UI can show a time-bounded count
     * instead of a misleading all-time total. Only fetches history when firingCounter > 1, to
     * avoid an extra Keep API call for every single-fire alert.
     */
    private void enrichWithRecentOccurrences(Map<String, Object> alert) {
        Object firingCounterObj = alert.get("firingCounter");
        int firingCounter = firingCounterObj instanceof Number n ? n.intValue() : 0;
        if (firingCounter <= 1) {
            alert.put("occurrencesLast24h", firingCounter);
            return;
        }

        String fingerprint = String.valueOf(alert.get("fingerprint"));
        try {
            List<Map<String, Object>> history = keepClient.getAlertHistory(fingerprint);
            Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
            long recentCount = history.stream()
                    .map(h -> h.get("lastReceived"))
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .filter(ts -> {
                        try {
                            return Instant.parse(ts).isAfter(cutoff);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();
            alert.put("occurrencesLast24h", recentCount);
        } catch (Exception e) {
            log.warn("Failed to compute recent occurrence count for fingerprint {}", fingerprint, e);
            alert.put("occurrencesLast24h", null);
        }
    }

    public String createKeepWorkflowFromAlertConfig(Long alertConfigId, String alertName, String alertDescription,
                                                     String alertType, String severity, String channels) {
        return createKeepWorkflowFromAlertConfig(alertConfigId, alertName, alertDescription, alertType, severity, channels, false);
    }

    /**
     * Builds a Keep workflow "action" (not "step" — actions are the side-effecting stage in Keep's DSL)
     * that calls the OpenSRE HTTP wrapper (see opensre-poc/server.py) with the triggering alert's data,
     * and enriches that alert with the response. host.docker.internal is required since this runs inside
     * the keep-backend container and needs to reach a process on the Docker host.
     */
    private Map<String, Object> buildAiInvestigationAction() {
        Map<String, Object> body = new HashMap<>();
        body.put("alert_name", "{{ alert.name }}");
        body.put("message", "{{ alert.message }}");
        body.put("severity", "{{ alert.severity }}");
        body.put("pipeline_name", "{{ alert.source }}");
        body.put("correlation_id", "{{ alert.fingerprint }}");

        Map<String, Object> with = new HashMap<>();
        with.put("url", "http://host.docker.internal:8090/investigate");
        with.put("method", "POST");
        with.put("body", body);
        with.put("enrich_alert", List.of(
                Map.of("key", "ai_root_cause", "value", "results.body.root_cause"),
                Map.of("key", "ai_report", "value", "results.body.report"),
                Map.of("key", "ai_based_on", "value", "results.body.based_on"),
                Map.of("key", "ai_recommended_actions", "value", "results.body.recommended_actions"),
                Map.of("key", "ai_investigated_at", "value", "results.body.investigated_at")
        ));

        Map<String, Object> provider = new HashMap<>();
        provider.put("type", "http");
        provider.put("config", "{{ providers.default-http }}");
        provider.put("with", with);

        Map<String, Object> action = new HashMap<>();
        action.put("name", "ai-investigation");
        action.put("provider", provider);
        return action;
    }

    private Map<String, Object> buildWorkflowDefinition(Long alertConfigId, String alertName, String alertDescription,
                                                          String alertType, String severity, String channels,
                                                          boolean triggerAiInvestigation) {
        Map<String, Object> workflowDefinition = new HashMap<>();
        workflowDefinition.put("name", "fixora-alert-" + alertConfigId + "-" + alertName.toLowerCase().replace(" ", "-"));
        workflowDefinition.put("description", alertDescription != null ? alertDescription : "Workflow for alert: " + alertName);

        Map<String, Object> triggers = new HashMap<>();
        triggers.put("type", "alert");
        Map<String, Object> triggerConfig = new HashMap<>();
        triggerConfig.put("alert_config_id", alertConfigId);
        triggerConfig.put("alert_type", alertType);
        triggerConfig.put("severity", severity);
        triggers.put("config", triggerConfig);
        workflowDefinition.put("triggers", List.of(triggers));

        // Notification channels are stored in the alert_config DB table for future use
        // when real notification providers (Slack, Teams, etc.) are configured in Keep.
        // No step is added here to avoid blocking the AI investigation action with a
        // placeholder HTTP call that would fail on a dummy URL.

        if (triggerAiInvestigation) {
            workflowDefinition.put("actions", List.of(buildAiInvestigationAction()));
        }

        return workflowDefinition;
    }

    public String createKeepWorkflowFromAlertConfig(Long alertConfigId, String alertName, String alertDescription,
                                                     String alertType, String severity, String channels,
                                                     boolean triggerAiInvestigation) {
        try {
            log.info("Creating Keep workflow for alert configuration {}", alertConfigId);

            Map<String, Object> workflowDefinition = buildWorkflowDefinition(
                    alertConfigId, alertName, alertDescription, alertType, severity, channels, triggerAiInvestigation);

            Map<String, Object> result = keepClient.createWorkflow(workflowDefinition);

            String keepWorkflowId = result != null && result.get("workflow_id") != null ?
                result.get("workflow_id").toString() : null;

            log.info("Successfully created Keep workflow with id: {}", keepWorkflowId);
            return keepWorkflowId;
        } catch (Exception e) {
            log.error("Error creating Keep workflow for alert configuration {}", alertConfigId, e);
            throw new RuntimeException("Failed to create Keep workflow for alert configuration", e);
        }
    }

    /**
     * Regenerates and pushes the Keep workflow definition for an alert config that already has one
     * (identified by keepWorkflowId). This is what makes toggling "Trigger AI Investigation" on an
     * *existing* config actually take effect — without this, the AI action is only ever added at
     * creation time and flipping the toggle later on an existing config silently does nothing.
     */
    public void updateKeepWorkflowFromAlertConfig(String keepWorkflowId, Long alertConfigId, String alertName,
                                                   String alertDescription, String alertType, String severity,
                                                   String channels, boolean triggerAiInvestigation) {
        try {
            log.info("Updating Keep workflow {} for alert configuration {}", keepWorkflowId, alertConfigId);

            Map<String, Object> workflowDefinition = buildWorkflowDefinition(
                    alertConfigId, alertName, alertDescription, alertType, severity, channels, triggerAiInvestigation);

            keepClient.updateWorkflow(keepWorkflowId, workflowDefinition);

            log.info("Successfully updated Keep workflow {}", keepWorkflowId);
        } catch (Exception e) {
            log.error("Error updating Keep workflow {} for alert configuration {}", keepWorkflowId, alertConfigId, e);
            throw new RuntimeException("Failed to update Keep workflow for alert configuration", e);
        }
    }
}
