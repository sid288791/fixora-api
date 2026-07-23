package org.simulynx.fixora.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeepProviderDTO {
    private String id;
    private String name;
    private String type;
    private String description;
    private String icon;
    private String configuration;
    private Boolean isInstalled;
    private Boolean isConfigured;
    private String documentation;
}
