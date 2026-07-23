package org.simulynx.fixora.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert_workflow")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertWorkflow {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "application_id", nullable = false)
    private Long applicationId;
    
    @Column(name = "service_id")
    private Long serviceId;
    
    @Column(name = "owning_ad_grp", nullable = false)
    private String owningAdGrp;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "keep_workflow_id")
    private String keepWorkflowId;
    
    @Column(name = "alert_configuration_id", nullable = false)
    private Long alertConfigurationId;
    
    @Column(name = "notification_channel_ids", columnDefinition = "TEXT")
    private String notificationChannelIds;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "last_triggered_at")
    private LocalDateTime lastTriggeredAt;
    
    @Column(name = "execution_count", nullable = false)
    private Long executionCount = 0L;
    
    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne
    @JoinColumn(name = "application_id", insertable = false, updatable = false)
    private Application application;
    
    @ManyToOne
    @JoinColumn(name = "alert_configuration_id", insertable = false, updatable = false)
    private AlertConfiguration alertConfiguration;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
