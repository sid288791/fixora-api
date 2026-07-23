package org.simulynx.fixora.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.simulynx.fixora.dto.TestAlertRequest;
import org.simulynx.fixora.dto.TestAlertResponse;
import org.simulynx.fixora.integration.keep.KeepClient;
import org.simulynx.fixora.util.GsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class KeepIntegrationService {
    
    @Autowired
    private KeepClient keepClient;
    
    @Autowired
    private AlertWorkflowService alertWorkflowService;
    
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
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("name", request.getTitle() != null ? request.getTitle() : "Test Alert");
            payload.put("status", "firing");
            payload.put("message", request.getMessage() != null ? request.getMessage() : "This is a test alert");
            payload.put("severity", request.getSeverity() != null ? request.getSeverity().toLowerCase() : "info");
            payload.put("source", List.of(request.getSource() != null ? request.getSource() : "FIXORA"));
            payload.put("lastReceived", LocalDateTime.now().toString());
            payload.put("fingerprint", "fixora-test-" + request.getAlertConfigurationId());
            
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
}
