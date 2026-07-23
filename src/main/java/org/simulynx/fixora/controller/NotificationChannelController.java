package org.simulynx.fixora.controller;

import lombok.extern.slf4j.Slf4j;
import org.simulynx.fixora.dto.NotificationChannelDTO;
import org.simulynx.fixora.service.NotificationChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/applications/{applicationId}/notification-channels")
public class NotificationChannelController {
    
    @Autowired
    private NotificationChannelService notificationChannelService;
    
    @PostMapping
    public ResponseEntity<NotificationChannelDTO> createNotificationChannel(
            @PathVariable Long applicationId,
            @RequestBody NotificationChannelDTO dto) {
        log.info("POST /api/applications/{}/notification-channels", applicationId);
        
        dto.setApplicationId(applicationId);
        NotificationChannelDTO created = notificationChannelService.createNotificationChannel(dto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    public ResponseEntity<List<NotificationChannelDTO>> getNotificationChannels(
            @PathVariable Long applicationId) {
        log.info("GET /api/applications/{}/notification-channels", applicationId);
        
        List<NotificationChannelDTO> channels = notificationChannelService.getNotificationChannelsByApplication(applicationId);
        
        return ResponseEntity.ok(channels);
    }
    
    @PutMapping("/{channelId}")
    public ResponseEntity<NotificationChannelDTO> updateNotificationChannel(
            @PathVariable Long applicationId,
            @PathVariable Long channelId,
            @RequestBody NotificationChannelDTO dto) {
        log.info("PUT /api/applications/{}/notification-channels/{}", applicationId, channelId);
        
        dto.setApplicationId(applicationId);
        NotificationChannelDTO updated = notificationChannelService.updateNotificationChannel(applicationId, channelId, dto);
        
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteNotificationChannel(
            @PathVariable Long applicationId,
            @PathVariable Long channelId) {
        log.info("DELETE /api/applications/{}/notification-channels/{}", applicationId, channelId);
        
        notificationChannelService.deleteNotificationChannel(applicationId, channelId);
        
        return ResponseEntity.noContent().build();
    }
}
