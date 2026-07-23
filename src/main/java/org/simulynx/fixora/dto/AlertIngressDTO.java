package org.simulynx.fixora.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertIngressDTO {
    private String integrationId;
    private String sourceType;
    private Map<String, Object> payload = new HashMap<>();
    private LocalDateTime receivedAt;
    
    @JsonAnySetter
    public void setPayloadField(String key, Object value) {
        payload.put(key, value);
    }
}
