package org.simulynx.fixora.integration.goalert;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Every method here throws (RestClientException for HTTP-level failures, IllegalStateException
 * for a login/GraphQL-level failure such as bad credentials or a GraphQL "errors" response)
 * rather than swallowing failures into an empty result — callers need a real GoAlert outage to
 * look different from "nothing matched", both in what gets logged and in whether a caller decides
 * to report a partial failure.
 */
public interface GoAlertClient {

    /**
     * Alerts on the given GoAlert service that were closed at or after {@code notClosedBefore}.
     * Each returned map has at least "id" (GoAlert's GraphQL alert id), "status", "summary", and
     * "details" — the last of which carries the Keep fingerprint tag GoAlertSyncScheduler parses.
     */
    List<Map<String, Object>> getRecentlyClosedAlerts(String serviceId, Instant notClosedBefore);

    /**
     * Closes every still-open (unacknowledged or acknowledged) alert on the given GoAlert service
     * whose "details" text carries the given Keep fingerprint tag. Used the opposite direction
     * from getRecentlyClosedAlerts: when an alert is closed from the Fixora side, this stops
     * GoAlert's escalation policy from continuing to page/SMS someone for it. Returns the number
     * of GoAlert alerts closed (0 if none matched — not necessarily a problem, since the alert may
     * already be closed or was never paged, e.g. a non-critical severity).
     */
    int closeAlertsByFingerprint(String serviceId, String fingerprint);
}
