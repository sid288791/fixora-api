package org.simulynx.fixora.service;

import lombok.extern.slf4j.Slf4j;
import org.simulynx.fixora.dto.AlertConfigurationDTO;
import org.simulynx.fixora.entity.AlertConfiguration;
import org.simulynx.fixora.repository.AlertConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class AlertConfigurationService {
    
    @Autowired
    private AlertConfigurationRepository alertConfigurationRepository;
    
    @Autowired
    private AuditService auditService;
    
    public AlertConfigurationDTO createAlertConfiguration(AlertConfigurationDTO dto) {
        log.info("Creating alert configuration for application {}", dto.getApplicationId());
        
        AlertConfiguration entity = convertDtoToEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        
        AlertConfiguration saved = alertConfigurationRepository.save(entity);
        
        auditService.logAction(dto.getApplicationId(), null, dto.getOwningAdGrp(), 
                "ALERT_CONFIG", String.valueOf(saved.getId()), "CREATE", null, 
                convertEntityToDto(saved));
        
        log.info("Alert configuration created with id {}", saved.getId());
        return convertEntityToDto(saved);
    }
    
    public AlertConfigurationDTO getAlertConfiguration(Long applicationId, Long configId) {
        log.info("Fetching alert configuration {} for application {}", configId, applicationId);
        
        AlertConfiguration entity = alertConfigurationRepository.findByIdAndApplicationId(configId, applicationId)
                .orElseThrow(() -> new RuntimeException("Alert configuration not found"));
        
        return convertEntityToDto(entity);
    }
    
    public List<AlertConfigurationDTO> getAlertConfigurationsByApplication(Long applicationId) {
        log.info("Fetching all alert configurations for application {}", applicationId);
        
        return alertConfigurationRepository.findByApplicationId(applicationId)
                .stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }
    
    public List<AlertConfigurationDTO> getAlertConfigurationsByApplicationAndService(Long applicationId, Long serviceId) {
        log.info("Fetching alert configurations for application {} and service {}", applicationId, serviceId);
        
        return alertConfigurationRepository.findByApplicationIdAndServiceId(applicationId, serviceId)
                .stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }
    
    public AlertConfigurationDTO updateAlertConfiguration(Long applicationId, Long configId, AlertConfigurationDTO dto) {
        log.info("Updating alert configuration {} for application {}", configId, applicationId);
        
        AlertConfiguration existing = alertConfigurationRepository.findByIdAndApplicationId(configId, applicationId)
                .orElseThrow(() -> new RuntimeException("Alert configuration not found"));
        
        AlertConfigurationDTO oldValues = convertEntityToDto(existing);
        
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setAlertType(dto.getAlertType());
        existing.setSeverity(dto.getSeverity());
        existing.setConditionExpression(dto.getConditionExpression());
        existing.setEnvironment(dto.getEnvironment());
        existing.setSource(dto.getSource());
        existing.setChannels(dto.getChannels());
        existing.setGoalertServiceUrl(dto.getGoalertServiceUrl());
        existing.setTeamsWebhookUrl(dto.getTeamsWebhookUrl());
        if (dto.getTriggerAiInvestigation() != null) {
            existing.setTriggerAiInvestigation(dto.getTriggerAiInvestigation());
        }
        existing.setEnabled(dto.getEnabled());
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        
        AlertConfiguration updated = alertConfigurationRepository.save(existing);
        
        auditService.logAction(applicationId, existing.getServiceId(), dto.getOwningAdGrp(), 
                "ALERT_CONFIG", String.valueOf(configId), "UPDATE", oldValues, 
                convertEntityToDto(updated));
        
        log.info("Alert configuration updated with id {}", configId);
        return convertEntityToDto(updated);
    }
    
    public void deleteAlertConfiguration(Long applicationId, Long configId) {
        log.info("Deleting alert configuration {} for application {}", configId, applicationId);
        
        AlertConfiguration existing = alertConfigurationRepository.findByIdAndApplicationId(configId, applicationId)
                .orElseThrow(() -> new RuntimeException("Alert configuration not found"));
        
        AlertConfigurationDTO deletedValues = convertEntityToDto(existing);
        
        alertConfigurationRepository.delete(existing);
        
        auditService.logAction(applicationId, existing.getServiceId(), existing.getOwningAdGrp(), 
                "ALERT_CONFIG", String.valueOf(configId), "DELETE", deletedValues, null);
        
        log.info("Alert configuration deleted with id {}", configId);
    }
    
    private AlertConfigurationDTO convertEntityToDto(AlertConfiguration entity) {
        AlertConfigurationDTO dto = new AlertConfigurationDTO();
        dto.setId(entity.getId());
        dto.setApplicationId(entity.getApplicationId());
        dto.setServiceId(entity.getServiceId());
        dto.setOwningAdGrp(entity.getOwningAdGrp());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setAlertType(entity.getAlertType());
        dto.setSeverity(entity.getSeverity());
        dto.setConditionExpression(entity.getConditionExpression());
        dto.setEnvironment(entity.getEnvironment());
        dto.setSource(entity.getSource());
        dto.setChannels(entity.getChannels());
        dto.setGoalertServiceUrl(entity.getGoalertServiceUrl());
        dto.setTeamsWebhookUrl(entity.getTeamsWebhookUrl());
        dto.setTriggerAiInvestigation(entity.getTriggerAiInvestigation());
        dto.setEnabled(entity.getEnabled());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
    
    private AlertConfiguration convertDtoToEntity(AlertConfigurationDTO dto) {
        AlertConfiguration entity = new AlertConfiguration();
        entity.setApplicationId(dto.getApplicationId());
        entity.setServiceId(dto.getServiceId());
        entity.setOwningAdGrp(dto.getOwningAdGrp());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setAlertType(dto.getAlertType());
        entity.setSeverity(dto.getSeverity());
        entity.setConditionExpression(dto.getConditionExpression());
        entity.setEnvironment(dto.getEnvironment());
        entity.setSource(dto.getSource());
        entity.setChannels(dto.getChannels());
        entity.setGoalertServiceUrl(dto.getGoalertServiceUrl());
        entity.setTeamsWebhookUrl(dto.getTeamsWebhookUrl());
        entity.setTriggerAiInvestigation(dto.getTriggerAiInvestigation() != null ? dto.getTriggerAiInvestigation() : false);
        entity.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        return entity;
    }
}
