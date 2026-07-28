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

    // Health Check
    Boolean healthCheck();
}
