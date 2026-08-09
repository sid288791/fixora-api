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

    // Not persisted -- set only on the response from an update/create when the Keep workflow
    // failed to sync, so the caller (UI) can surface a warning instead of silently believing
    // everything (including e.g. the AI toggle) actually took effect in Keep.
    private String keepSyncWarning;
}
