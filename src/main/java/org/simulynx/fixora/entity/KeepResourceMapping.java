package org.simulynx.fixora.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "keep_resource_mappings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeepResourceMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "application_id", nullable = false)
    private Long applicationId;
    
    @Column(name = "service_id")
    private Long serviceId;
    
    @Column(name = "owning_ad_grp", nullable = false)
    private String owningAdGrp;
    
    @Column(name = "resource_type", nullable = false)
    private String resourceType;
    
    @Column(name = "local_id", nullable = false)
    private String localId;
    
    @Column(name = "keep_resource_id", nullable = false)
    private String keepResourceId;
    
    @Column(name = "keep_resource_name")
    private String keepResourceName;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
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
