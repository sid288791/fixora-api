package org.simulynx.fixora.repository;

import org.simulynx.fixora.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findByApplicationId(Long applicationId);
    List<AuditEvent> findByApplicationIdAndEntityType(Long applicationId, String entityType);
    List<AuditEvent> findByApplicationIdAndEntityId(Long applicationId, String entityId);
    List<AuditEvent> findByOwningAdGrp(String owningAdGrp);
    Page<AuditEvent> findByApplicationIdOrderByCreatedAtDesc(Long applicationId, Pageable pageable);
    List<AuditEvent> findByApplicationIdAndCreatedAtBetween(Long applicationId, LocalDateTime startTime, LocalDateTime endTime);
    List<AuditEvent> findByApplicationIdAndAction(Long applicationId, String action);
}
