package org.simulynx.fixora.repository;

import org.simulynx.fixora.entity.AlertWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertWorkflowRepository extends JpaRepository<AlertWorkflow, Long> {
    List<AlertWorkflow> findByApplicationId(Long applicationId);
    List<AlertWorkflow> findByApplicationIdAndStatus(Long applicationId, String status);
    Optional<AlertWorkflow> findByIdAndApplicationId(Long id, Long applicationId);
    List<AlertWorkflow> findByApplicationIdAndServiceId(Long applicationId, Long serviceId);
    List<AlertWorkflow> findByOwningAdGrp(String owningAdGrp);
    List<AlertWorkflow> findByAlertConfigurationId(Long alertConfigurationId);
    Optional<AlertWorkflow> findByKeepWorkflowId(String keepWorkflowId);
    List<AlertWorkflow> findByIsActiveTrue();
}
