package org.simulynx.fixora.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GoAlert's generic incoming API returns no response body (204 No Content) and exposes no
 * free-form metadata/dedup field over GraphQL, so a Keep alert's fingerprint has nowhere else to
 * ride along through GoAlert and back except inside the alert's own "details" text. This is the
 * single source of truth for that tag's format, shared by:
 *   - KeepIntegrationService, which stamps it into "details" when paging GoAlert
 *   - GoAlertSyncScheduler / GoAlertClientImpl, which parse it back out to sync closures either
 *     direction between GoAlert and Keep
 */
public final class GoAlertFingerprintTag {

    private static final String PREFIX = "fixora-fingerprint:";
    private static final Pattern PATTERN = Pattern.compile(Pattern.quote(PREFIX) + "\\s*(\\S+)");

    private GoAlertFingerprintTag() {
    }

    public static String appendTo(String details, String fingerprintExpression) {
        return details + "\n\n" + PREFIX + " " + fingerprintExpression;
    }

    /**
     * Extracts the fingerprint from a GoAlert alert's "details" text, or null if the tag isn't
     * present (e.g. an alert GoAlert received from something other than Fixora/Keep).
     */
    public static String extract(String details) {
        if (details == null) {
            return null;
        }
        Matcher matcher = PATTERN.matcher(details);
        return matcher.find() ? matcher.group(1) : null;
    }
}
