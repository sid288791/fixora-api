package org.simulynx.fixora.repository;

import org.simulynx.fixora.entity.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, Long> {
    List<NotificationChannel> findByApplicationId(Long applicationId);
    List<NotificationChannel> findByApplicationIdAndStatus(Long applicationId, String status);
    Optional<NotificationChannel> findByIdAndApplicationId(Long id, Long applicationId);
    List<NotificationChannel> findByApplicationIdAndServiceId(Long applicationId, Long serviceId);
    List<NotificationChannel> findByOwningAdGrp(String owningAdGrp);
    Optional<NotificationChannel> findByApplicationIdAndIsDefaultTrue(Long applicationId);
}
