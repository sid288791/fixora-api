package org.simulynx.fixora.controller;

import lombok.extern.slf4j.Slf4j;
import org.simulynx.fixora.dto.TestAlertRequest;
import org.simulynx.fixora.dto.TestAlertResponse;
import org.simulynx.fixora.service.KeepIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/applications/{applicationId}")
public class TestAlertController {
    
    @Autowired
    private KeepIntegrationService keepIntegrationService;
    
    @PostMapping("/test-alert")
    public ResponseEntity<TestAlertResponse> sendTestAlert(
            @PathVariable Long applicationId,
            @RequestBody TestAlertRequest request) {
        log.info("POST /api/applications/{}/test-alert", applicationId);
        
        TestAlertResponse response = keepIntegrationService.sendTestAlert(applicationId, request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/keep/health")
    public ResponseEntity<Map<String, Object>> checkKeepHealth(
            @PathVariable Long applicationId) {
        log.info("GET /api/applications/{}/keep/health", applicationId);
        
        Boolean isHealthy = keepIntegrationService.checkKeepHealth();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", isHealthy ? "HEALTHY" : "UNHEALTHY");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/keep/providers/{providerId}")
    public ResponseEntity<Map<String, Object>> createKeepProvider(
            @PathVariable Long applicationId,
            @PathVariable String providerId,
            @RequestBody Map<String, Object> config) {
        log.info("POST /api/applications/{}/keep/providers/{}", applicationId, providerId);
        
        Map<String, Object> result = keepIntegrationService.createKeepProvider(providerId, config);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    @GetMapping("/keep/providers/{providerId}")
    public ResponseEntity<Map<String, Object>> getKeepProvider(
            @PathVariable Long applicationId,
            @PathVariable String providerId) {
        log.info("GET /api/applications/{}/keep/providers/{}", applicationId, providerId);
        
        Map<String, Object> result = keepIntegrationService.getKeepProvider(providerId);
        
        return ResponseEntity.ok(result);
    }
    
    @PutMapping("/keep/providers/{providerId}")
    public ResponseEntity<Map<String, Object>> updateKeepProvider(
            @PathVariable Long applicationId,
            @PathVariable String providerId,
            @RequestBody Map<String, Object> config) {
        log.info("PUT /api/applications/{}/keep/providers/{}", applicationId, providerId);
        
        Map<String, Object> result = keepIntegrationService.updateKeepProvider(providerId, config);
        
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/keep/providers/{providerId}")
    public ResponseEntity<Void> deleteKeepProvider(
            @PathVariable Long applicationId,
            @PathVariable String providerId) {
        log.info("DELETE /api/applications/{}/keep/providers/{}", applicationId, providerId);
        
        keepIntegrationService.deleteKeepProvider(providerId);
        
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/keep/workflows")
    public ResponseEntity<Map<String, Object>> createKeepWorkflow(
            @PathVariable Long applicationId,
            @RequestBody Map<String, Object> workflowDefinition) {
        log.info("POST /api/applications/{}/keep/workflows", applicationId);
        
        Map<String, Object> result = keepIntegrationService.createKeepWorkflow(workflowDefinition);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    @GetMapping("/keep/workflows")
    public ResponseEntity<List<Map<String, Object>>> getKeepWorkflows(
            @PathVariable Long applicationId) {
        log.info("GET /api/applications/{}/keep/workflows", applicationId);
        
        List<Map<String, Object>> workflows = keepIntegrationService.getAllKeepWorkflows();
        
        return ResponseEntity.ok(workflows);
    }
    
    @GetMapping("/keep/workflows/{workflowId}")
    public ResponseEntity<Map<String, Object>> getKeepWorkflow(
            @PathVariable Long applicationId,
            @PathVariable String workflowId) {
        log.info("GET /api/applications/{}/keep/workflows/{}", applicationId, workflowId);
        
        Map<String, Object> workflow = keepIntegrationService.getKeepWorkflow(workflowId);
        
        return ResponseEntity.ok(workflow);
    }
    
    @PutMapping("/keep/workflows/{workflowId}")
    public ResponseEntity<Map<String, Object>> updateKeepWorkflow(
            @PathVariable Long applicationId,
            @PathVariable String workflowId,
            @RequestBody Map<String, Object> workflowDefinition) {
        log.info("PUT /api/applications/{}/keep/workflows/{}", applicationId, workflowId);
        
        Map<String, Object> result = keepIntegrationService.updateKeepWorkflow(workflowId, workflowDefinition);
        
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/keep/workflows/{workflowId}")
    public ResponseEntity<Void> deleteKeepWorkflow(
            @PathVariable Long applicationId,
            @PathVariable String workflowId) {
        log.info("DELETE /api/applications/{}/keep/workflows/{}", applicationId, workflowId);
        
        keepIntegrationService.deleteKeepWorkflow(workflowId);
        
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/keep/workflows/{workflowId}/status")
    public ResponseEntity<Map<String, Object>> getWorkflowStatus(
            @PathVariable Long applicationId,
            @PathVariable String workflowId) {
        log.info("GET /api/applications/{}/keep/workflows/{}/status", applicationId, workflowId);
        
        Map<String, Object> status = keepIntegrationService.checkWorkflowStatus(workflowId);
        
        return ResponseEntity.ok(status);
    }
}
