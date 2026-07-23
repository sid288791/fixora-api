package org.simulynx.fixora.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestAlertRequest {
    private Long alertConfigurationId;
    private Long notificationChannelId;
    private String title;
    private String message;
    private String severity;
    private String source;
}
