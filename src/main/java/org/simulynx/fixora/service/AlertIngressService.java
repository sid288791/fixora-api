package org.simulynx.fixora.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.simulynx.fixora.dto.AlertIngressDTO;
import org.simulynx.fixora.entity.Application;
import org.simulynx.fixora.repository.ApplicationRepository;
import org.simulynx.fixora.util.GsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class AlertIngressService {
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private AuditService auditService;
    
    private final Gson gson = GsonUtil.getInstance();
    
    public Map<String, Object> ingestAlert(String integrationId, String sourceType, Map<String, Object> alertPayload) {
        log.info("Received alert ingress request from source: {} for integration: {}", sourceType, integrationId);
        
        Application application = applicationRepository.findByIntegrationId(integrationId)
                .orElseThrow(() -> new RuntimeException("Application not found with integrationId: " + integrationId));
        
        Map<String, Object> response = new HashMap<>();
        try {
            log.debug("Alert payload: {}", gson.toJson(alertPayload));
            
            Map<String, Object> alertMetadata = new HashMap<>();
            alertMetadata.put("integrationId", integrationId);
            alertMetadata.put("sourceType", sourceType);
            alertMetadata.put("applicationId", application.getId());
            alertMetadata.put("applicationName", application.getName());
            alertMetadata.put("receivedAt", LocalDateTime.now());
            alertMetadata.put("payload", alertPayload);
            
            auditService.logAction(
                    application.getId(),
                    null,
                    application.getOwnerEmail(),
                    "ALERT_INGRESS",
                    sourceType,
                    "CREATE",
                    null,
                    alertMetadata
            );
            
            response.put("status", "success");
            response.put("message", "Alert ingested successfully");
            response.put("alertId", integrationId + "-" + System.nanoTime());
            response.put("receivedAt", LocalDateTime.now());
            response.put("sourceType", sourceType);
            
            log.info("Successfully ingested alert from {} for integration {}", sourceType, integrationId);
            
        } catch (Exception e) {
            log.error("Error processing alert from {} for integration {}: {}", sourceType, integrationId, e.getMessage(), e);
            
            response.put("status", "error");
            response.put("message", "Failed to process alert: " + e.getMessage());
            response.put("receivedAt", LocalDateTime.now());
            response.put("sourceType", sourceType);
        }
        
        return response;
    }
    
    public Application getApplicationByIntegrationId(String integrationId) {
        log.debug("Fetching application by integrationId: {}", integrationId);
        return applicationRepository.findByIntegrationId(integrationId)
                .orElseThrow(() -> new RuntimeException("Application not found with integrationId: " + integrationId));
    }
}
