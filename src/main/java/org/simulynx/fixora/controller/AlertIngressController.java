package org.simulynx.fixora.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.simulynx.fixora.service.AlertIngressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/alert-ingress")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AlertIngressController {
    
    @Autowired
    private AlertIngressService alertIngressService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Generic webhook endpoint to accept alerts from various sources (Grafana, Prometheus, etc.)
     * 
     * @param integrationId The unique integration ID of the application
     * @param sourceType The type of alert source (e.g., grafana, prometheus, datadog, etc.)
     * @param alertPayload The alert payload in any format
     * @return Response with status and alert ID
     */
    @PostMapping("/{integrationId}/{sourceType}")
    public ResponseEntity<Map<String, Object>> ingestAlert(
            @PathVariable String integrationId,
            @PathVariable String sourceType,
            @RequestBody Map<String, Object> alertPayload) {
        
        log.info("Received alert ingress request - integrationId: {}, sourceType: {}", integrationId, sourceType);
        
        Map<String, Object> response = alertIngressService.ingestAlert(integrationId, sourceType, alertPayload);
        
        if ("success".equals(response.get("status"))) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(400).body(response);
        }
    }
    
    /**
     * Health check endpoint for alert ingress
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "service", "alert-ingress",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
