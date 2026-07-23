package org.simulynx.fixora.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertWorkflowDTO {
    private Long id;
    private Long applicationId;
    private Long serviceId;
    private String owningAdGrp;
    private String name;
    private String description;
    private String keepWorkflowId;
    private Long alertConfigurationId;
    private String notificationChannelIds;
    private Boolean isActive;
    private LocalDateTime lastTriggeredAt;
    private Long executionCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
