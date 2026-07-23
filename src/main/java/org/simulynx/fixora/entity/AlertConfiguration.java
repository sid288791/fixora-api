package org.simulynx.fixora.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertConfiguration {
    
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
    
    @Column(name = "alert_type", nullable = false)
    private String alertType;
    
    @Column(name = "severity", nullable = false)
    private String severity;
    
    @Column(name = "condition_expression", columnDefinition = "TEXT")
    private String conditionExpression;
    
    @Column(name = "environment")
    private String environment;
    
    @Column(name = "source")
    private String source;
    
    @Column(name = "channels")
    private String channels;
    
    @Column(name = "goalert_service_url")
    private String goalertServiceUrl;
    
    @Column(name = "teams_webhook_url")
    private String teamsWebhookUrl;
    
    @Column(name = "trigger_ai_investigation", nullable = false)
    private Boolean triggerAiInvestigation = false;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne
    @JoinColumn(name = "application_id", insertable = false, updatable = false)
    private Application application;
    
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
