package org.simulynx.fixora.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertConfigurationDTO {
    private Long id;
    private Long applicationId;
    private Long serviceId;
    private String owningAdGrp;
    private String name;
    private String description;
    private String alertType;
    private String severity;
    private String conditionExpression;
    private String environment;
    private String source;
    private String channels;
    private String goalertServiceUrl;
    private String teamsWebhookUrl;
    private Boolean triggerAiInvestigation;
    private Boolean enabled;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
