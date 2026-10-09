package org.simulynx.fixora.service;

import lombok.extern.slf4j.Slf4j;
import org.simulynx.fixora.integration.goalert.GoAlertClient;
import org.simulynx.fixora.util.GoAlertFingerprintTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Closes the loop the other way from KeepIntegrationService.buildGoAlertEscalationAction: when
 * someone acknowledges-and-closes (or just closes) an alert directly in GoAlert -- UI, GoAlert's
 * own API, whatever -- Fixora/Keep never hears about it on its own, because GoAlert's generic
 * incoming API is one-way (Keep -> GoAlert) and GoAlert has no outgoing webhook for status
 * changes. This polls GoAlert instead, on a fixed interval, and mirrors any newly-closed alert's
 * status back into Keep.
 *
 * Disabled (no-ops every tick) unless goalert.service-id is configured -- a fresh checkout with
 * no GoAlert service set up yet should not log errors every 30 seconds.
 */
@Slf4j
@Component
public class GoAlertSyncScheduler {

    @Autowired
    private GoAlertClient goAlertClient;

    @Autowired
    private KeepIntegrationService keepIntegrationService;

    @Value("${goalert.service-id:}")
    private String goalertServiceId;

    // Looked back past the previous poll's start on every run, rather than exactly from where the
    // last run left off, so a GoAlert closure that lands right at the edge of a polling window
    // can't be missed. Re-resolving an already-resolved Keep alert is a harmless no-op.
    private static final long OVERLAP_BUFFER_SECONDS = 120;

    private final AtomicReference<Instant> lastPolledAt = new AtomicReference<>(Instant.now());

    @Scheduled(fixedDelayString = "${goalert.sync.poll-interval-ms:30000}")
    public void syncClosedAlerts() {
        if (goalertServiceId == null || goalertServiceId.isBlank()) {
            return;
        }

        Instant pollWindowStart = lastPolledAt.get().minusSeconds(OVERLAP_BUFFER_SECONDS);
        Instant pollStartedAt = Instant.now();

        List<Map<String, Object>> closedAlerts;
        try {
            closedAlerts = goAlertClient.getRecentlyClosedAlerts(goalertServiceId, pollWindowStart);
        } catch (Exception e) {
            log.error("GoAlert closed-alert sync poll failed, will retry next tick", e);
            return;
        }

        for (Map<String, Object> alert : closedAlerts) {
            try {
                String details = String.valueOf(alert.getOrDefault("details", ""));
                String fingerprint = GoAlertFingerprintTag.extract(details);
                if (fingerprint == null) {
                    log.debug("Closed GoAlert alert {} has no fixora fingerprint tag, skipping",
                            alert.get("id"));
                    continue;
                }
                keepIntegrationService.resolveAlertByFingerprint(fingerprint);
            } catch (Exception e) {
                log.error("Failed to sync closed GoAlert alert {} back into Keep", alert.get("id"), e);
            }
        }

        lastPolledAt.set(pollStartedAt);
    }
}
