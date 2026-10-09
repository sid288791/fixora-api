package org.simulynx.fixora.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.simulynx.fixora.entity.Application;
import org.simulynx.fixora.integration.goalert.GoAlertClient;
import org.simulynx.fixora.integration.keep.KeepClient;
import org.simulynx.fixora.repository.ApplicationRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeepIntegrationServiceTest {

    @Mock
    private KeepClient keepClient;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private GoAlertClient goAlertClient;

    @InjectMocks
    private KeepIntegrationService keepIntegrationService;

    private static final String FINGERPRINT = "fixora-test-99";

    @BeforeEach
    void setUp() {
        // goalertServiceId is @Value-injected in the running app; field-injected here since this
        // is a plain Mockito unit test, not a Spring context test.
        ReflectionTestUtils.setField(keepIntegrationService, "goalertServiceId", "goalert-service-1");
    }

    private Map<String, Object> existingAlert(Long applicationId) {
        return Map.of(
                "name", "Test alert",
                "message", "Something broke",
                "severity", "critical",
                "source", List.of("FIXORA-TEST"),
                "fingerprint", FINGERPRINT,
                "labels", Map.of("fixora_application_id", String.valueOf(applicationId), "fixora_alert_config_id", "1")
        );
    }

    @Test
    void resolveAlertByFingerprint_resolvesWhenAlertExists() {
        when(keepClient.getAlerts()).thenReturn(List.of(existingAlert(1L)));

        boolean result = keepIntegrationService.resolveAlertByFingerprint(FINGERPRINT);

        assertTrue(result);
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(keepClient).reportAlertEvent(payloadCaptor.capture());
        assertEquals("resolved", payloadCaptor.getValue().get("status"));
        assertEquals(FINGERPRINT, payloadCaptor.getValue().get("fingerprint"));
    }

    @Test
    void resolveAlertByFingerprint_returnsFalseWhenNoMatchingAlert() {
        when(keepClient.getAlerts()).thenReturn(List.of());

        boolean result = keepIntegrationService.resolveAlertByFingerprint(FINGERPRINT);

        assertFalse(result);
        verify(keepClient, never()).reportAlertEvent(any());
    }

    @Test
    void closeAlert_throwsWhenApplicationDoesNotExist() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> keepIntegrationService.closeAlert(1L, FINGERPRINT, null));
    }

    @Test
    void closeAlert_refusesToCloseAnAlertBelongingToAnotherApplication() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(new Application()));
        when(keepClient.getAlerts()).thenReturn(List.of(existingAlert(2L)));

        boolean result = keepIntegrationService.closeAlert(1L, FINGERPRINT, null);

        assertFalse(result);
        verify(keepClient, never()).reportAlertEvent(any());
    }

    @Test
    void closeAlert_resolvesInKeepAttachesRcaNoteAndClosesInGoAlert() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(new Application()));
        when(keepClient.getAlerts()).thenReturn(List.of(existingAlert(1L)));
        when(goAlertClient.closeAlertsByFingerprint("goalert-service-1", FINGERPRINT)).thenReturn(1);

        boolean result = keepIntegrationService.closeAlert(1L, FINGERPRINT, "root cause: db pool exhaustion");

        assertTrue(result);
        verify(keepClient).reportAlertEvent(any());
        ArgumentCaptor<Map<String, Object>> enrichCaptor = ArgumentCaptor.forClass(Map.class);
        verify(keepClient).enrichAlert(eq(FINGERPRINT), enrichCaptor.capture());
        assertEquals("root cause: db pool exhaustion", enrichCaptor.getValue().get("fixora_rca_note"));
        verify(goAlertClient).closeAlertsByFingerprint("goalert-service-1", FINGERPRINT);
    }

    @Test
    void closeAlert_skipsEnrichmentWhenNoRcaNoteGiven() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(new Application()));
        when(keepClient.getAlerts()).thenReturn(List.of(existingAlert(1L)));

        keepIntegrationService.closeAlert(1L, FINGERPRINT, null);

        verify(keepClient, never()).enrichAlert(anyString(), any());
    }

    @Test
    void closeAlert_stillReturnsTrueWhenGoAlertCloseFails() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(new Application()));
        when(keepClient.getAlerts()).thenReturn(List.of(existingAlert(1L)));
        doThrow(new RuntimeException("GoAlert unreachable"))
                .when(goAlertClient).closeAlertsByFingerprint(anyString(), anyString());

        boolean result = keepIntegrationService.closeAlert(1L, FINGERPRINT, null);

        assertTrue(result, "Keep was already resolved by this point -- a GoAlert-side failure must not undo that");
        verify(keepClient).reportAlertEvent(any());
    }

    @Test
    void closeAlert_skipsGoAlertCloseWhenNoServiceIdConfigured() {
        ReflectionTestUtils.setField(keepIntegrationService, "goalertServiceId", "");
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(new Application()));
        when(keepClient.getAlerts()).thenReturn(List.of(existingAlert(1L)));

        keepIntegrationService.closeAlert(1L, FINGERPRINT, null);

        verify(goAlertClient, never()).closeAlertsByFingerprint(anyString(), anyString());
    }
}
