package org.simulynx.fixora.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationChannelDTO {
    private Long id;
    private Long applicationId;
    private Long serviceId;
    private String owningAdGrp;
    private String name;
    private String description;
    private String channelType;
    private String endpoint;
    private String configuration;
    private String keepProviderId;
    private Boolean isDefault;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
