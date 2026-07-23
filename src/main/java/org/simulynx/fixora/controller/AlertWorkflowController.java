package org.simulynx.fixora.controller;

import lombok.extern.slf4j.Slf4j;
import org.simulynx.fixora.dto.AlertWorkflowDTO;
import org.simulynx.fixora.service.AlertWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/applications/{applicationId}/alert-workflows")
public class AlertWorkflowController {
    
    @Autowired
    private AlertWorkflowService alertWorkflowService;
    
    @PostMapping
    public ResponseEntity<AlertWorkflowDTO> createAlertWorkflow(
            @PathVariable Long applicationId,
            @RequestBody AlertWorkflowDTO dto) {
        log.info("POST /api/applications/{}/alert-workflows", applicationId);
        
        dto.setApplicationId(applicationId);
        AlertWorkflowDTO created = alertWorkflowService.createAlertWorkflow(dto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    public ResponseEntity<List<AlertWorkflowDTO>> getAlertWorkflows(
            @PathVariable Long applicationId) {
        log.info("GET /api/applications/{}/alert-workflows", applicationId);
        
        List<AlertWorkflowDTO> workflows = alertWorkflowService.getAlertWorkflowsByApplication(applicationId);
        
        return ResponseEntity.ok(workflows);
    }
    
    @GetMapping("/{workflowId}")
    public ResponseEntity<AlertWorkflowDTO> getAlertWorkflow(
            @PathVariable Long applicationId,
            @PathVariable Long workflowId) {
        log.info("GET /api/applications/{}/alert-workflows/{}", applicationId, workflowId);
        
        AlertWorkflowDTO workflow = alertWorkflowService.getAlertWorkflow(applicationId, workflowId);
        
        return ResponseEntity.ok(workflow);
    }
    
    @PutMapping("/{workflowId}")
    public ResponseEntity<AlertWorkflowDTO> updateAlertWorkflow(
            @PathVariable Long applicationId,
            @PathVariable Long workflowId,
            @RequestBody AlertWorkflowDTO dto) {
        log.info("PUT /api/applications/{}/alert-workflows/{}", applicationId, workflowId);
        
        dto.setApplicationId(applicationId);
        AlertWorkflowDTO updated = alertWorkflowService.updateAlertWorkflow(applicationId, workflowId, dto);
        
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{workflowId}")
    public ResponseEntity<Void> deleteAlertWorkflow(
            @PathVariable Long applicationId,
            @PathVariable Long workflowId) {
        log.info("DELETE /api/applications/{}/alert-workflows/{}", applicationId, workflowId);
        
        alertWorkflowService.deleteAlertWorkflow(applicationId, workflowId);
        
        return ResponseEntity.noContent().build();
    }
}
