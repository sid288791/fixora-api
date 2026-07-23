package org.simulynx.fixora.service;

import lombok.extern.slf4j.Slf4j;
import org.simulynx.fixora.dto.NotificationChannelDTO;
import org.simulynx.fixora.entity.NotificationChannel;
import org.simulynx.fixora.repository.NotificationChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class NotificationChannelService {
    
    @Autowired
    private NotificationChannelRepository notificationChannelRepository;
    
    @Autowired
    private AuditService auditService;
    
    public NotificationChannelDTO createNotificationChannel(NotificationChannelDTO dto) {
        log.info("Creating notification channel for application {}", dto.getApplicationId());
        
        NotificationChannel entity = convertDtoToEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        
        NotificationChannel saved = notificationChannelRepository.save(entity);
        
        auditService.logAction(dto.getApplicationId(), null, dto.getOwningAdGrp(), 
                "NOTIFICATION_CHANNEL", String.valueOf(saved.getId()), "CREATE", null, 
                convertEntityToDto(saved));
        
        log.info("Notification channel created with id {}", saved.getId());
        return convertEntityToDto(saved);
    }
    
    public NotificationChannelDTO getNotificationChannel(Long applicationId, Long channelId) {
        log.info("Fetching notification channel {} for application {}", channelId, applicationId);
        
        NotificationChannel entity = notificationChannelRepository.findByIdAndApplicationId(channelId, applicationId)
                .orElseThrow(() -> new RuntimeException("Notification channel not found"));
        
        return convertEntityToDto(entity);
    }
    
    public List<NotificationChannelDTO> getNotificationChannelsByApplication(Long applicationId) {
        log.info("Fetching all notification channels for application {}", applicationId);
        
        return notificationChannelRepository.findByApplicationId(applicationId)
                .stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }
    
    public List<NotificationChannelDTO> getNotificationChannelsByApplicationAndService(Long applicationId, Long serviceId) {
        log.info("Fetching notification channels for application {} and service {}", applicationId, serviceId);
        
        return notificationChannelRepository.findByApplicationIdAndServiceId(applicationId, serviceId)
                .stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }
    
    public NotificationChannelDTO updateNotificationChannel(Long applicationId, Long channelId, NotificationChannelDTO dto) {
        log.info("Updating notification channel {} for application {}", channelId, applicationId);
        
        NotificationChannel existing = notificationChannelRepository.findByIdAndApplicationId(channelId, applicationId)
                .orElseThrow(() -> new RuntimeException("Notification channel not found"));
        
        NotificationChannelDTO oldValues = convertEntityToDto(existing);
        
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setChannelType(dto.getChannelType());
        existing.setEndpoint(dto.getEndpoint());
        existing.setConfiguration(dto.getConfiguration());
        existing.setIsDefault(dto.getIsDefault());
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        
        NotificationChannel updated = notificationChannelRepository.save(existing);
        
        auditService.logAction(applicationId, existing.getServiceId(), dto.getOwningAdGrp(), 
                "NOTIFICATION_CHANNEL", String.valueOf(channelId), "UPDATE", oldValues, 
                convertEntityToDto(updated));
        
        log.info("Notification channel updated with id {}", channelId);
        return convertEntityToDto(updated);
    }
    
    public void deleteNotificationChannel(Long applicationId, Long channelId) {
        log.info("Deleting notification channel {} for application {}", channelId, applicationId);
        
        NotificationChannel existing = notificationChannelRepository.findByIdAndApplicationId(channelId, applicationId)
                .orElseThrow(() -> new RuntimeException("Notification channel not found"));
        
        NotificationChannelDTO deletedValues = convertEntityToDto(existing);
        
        notificationChannelRepository.delete(existing);
        
        auditService.logAction(applicationId, existing.getServiceId(), existing.getOwningAdGrp(), 
                "NOTIFICATION_CHANNEL", String.valueOf(channelId), "DELETE", deletedValues, null);
        
        log.info("Notification channel deleted with id {}", channelId);
    }
    
    private NotificationChannelDTO convertEntityToDto(NotificationChannel entity) {
        NotificationChannelDTO dto = new NotificationChannelDTO();
        dto.setId(entity.getId());
        dto.setApplicationId(entity.getApplicationId());
        dto.setServiceId(entity.getServiceId());
        dto.setOwningAdGrp(entity.getOwningAdGrp());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setChannelType(entity.getChannelType());
        dto.setEndpoint(entity.getEndpoint());
        dto.setConfiguration(entity.getConfiguration());
        dto.setKeepProviderId(entity.getKeepProviderId());
        dto.setIsDefault(entity.getIsDefault());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
    
    private NotificationChannel convertDtoToEntity(NotificationChannelDTO dto) {
        NotificationChannel entity = new NotificationChannel();
        entity.setApplicationId(dto.getApplicationId());
        entity.setServiceId(dto.getServiceId());
        entity.setOwningAdGrp(dto.getOwningAdGrp());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setChannelType(dto.getChannelType());
        entity.setEndpoint(dto.getEndpoint());
        entity.setConfiguration(dto.getConfiguration());
        entity.setKeepProviderId(dto.getKeepProviderId());
        entity.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        return entity;
    }
}
