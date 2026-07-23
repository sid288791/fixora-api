package org.simulynx.fixora.service;

import lombok.extern.slf4j.Slf4j;
import org.simulynx.fixora.dto.AlertWorkflowDTO;
import org.simulynx.fixora.entity.AlertWorkflow;
import org.simulynx.fixora.repository.AlertWorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class AlertWorkflowService {
    
    @Autowired
    private AlertWorkflowRepository alertWorkflowRepository;
    
    @Autowired
    private AuditService auditService;
    
    public AlertWorkflowDTO createAlertWorkflow(AlertWorkflowDTO dto) {
        log.info("Creating alert workflow for application {}", dto.getApplicationId());
        
        AlertWorkflow entity = convertDtoToEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setExecutionCount(0L);
        
        AlertWorkflow saved = alertWorkflowRepository.save(entity);
        
        auditService.logAction(dto.getApplicationId(), null, dto.getOwningAdGrp(), 
                "ALERT_WORKFLOW", String.valueOf(saved.getId()), "CREATE", null, 
                convertEntityToDto(saved));
        
        log.info("Alert workflow created with id {}", saved.getId());
        return convertEntityToDto(saved);
    }
    
    public AlertWorkflowDTO getAlertWorkflow(Long applicationId, Long workflowId) {
        log.info("Fetching alert workflow {} for application {}", workflowId, applicationId);
        
        AlertWorkflow entity = alertWorkflowRepository.findByIdAndApplicationId(workflowId, applicationId)
                .orElseThrow(() -> new RuntimeException("Alert workflow not found"));
        
        return convertEntityToDto(entity);
    }
    
    public List<AlertWorkflowDTO> getAlertWorkflowsByApplication(Long applicationId) {
        log.info("Fetching all alert workflows for application {}", applicationId);
        
        return alertWorkflowRepository.findByApplicationId(applicationId)
                .stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }
    
    public List<AlertWorkflowDTO> getAlertWorkflowsByApplicationAndService(Long applicationId, Long serviceId) {
        log.info("Fetching alert workflows for application {} and service {}", applicationId, serviceId);
        
        return alertWorkflowRepository.findByApplicationIdAndServiceId(applicationId, serviceId)
                .stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }
    
    public AlertWorkflowDTO updateAlertWorkflow(Long applicationId, Long workflowId, AlertWorkflowDTO dto) {
        log.info("Updating alert workflow {} for application {}", workflowId, applicationId);
        
        AlertWorkflow existing = alertWorkflowRepository.findByIdAndApplicationId(workflowId, applicationId)
                .orElseThrow(() -> new RuntimeException("Alert workflow not found"));
        
        AlertWorkflowDTO oldValues = convertEntityToDto(existing);
        
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setNotificationChannelIds(dto.getNotificationChannelIds());
        existing.setIsActive(dto.getIsActive());
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        
        AlertWorkflow updated = alertWorkflowRepository.save(existing);
        
        auditService.logAction(applicationId, existing.getServiceId(), dto.getOwningAdGrp(), 
                "ALERT_WORKFLOW", String.valueOf(workflowId), "UPDATE", oldValues, 
                convertEntityToDto(updated));
        
        log.info("Alert workflow updated with id {}", workflowId);
        return convertEntityToDto(updated);
    }
    
    public void deleteAlertWorkflow(Long applicationId, Long workflowId) {
        log.info("Deleting alert workflow {} for application {}", workflowId, applicationId);
        
        AlertWorkflow existing = alertWorkflowRepository.findByIdAndApplicationId(workflowId, applicationId)
                .orElseThrow(() -> new RuntimeException("Alert workflow not found"));
        
        AlertWorkflowDTO deletedValues = convertEntityToDto(existing);
        
        alertWorkflowRepository.delete(existing);
        
        auditService.logAction(applicationId, existing.getServiceId(), existing.getOwningAdGrp(), 
                "ALERT_WORKFLOW", String.valueOf(workflowId), "DELETE", deletedValues, null);
        
        log.info("Alert workflow deleted with id {}", workflowId);
    }
    
    public void recordExecution(Long applicationId, Long workflowId) {
        log.info("Recording execution for alert workflow {}", workflowId);
        
        AlertWorkflow existing = alertWorkflowRepository.findByIdAndApplicationId(workflowId, applicationId)
                .orElseThrow(() -> new RuntimeException("Alert workflow not found"));
        
        existing.setLastTriggeredAt(LocalDateTime.now());
        existing.setExecutionCount(existing.getExecutionCount() + 1);
        existing.setUpdatedAt(LocalDateTime.now());
        
        alertWorkflowRepository.save(existing);
    }
    
    private AlertWorkflowDTO convertEntityToDto(AlertWorkflow entity) {
        AlertWorkflowDTO dto = new AlertWorkflowDTO();
        dto.setId(entity.getId());
        dto.setApplicationId(entity.getApplicationId());
        dto.setServiceId(entity.getServiceId());
        dto.setOwningAdGrp(entity.getOwningAdGrp());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setKeepWorkflowId(entity.getKeepWorkflowId());
        dto.setAlertConfigurationId(entity.getAlertConfigurationId());
        dto.setNotificationChannelIds(entity.getNotificationChannelIds());
        dto.setIsActive(entity.getIsActive());
        dto.setLastTriggeredAt(entity.getLastTriggeredAt());
        dto.setExecutionCount(entity.getExecutionCount());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
    
    private AlertWorkflow convertDtoToEntity(AlertWorkflowDTO dto) {
        AlertWorkflow entity = new AlertWorkflow();
        entity.setApplicationId(dto.getApplicationId());
        entity.setServiceId(dto.getServiceId());
        entity.setOwningAdGrp(dto.getOwningAdGrp());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setKeepWorkflowId(dto.getKeepWorkflowId());
        entity.setAlertConfigurationId(dto.getAlertConfigurationId());
        entity.setNotificationChannelIds(dto.getNotificationChannelIds());
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        return entity;
    }
}
