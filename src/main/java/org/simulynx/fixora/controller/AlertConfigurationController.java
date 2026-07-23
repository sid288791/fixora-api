package org.simulynx.fixora.controller;

import lombok.extern.slf4j.Slf4j;
import org.simulynx.fixora.dto.AlertConfigurationDTO;
import org.simulynx.fixora.service.AlertAccessControlService;
import org.simulynx.fixora.service.AlertConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/applications/{applicationId}/alert-configurations")
public class AlertConfigurationController {
    
    @Autowired
    private AlertConfigurationService alertConfigurationService;

    @Autowired
    private AlertAccessControlService accessControlService;
    
    @PostMapping
    public ResponseEntity<AlertConfigurationDTO> createAlertConfiguration(
            @PathVariable Long applicationId,
            @RequestHeader(value = "X-AD-Group", required = false) String userAdGroup,
            @RequestBody AlertConfigurationDTO dto) {
        log.info("POST /api/applications/{}/alert-configurations", applicationId);
        accessControlService.assertAuthorized(applicationId, userAdGroup);
        
        dto.setApplicationId(applicationId);
        AlertConfigurationDTO created = alertConfigurationService.createAlertConfiguration(dto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    public ResponseEntity<List<AlertConfigurationDTO>> getAlertConfigurations(
            @PathVariable Long applicationId,
            @RequestHeader(value = "X-AD-Group", required = false) String userAdGroup) {
        log.info("GET /api/applications/{}/alert-configurations", applicationId);
        accessControlService.assertAuthorized(applicationId, userAdGroup);
        
        List<AlertConfigurationDTO> configurations = alertConfigurationService.getAlertConfigurationsByApplication(applicationId);
        
        return ResponseEntity.ok(configurations);
    }
    
    @GetMapping("/{configurationId}")
    public ResponseEntity<AlertConfigurationDTO> getAlertConfiguration(
            @PathVariable Long applicationId,
            @PathVariable Long configurationId,
            @RequestHeader(value = "X-AD-Group", required = false) String userAdGroup) {
        log.info("GET /api/applications/{}/alert-configurations/{}", applicationId, configurationId);
        accessControlService.assertAuthorized(applicationId, userAdGroup);
        
        AlertConfigurationDTO configuration = alertConfigurationService.getAlertConfiguration(applicationId, configurationId);
        
        return ResponseEntity.ok(configuration);
    }
    
    @PutMapping("/{configurationId}")
    public ResponseEntity<AlertConfigurationDTO> updateAlertConfiguration(
            @PathVariable Long applicationId,
            @PathVariable Long configurationId,
            @RequestHeader(value = "X-AD-Group", required = false) String userAdGroup,
            @RequestBody AlertConfigurationDTO dto) {
        log.info("PUT /api/applications/{}/alert-configurations/{}", applicationId, configurationId);
        accessControlService.assertAuthorized(applicationId, userAdGroup);
        
        dto.setApplicationId(applicationId);
        AlertConfigurationDTO updated = alertConfigurationService.updateAlertConfiguration(applicationId, configurationId, dto);
        
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{configurationId}")
    public ResponseEntity<Void> deleteAlertConfiguration(
            @PathVariable Long applicationId,
            @PathVariable Long configurationId,
            @RequestHeader(value = "X-AD-Group", required = false) String userAdGroup) {
        log.info("DELETE /api/applications/{}/alert-configurations/{}", applicationId, configurationId);
        accessControlService.assertAuthorized(applicationId, userAdGroup);
        
        alertConfigurationService.deleteAlertConfiguration(applicationId, configurationId);
        
        return ResponseEntity.noContent().build();
    }
}
