package org.simulynx.fixora.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.simulynx.fixora.entity.AuditEvent;
import org.simulynx.fixora.repository.AuditEventRepository;
import org.simulynx.fixora.util.GsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
public class AuditService {
    
    @Autowired
    private AuditEventRepository auditEventRepository;
    
    private final Gson gson = GsonUtil.getInstance();
    
    public void logAction(Long applicationId, Long serviceId, String owningAdGrp, String entityType, 
                         String entityId, String action, Object oldValues, Object newValues) {
        try {
            AuditEvent event = new AuditEvent();
            event.setApplicationId(applicationId);
            event.setServiceId(serviceId);
            event.setOwningAdGrp(owningAdGrp);
            event.setEntityType(entityType);
            event.setEntityId(entityId);
            event.setAction(action);
            event.setPerformedBy(System.getProperty("user.name", "SYSTEM"));
            
            if (oldValues != null) {
                event.setOldValues(gson.toJson(oldValues));
            }
            
            if (newValues != null) {
                event.setNewValues(gson.toJson(newValues));
            }
            
            event.setChangeSummary(String.format("%s %s for %s", action, entityType, entityId));
            event.setStatus("ACTIVE");
            event.setCreatedAt(LocalDateTime.now());
            event.setUpdatedAt(LocalDateTime.now());
            
            auditEventRepository.save(event);
            
            log.debug("Audit event logged: {} {} for entity {}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Error logging audit event", e);
        }
    }
}
