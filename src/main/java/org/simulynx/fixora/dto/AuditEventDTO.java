package org.simulynx.fixora.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventDTO {
    private Long id;
    private Long applicationId;
    private Long serviceId;
    private String owningAdGrp;
    private String entityType;
    private String entityId;
    private String action;
    private String performedBy;
    private String oldValues;
    private String newValues;
    private String changeSummary;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
