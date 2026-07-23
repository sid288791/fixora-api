package org.simulynx.fixora.repository;

import org.simulynx.fixora.entity.AlertConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertConfigurationRepository extends JpaRepository<AlertConfiguration, Long> {
    List<AlertConfiguration> findByApplicationId(Long applicationId);
    List<AlertConfiguration> findByApplicationIdAndStatus(Long applicationId, String status);
    Optional<AlertConfiguration> findByIdAndApplicationId(Long id, Long applicationId);
    List<AlertConfiguration> findByApplicationIdAndServiceId(Long applicationId, Long serviceId);
    List<AlertConfiguration> findByOwningAdGrp(String owningAdGrp);
}
