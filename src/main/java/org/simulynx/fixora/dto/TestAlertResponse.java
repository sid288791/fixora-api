package org.simulynx.fixora.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestAlertResponse {
    private Boolean success;
    private String message;
    private String alertId;
    private String channelId;
    private LocalDateTime sentAt;
    private String status;
}
