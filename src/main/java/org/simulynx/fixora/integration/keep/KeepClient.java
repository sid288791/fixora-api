package org.simulynx.fixora.integration.keep;

import org.simulynx.fixora.dto.KeepProviderDTO;

import java.util.List;
import java.util.Map;

public interface KeepClient {
    
    // Provider Management
    List<KeepProviderDTO> getAllProviders();
    KeepProviderDTO getProvider(String providerId);
    Map<String, Object> installProvider(String providerId, Map<String, Object> config);
    void uninstallProvider(String providerId);
    Map<String, Object> updateProviderConfig(String providerId, Map<String, Object> config);
    Map<String, Object> getProviderConfig(String providerId);
    
    // Workflow Management
    Map<String, Object> createWorkflow(Map<String, Object> workflowDefinition);
    Map<String, Object> getWorkflow(String workflowId);
    List<Map<String, Object>> getAllWorkflows();
    Map<String, Object> updateWorkflow(String workflowId, Map<String, Object> workflowDefinition);
    void deleteWorkflow(String workflowId);
    
    // Workflow Execution
    Map<String, Object> triggerWorkflow(String workflowId, Map<String, Object> payload);
    Map<String, Object> getWorkflowStatus(String workflowId);
    Map<String, Object> getWorkflowExecutionHistory(String workflowId);
    
    // Test Alert
    Map<String, Object> sendTestAlert(String workflowId, Map<String, Object> alertPayload);

    // Alerts
    List<Map<String, Object>> getAlerts();
    List<Map<String, Object>> getAlertHistory(String fingerprint);

    /**
     * Posts an alert event straight to Keep's /alerts/event endpoint, the same ingestion path
     * used for every other alert (test alerts included). Used to push a status change -- e.g.
     * "resolved" -- for an alert that already exists in Keep, keyed by its fingerprint.
     */
    Map<String, Object> reportAlertEvent(Map<String, Object> alertPayload);

    /**
     * Merges the given key/value pairs into an existing alert's enriched fields (Keep's
     * POST /alerts/enrich, the same mechanism the AI-investigation workflow action uses for
     * ai_root_cause etc). Used to attach an RCA note when an alert is closed manually.
     */
    void enrichAlert(String fingerprint, Map<String, Object> enrichments);

    // Health Check
    Boolean healthCheck();
}
