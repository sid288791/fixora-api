package org.simulynx.fixora.repository;

import org.simulynx.fixora.entity.KeepResourceMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeepResourceMappingRepository extends JpaRepository<KeepResourceMapping, Long> {
    List<KeepResourceMapping> findByApplicationId(Long applicationId);
    List<KeepResourceMapping> findByApplicationIdAndStatus(Long applicationId, String status);
    Optional<KeepResourceMapping> findByIdAndApplicationId(Long id, Long applicationId);
    List<KeepResourceMapping> findByApplicationIdAndServiceId(Long applicationId, Long serviceId);
    List<KeepResourceMapping> findByOwningAdGrp(String owningAdGrp);
    Optional<KeepResourceMapping> findByLocalIdAndResourceType(String localId, String resourceType);
    Optional<KeepResourceMapping> findByKeepResourceId(String keepResourceId);
    List<KeepResourceMapping> findByApplicationIdAndResourceType(Long applicationId, String resourceType);
}
