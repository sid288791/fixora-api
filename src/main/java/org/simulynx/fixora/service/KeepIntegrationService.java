package org.simulynx.fixora.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.simulynx.fixora.dto.TestAlertRequest;
import org.simulynx.fixora.dto.TestAlertResponse;
import org.simulynx.fixora.entity.Application;
import org.simulynx.fixora.integration.goalert.GoAlertClient;
import org.simulynx.fixora.integration.keep.KeepClient;
import org.simulynx.fixora.repository.ApplicationRepository;
import org.simulynx.fixora.util.GsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Intentionally NOT @Transactional: this class does no DB writes, only reads (via
// applicationRepository) and external HTTP calls to Keep. A class-level @Transactional here
// used to (a) hold a DB connection open across every blocking Keep API call, and (b) cause any
// RuntimeException thrown from a method here -- e.g. a Keep API failure -- to mark the CALLER's
// transaction (AlertConfigurationService's) as rollback-only, so even catching and swallowing
// that exception in the caller still failed the whole request with UnexpectedRollbackException.
@Slf4j
@Service
public class KeepIntegrationService {

    private static final Set<String> ACTIVE_STATUSES = Set.of("firing", "acknowledged");
    private static final Set<String> CLOSED_STATUSES = Set.of("resolved");

    @Autowired
    private KeepClient keepClient;

    @Autowired
    private AlertWorkflowService alertWorkflowService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private GoAlertClient goAlertClient;

    @Value("${goalert.service-id:}")
    private String goalertServiceId;

    private final Gson gson = GsonUtil.getInstance();
    
    public Boolean checkKeepHealth() {
        try {
            log.info("Checking Keep platform health");
            return keepClient.healthCheck();
        } catch (Exception e) {
            log.error("Error checking Keep platform health", e);
            return false;
        }
    }
    
    public Map<String, Object> createKeepProvider(String providerId, Map<String, Object> config) {
        try {
            log.info("Installing Keep provider: {}", providerId);
            Map<String, Object> result = keepClient.installProvider(providerId, config);
            log.info("Successfully installed Keep provider: {}", providerId);
            return result;
        } catch (Exception e) {
            log.error("Error installing Keep provider: {}", providerId, e);
            throw new RuntimeException("Failed to install Keep provider", e);
        }
    }
    
    public void deleteKeepProvider(String providerId) {
        try {
            log.info("Uninstalling Keep provider: {}", providerId);
            keepClient.uninstallProvider(providerId);
            log.info("Successfully uninstalled Keep provider: {}", providerId);
        } catch (Exception e) {
            log.error("Error uninstalling Keep provider: {}", providerId, e);
            throw new RuntimeException("Failed to uninstall Keep provider", e);
        }
    }
    
    public Map<String, Object> updateKeepProvider(String providerId, Map<String, Object> config) {
        try {
            log.info("Updating Keep provider: {}", providerId);
            Map<String, Object> result = keepClient.updateProviderConfig(providerId, config);
            log.info("Successfully updated Keep provider: {}", providerId);
            return result;
        } catch (Exception e) {
            log.error("Error updating Keep provider: {}", providerId, e);
            throw new RuntimeException("Failed to update Keep provider", e);
        }
    }
    
    public Map<String, Object> getKeepProvider(String providerId) {
        try {
            log.info("Fetching Keep provider: {}", providerId);
            Map<String, Object> config = keepClient.getProviderConfig(providerId);
            log.info("Successfully fetched Keep provider: {}", providerId);
            return config;
        } catch (Exception e) {
            log.error("Error fetching Keep provider: {}", providerId, e);
            throw new RuntimeException("Failed to fetch Keep provider", e);
        }
    }
    
    public Map<String, Object> createKeepWorkflow(Map<String, Object> workflowDefinition) {
        try {
            log.info("Creating Keep workflow");
            Map<String, Object> result = keepClient.createWorkflow(workflowDefinition);
            log.info("Successfully created Keep workflow");
            return result;
        } catch (Exception e) {
            log.error("Error creating Keep workflow", e);
            throw new RuntimeException("Failed to create Keep workflow", e);
        }
    }
    
    public Map<String, Object> getKeepWorkflow(String workflowId) {
        try {
            log.info("Fetching Keep workflow: {}", workflowId);
            Map<String, Object> workflow = keepClient.getWorkflow(workflowId);
            log.info("Successfully fetched Keep workflow: {}", workflowId);
            return workflow;
        } catch (Exception e) {
            log.error("Error fetching Keep workflow: {}", workflowId, e);
            throw new RuntimeException("Failed to fetch Keep workflow", e);
        }
    }
    
    public List<Map<String, Object>> getAllKeepWorkflows() {
        try {
            log.info("Fetching all Keep workflows");
            List<Map<String, Object>> workflows = keepClient.getAllWorkflows();
            log.info("Successfully fetched {} Keep workflows", workflows.size());
            return workflows;
        } catch (Exception e) {
            log.error("Error fetching Keep workflows", e);
            throw new RuntimeException("Failed to fetch Keep workflows", e);
        }
    }
    
    public Map<String, Object> updateKeepWorkflow(String workflowId, Map<String, Object> workflowDefinition) {
        try {
            log.info("Updating Keep workflow: {}", workflowId);
            Map<String, Object> result = keepClient.updateWorkflow(workflowId, workflowDefinition);
            log.info("Successfully updated Keep workflow: {}", workflowId);
            return result;
        } catch (Exception e) {
            log.error("Error updating Keep workflow: {}", workflowId, e);
            throw new RuntimeException("Failed to update Keep workflow", e);
        }
    }
    
    /**
     * Marks the Keep alert with the given fingerprint as resolved, by re-sending it to Keep's
     * ingestion endpoint with status "resolved". Keep upserts alerts by fingerprint, so this needs
     * to carry forward the alert's existing name/severity/source/labels rather than just the
     * fingerprint and new status — sending a bare status update would otherwise blank those fields
     * out, and getAlertsForApplication relies on the fixora_application_id/fixora_alert_config_id
     * labels surviving to keep bucketing this alert as belonging to its application after closure.
     * Called by GoAlertSyncScheduler when GoAlert reports the paired alert as closed. Returns false
     * (rather than throwing) when no matching alert is found, since a stale/already-cleared
     * fingerprint from GoAlert shouldn't fail the whole sync poll.
     */
    public boolean resolveAlertByFingerprint(String fingerprint) {
        Map<String, Object> existing = findKeepAlertByFingerprint(fingerprint);
        if (existing == null) {
            log.warn("No Keep alert found for fingerprint {} while syncing a GoAlert closure", fingerprint);
            return false;
        }

        resolveInKeep(existing, fingerprint);
        log.info("Resolved Keep alert with fingerprint {} following GoAlert closure", fingerprint);
        return true;
    }

    /**
     * Closes an alert from the Fixora side (the reverse direction of resolveAlertByFingerprint,
     * called instead when someone closes the alert from the Fixora UI/API rather than from
     * GoAlert). Does two things, both best-effort against GoAlert so a GoAlert outage doesn't
     * block closing the alert in Fixora/Keep: resolves it in Keep, optionally attaching an RCA
     * note as an enrichment, and closes the matching alert in GoAlert so its escalation policy
     * stops paging/texting anyone further for it.
     *
     * @return true if a matching alert belonging to this application was found and resolved
     */
    public boolean closeAlert(Long applicationId, String fingerprint, String rcaNote) {
        applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));

        Map<String, Object> existing = findKeepAlertByFingerprint(fingerprint);
        if (existing == null) {
            log.warn("No Keep alert found for fingerprint {} while closing from Fixora", fingerprint);
            return false;
        }

        Object labelsObj = existing.get("labels");
        String ownerAppId = labelsObj instanceof Map<?, ?> labels
                ? String.valueOf(labels.get("fixora_application_id")) : null;
        if (!String.valueOf(applicationId).equals(ownerAppId)) {
            log.warn("Alert with fingerprint {} does not belong to application {}, refusing to close",
                    fingerprint, applicationId);
            return false;
        }

        resolveInKeep(existing, fingerprint);

        if (rcaNote != null && !rcaNote.isBlank()) {
            keepClient.enrichAlert(fingerprint, Map.of(
                    "fixora_rca_note", rcaNote,
                    "fixora_closed_at", Instant.now().toString()
            ));
        }

        if (goalertServiceId != null && !goalertServiceId.isBlank()) {
            // Best-effort by design (see class javadoc above): a GoAlert outage shouldn't block
            // closing the alert in Fixora/Keep, which has already happened by this point. Caught
            // here specifically (rather than left to propagate) so that outage is distinguishable
            // in the logs from GoAlertClient's normal "0 alerts matched" case -- both used to look
            // identical before this call threw on real failures instead of swallowing them.
            try {
                int closedInGoAlert = goAlertClient.closeAlertsByFingerprint(goalertServiceId, fingerprint);
                log.info("Closed {} matching GoAlert alert(s) for fingerprint {} following manual close in Fixora",
                        closedInGoAlert, fingerprint);
            } catch (Exception e) {
                log.error("Alert {} was closed in Fixora/Keep, but closing the matching GoAlert alert(s) failed "
                        + "-- GoAlert may keep escalating/paging for it until closed manually there", fingerprint, e);
            }
        }

        log.info("Closed alert with fingerprint {} from Fixora (application {})", fingerprint, applicationId);
        return true;
    }

    private Map<String, Object> findKeepAlertByFingerprint(String fingerprint) {
        for (Map<String, Object> alert : keepClient.getAlerts()) {
            if (fingerprint.equals(String.valueOf(alert.get("fingerprint")))) {
                return alert;
            }
        }
        return null;
    }

    private void resolveInKeep(Map<String, Object> existing, String fingerprint) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", existing.get("name"));
        payload.put("status", "resolved");
        payload.put("message", existing.get("message"));
        payload.put("severity", existing.get("severity"));
        Object source = existing.get("source");
        payload.put("source", source instanceof List ? source
                : source != null ? List.of(String.valueOf(source)) : List.of());
        payload.put("lastReceived", Instant.now().toString());
        payload.put("fingerprint", fingerprint);
        Object labels = existing.get("labels");
        if (labels instanceof Map) {
            payload.put("labels", labels);
        }

        keepClient.reportAlertEvent(payload);
    }

    public void deleteKeepWorkflow(String workflowId) {
        try {
            log.info("Deleting Keep workflow: {}", workflowId);
            keepClient.deleteWorkflow(workflowId);
            log.info("Successfully deleted Keep workflow: {}", workflowId);
        } catch (Exception e) {
            log.error("Error deleting Keep workflow: {}", workflowId, e);
            throw new RuntimeException("Failed to delete Keep workflow", e);
        }
    }
    
    public Map<String, Object> checkWorkflowStatus(String workflowId) {
        try {
            log.info("Checking Keep workflow status: {}", workflowId);
            Map<String, Object> status = keepClient.getWorkflowStatus(workflowId);
            log.info("Successfully fetched Keep workflow status: {}", workflowId);
            return status;
        } catch (Exception e) {
            log.error("Error checking Keep workflow status: {}", workflowId, e);
            throw new RuntimeException("Failed to check workflow status", e);
        }
    }
    
    public TestAlertResponse sendTestAlert(Long applicationId, TestAlertRequest request) {
        try {
            log.info("Sending test alert for workflow config from alert configuration {}", request.getAlertConfigurationId());

            Application application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));

            Map<String, Object> payload = new HashMap<>();
            payload.put("name", request.getTitle() != null ? request.getTitle() : "Test Alert");
            payload.put("status", "firing");
            payload.put("message", request.getMessage() != null ? request.getMessage() : "This is a test alert");
            payload.put("severity", request.getSeverity() != null ? request.getSeverity().toLowerCase() : "info");
            payload.put("source", List.of(request.getSource() != null ? request.getSource() : "FIXORA"));
            payload.put("lastReceived", Instant.now().toString());
            payload.put("fingerprint", "fixora-test-" + request.getAlertConfigurationId());
            // Keep has no native "application" concept, so this is how we tie an alert back
            // to the Fixora application it belongs to when listing active/closed alerts later.
            // fixora_alert_config_id also lets each config's Keep workflow trigger scope itself
            // to only its own alerts via a CEL filter (see buildWorkflowDefinition) -- without it,
            // Keep runs every alert-type workflow on every alert event (confirmed via Keep's own
            // workflowmanager.py: a trigger with neither "filters" nor "cel" always evaluates to
            // should_run=True).
            payload.put("labels", Map.of(
                    "fixora_application_id", String.valueOf(applicationId),
                    "fixora_application_alias", application.getAlias(),
                    "fixora_alert_config_id", String.valueOf(request.getAlertConfigurationId())
            ));

            Map<String, Object> response = keepClient.sendTestAlert(
                    "test-workflow-" + request.getAlertConfigurationId(), 
                    payload
            );
            
            TestAlertResponse result = new TestAlertResponse();
            result.setSuccess(true);
            result.setMessage("Test alert sent successfully");
            result.setAlertId(String.valueOf(request.getAlertConfigurationId()));
            result.setChannelId(String.valueOf(request.getNotificationChannelId()));
            result.setSentAt(LocalDateTime.now());
            result.setStatus("SENT");
            
            log.info("Successfully sent test alert");
            return result;
        } catch (Exception e) {
            log.error("Error sending test alert", e);
            
            TestAlertResponse result = new TestAlertResponse();
            result.setSuccess(false);
            result.setMessage("Failed to send test alert: " + e.getMessage());
            result.setSentAt(LocalDateTime.now());
            result.setStatus("FAILED");
            
            return result;
        }
    }

    /**
     * Fetches alerts belonging to the given application (via the fixora_application_id label
     * stamped at alert-creation time) and buckets them into "active" (firing/acknowledged) or
     * "closed" (resolved). Alerts sent before this labeling existed won't match anything.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getAlertsForApplication(Long applicationId, String bucket) {
        applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));

        Set<String> allowedStatuses = "closed".equalsIgnoreCase(bucket) ? CLOSED_STATUSES : ACTIVE_STATUSES;
        String applicationIdStr = String.valueOf(applicationId);

        List<Map<String, Object>> allAlerts = keepClient.getAlerts();
        List<Map<String, Object>> matched = new ArrayList<>();

        for (Map<String, Object> alert : allAlerts) {
            Object labelsObj = alert.get("labels");
            if (!(labelsObj instanceof Map<?, ?> labels)) {
                continue;
            }
            Object appIdLabel = labels.get("fixora_application_id");
            if (appIdLabel == null || !applicationIdStr.equals(String.valueOf(appIdLabel))) {
                continue;
            }
            String status = String.valueOf(alert.getOrDefault("status", "")).toLowerCase();
            if (allowedStatuses.contains(status)) {
                matched.add(alert);
                enrichWithRecentOccurrences(alert);
            }
        }

        return matched;
    }

    /**
     * firingCounter on the alert object is a lifetime count since the alert was first created —
     * an alert that fired 100 times over 20 days looks identical in that field to one that fired
     * 100 times in the last hour. Adds an "occurrencesLast24h" field computed from the alert's
     * actual history (each entry has its own timestamp) so the UI can show a time-bounded count
     * instead of a misleading all-time total. Only fetches history when firingCounter > 1, to
     * avoid an extra Keep API call for every single-fire alert.
     */
    private void enrichWithRecentOccurrences(Map<String, Object> alert) {
        Object firingCounterObj = alert.get("firingCounter");
        int firingCounter = firingCounterObj instanceof Number n ? n.intValue() : 0;
        if (firingCounter <= 1) {
            alert.put("occurrencesLast24h", firingCounter);
            return;
        }

        String fingerprint = String.valueOf(alert.get("fingerprint"));
        try {
            List<Map<String, Object>> history = keepClient.getAlertHistory(fingerprint);
            Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
            int unparseable = 0;
            long recentCount = 0;
            for (Map<String, Object> h : history) {
                Object raw = h.get("lastReceived");
                if (raw == null) {
                    continue;
                }
                String ts = String.valueOf(raw);
                try {
                    if (Instant.parse(ts).isAfter(cutoff)) {
                        recentCount++;
                    }
                } catch (Exception e) {
                    unparseable++;
                }
            }
            // Surfaced instead of silently dropping unparseable entries from the count -- a
            // silent skip here previously made occurrencesLast24h look like a real, trustworthy
            // number even when it was undercounting due to a timestamp format Keep changed or
            // that this code doesn't handle.
            if (unparseable > 0) {
                log.warn("Skipped {} unparseable lastReceived timestamp(s) while counting recent "
                                + "occurrences for fingerprint {} -- occurrencesLast24h may be undercounted",
                        unparseable, fingerprint);
            }
            alert.put("occurrencesLast24h", recentCount);
        } catch (Exception e) {
            log.warn("Failed to compute recent occurrence count for fingerprint {}", fingerprint, e);
            alert.put("occurrencesLast24h", null);
        }
    }

    public String createKeepWorkflowFromAlertConfig(Long alertConfigId, String alertName, String alertDescription,
                                                     String alertType, String severity, String channels) {
        return createKeepWorkflowFromAlertConfig(alertConfigId, alertName, alertDescription, alertType, severity, channels, false, null);
    }

    /**
     * Builds a Keep workflow "action" (not "step" — actions are the side-effecting stage in Keep's DSL)
     * that calls the fixora-rca-ai server (github.com/sid288791/fixora-rca-ai) with the triggering alert's data,
     * and enriches that alert with the response. host.docker.internal is required since this runs inside
     * the keep-backend container and needs to reach a process on the Docker host.
     */
    private Map<String, Object> buildAiInvestigationAction() {
        Map<String, Object> body = new HashMap<>();
        body.put("alert_name", "{{ alert.name }}");
        body.put("message", "{{ alert.message }}");
        body.put("severity", "{{ alert.severity }}");
        body.put("pipeline_name", "{{ alert.source }}");
        body.put("correlation_id", "{{ alert.fingerprint }}");

        Map<String, Object> with = new HashMap<>();
        with.put("url", "http://host.docker.internal:8090/investigate");
        with.put("method", "POST");
        with.put("body", body);
        with.put("enrich_alert", List.of(
                Map.of("key", "ai_root_cause", "value", "results.body.root_cause"),
                Map.of("key", "ai_report", "value", "results.body.report"),
                Map.of("key", "ai_based_on", "value", "results.body.based_on"),
                Map.of("key", "ai_recommended_actions", "value", "results.body.recommended_actions"),
                Map.of("key", "ai_investigated_at", "value", "results.body.investigated_at")
        ));

        Map<String, Object> provider = new HashMap<>();
        provider.put("type", "http");
        provider.put("config", "{{ providers.default-http }}");
        provider.put("with", with);

        Map<String, Object> action = new HashMap<>();
        action.put("name", "ai-investigation");
        action.put("provider", provider);
        return action;
    }

    /**
     * Builds a Keep workflow "action" that pages the on-call engineer via GoAlert's generic
     * alert-ingestion API (see docs/goalert-setup.md). goalertServiceUrl is the full webhook URL
     * including the per-service integration key/token
     * (https://goalert.example.com/api/v2/generic/incoming?token=...), stored on the
     * AlertConfiguration. GoAlert itself resolves the on-call schedule and calls/SMS's via its
     * built-in Twilio integration — Fixora/Keep never talks to Twilio directly.
     */
    private Map<String, Object> buildGoAlertEscalationAction(String goalertServiceUrl) {
        Map<String, Object> body = new HashMap<>();
        body.put("summary", "{{ alert.name }}");
        body.put("details", org.simulynx.fixora.util.GoAlertFingerprintTag.appendTo("{{ alert.message }}", "{{ alert.fingerprint }}"));

        Map<String, Object> with = new HashMap<>();
        with.put("url", goalertServiceUrl);
        with.put("method", "POST");
        with.put("body", body);

        Map<String, Object> provider = new HashMap<>();
        provider.put("type", "http");
        provider.put("config", "{{ providers.default-http }}");
        provider.put("with", with);

        Map<String, Object> action = new HashMap<>();
        action.put("name", "goalert-escalation");
        action.put("provider", provider);
        return action;
    }

    private Map<String, Object> buildWorkflowDefinition(Long alertConfigId, String alertName, String alertDescription,
                                                          String alertType, String severity, String channels,
                                                          boolean triggerAiInvestigation, String goalertServiceUrl) {
        Map<String, Object> workflowDefinition = new HashMap<>();
        workflowDefinition.put("name", "fixora-alert-" + alertConfigId + "-" + alertName.toLowerCase().replace(" ", "-"));
        workflowDefinition.put("description", alertDescription != null ? alertDescription : "Workflow for alert: " + alertName);

        Map<String, Object> triggers = new HashMap<>();
        triggers.put("type", "alert");
        // Keep's alert-trigger evaluation only ever reads "filters" or "cel" on a trigger
        // (confirmed against Keep's own workflowmanager.py) -- a "config" key here, which this
        // used to set, is silently ignored. Without filters/cel, Keep treats the trigger as
        // always-matching and runs this workflow on EVERY alert event system-wide, not just this
        // config's own alerts. Scope it to only this alert config's own alerts via the
        // fixora_alert_config_id label stamped on every alert we send (see sendTestAlert).
        //
        // status == "firing" matters just as much as the label scoping: without it, Keep also
        // runs this workflow on the "resolved" event that GoAlertSyncScheduler/closeAlert send
        // back to Keep when an alert is closed (see resolveInKeep) -- which re-pages GoAlert for
        // an alert that was just closed, live-tested and confirmed against Keep 2026-08-10 (an
        // "alert.status" field reference silently never matches; the event's status is a
        // top-level "status" field in the trigger's CEL context, confirmed the same way).
        triggers.put("cel", "labels.fixora_alert_config_id == \"" + alertConfigId + "\" && status == \"firing\"");
        workflowDefinition.put("triggers", List.of(triggers));

        // Notification channels are stored in the alert_config DB table for future use
        // when real notification providers (Slack, Teams, etc.) are configured in Keep.
        // No step is added here to avoid blocking the AI investigation action with a
        // placeholder HTTP call that would fail on a dummy URL.

        List<Map<String, Object>> actions = new ArrayList<>();
        // GoAlert escalation goes first — it's a fast webhook call that pages a human immediately.
        // The AI investigation below can take 3-5 minutes; if it ran first, critical-severity
        // paging would be delayed by that long. Both still execute within the same workflow run,
        // so from the on-call engineer's perspective escalation and AI investigation happen in
        // parallel rather than AI blocking the page.
        if ("critical".equalsIgnoreCase(severity) && goalertServiceUrl != null && !goalertServiceUrl.isBlank()) {
            actions.add(buildGoAlertEscalationAction(goalertServiceUrl));
        }
        if (triggerAiInvestigation) {
            actions.add(buildAiInvestigationAction());
        }
        if (!actions.isEmpty()) {
            workflowDefinition.put("actions", actions);
        }

        return workflowDefinition;
    }

    public String createKeepWorkflowFromAlertConfig(Long alertConfigId, String alertName, String alertDescription,
                                                     String alertType, String severity, String channels,
                                                     boolean triggerAiInvestigation, String goalertServiceUrl) {
        try {
            log.info("Creating Keep workflow for alert configuration {}", alertConfigId);

            Map<String, Object> workflowDefinition = buildWorkflowDefinition(
                    alertConfigId, alertName, alertDescription, alertType, severity, channels,
                    triggerAiInvestigation, goalertServiceUrl);

            Map<String, Object> result = keepClient.createWorkflow(workflowDefinition);

            String keepWorkflowId = result != null && result.get("workflow_id") != null ?
                result.get("workflow_id").toString() : null;

            log.info("Successfully created Keep workflow with id: {}", keepWorkflowId);
            return keepWorkflowId;
        } catch (Exception e) {
            log.error("Error creating Keep workflow for alert configuration {}", alertConfigId, e);
            throw new RuntimeException("Failed to create Keep workflow for alert configuration", e);
        }
    }

    /**
     * Regenerates and pushes the Keep workflow definition for an alert config that already has one
     * (identified by keepWorkflowId). This is what makes toggling "Trigger AI Investigation" on an
     * *existing* config actually take effect — without this, the AI action is only ever added at
     * creation time and flipping the toggle later on an existing config silently does nothing.
     */
    public void updateKeepWorkflowFromAlertConfig(String keepWorkflowId, Long alertConfigId, String alertName,
                                                   String alertDescription, String alertType, String severity,
                                                   String channels, boolean triggerAiInvestigation, String goalertServiceUrl) {
        try {
            log.info("Updating Keep workflow {} for alert configuration {}", keepWorkflowId, alertConfigId);

            Map<String, Object> workflowDefinition = buildWorkflowDefinition(
                    alertConfigId, alertName, alertDescription, alertType, severity, channels,
                    triggerAiInvestigation, goalertServiceUrl);

            keepClient.updateWorkflow(keepWorkflowId, workflowDefinition);

            log.info("Successfully updated Keep workflow {}", keepWorkflowId);
        } catch (Exception e) {
            log.error("Error updating Keep workflow {} for alert configuration {}", keepWorkflowId, alertConfigId, e);
            throw new RuntimeException("Failed to update Keep workflow for alert configuration", e);
        }
    }
}
