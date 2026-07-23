package org.simulynx.fixora.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeepResourceMappingDTO {
    private Long id;
    private Long applicationId;
    private Long serviceId;
    private String owningAdGrp;
    private String resourceType;
    private String localId;
    private String keepResourceId;
    private String keepResourceName;
    private String metadata;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
