package org.simulynx.fixora.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.simulynx.fixora.entity.Application;
import org.simulynx.fixora.integration.goalert.GoAlertClient;
import org.simulynx.fixora.integration.keep.KeepClient;
import org.simulynx.fixora.integration.orchestrator.OrchestratorClient;
import org.simulynx.fixora.repository.ApplicationRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeepIntegrationServiceDeepAnalysisTest {

    @Mock
    private KeepClient keepClient;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private GoAlertClient goAlertClient;

    @Mock
    private OrchestratorClient orchestratorClient;

    @InjectMocks
    private KeepIntegrationService keepIntegrationService;

    private static final String FINGERPRINT = "fixora-test-99";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(keepIntegrationService, "goalertServiceId", "goalert-service-1");
    }

    private Map<String, Object> existingAlert(Long applicationId) {
        return Map.of(
                "name", "Test alert",
                "message", "Something broke",
                "severity", "critical",
                "fingerprint", FINGERPRINT,
                "labels", Map.of("fixora_application_id", String.valueOf(applicationId))
        );
    }

    @Test
    void runDeepAnalysis_forwardsToOrchestratorAndReturnsResult() {
        Application app = new Application();
        app.setName("payment-service");
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(keepClient.getAlerts()).thenReturn(List.of(existingAlert(1L)));
        when(orchestratorClient.runDeepAnalysis(anyString(), anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(Map.of("root_cause_analysis", Map.of("root_cause", "DB pool exhaustion")));

        Map<String, Object> result = keepIntegrationService.runDeepAnalysis(1L, FINGERPRINT);

        assertEquals("DB pool exhaustion",
                ((Map<?, ?>) result.get("root_cause_analysis")).get("root_cause"));
    }

    @Test
    void runDeepAnalysis_throwsWhenAlertNotFound() {
        Application app = new Application();
        app.setName("payment-service");
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(keepClient.getAlerts()).thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> keepIntegrationService.runDeepAnalysis(1L, FINGERPRINT));
    }

    @Test
    void runDeepAnalysis_refusesAlertBelongingToAnotherApplication() {
        Application app = new Application();
        app.setName("payment-service");
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(keepClient.getAlerts()).thenReturn(List.of(existingAlert(2L)));

        assertThrows(RuntimeException.class, () -> keepIntegrationService.runDeepAnalysis(1L, FINGERPRINT));
    }

    @Test
    void runDeepAnalysis_propagatesOrchestratorFailure() {
        Application app = new Application();
        app.setName("payment-service");
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(keepClient.getAlerts()).thenReturn(List.of(existingAlert(1L)));
        when(orchestratorClient.runDeepAnalysis(anyString(), anyString(), anyString(), anyString(), anyString(), any()))
                .thenThrow(new RestClientException("orchestrator unreachable"));

        assertThrows(RestClientException.class, () -> keepIntegrationService.runDeepAnalysis(1L, FINGERPRINT));
    }
}
