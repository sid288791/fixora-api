package org.simulynx.fixora.integration.goalert;

import lombok.extern.slf4j.Slf4j;
import org.simulynx.fixora.util.GoAlertFingerprintTag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST/GraphQL client for GoAlert (https://github.com/target/goalert), used solely to poll for
 * alerts closed on GoAlert's side so their status can be mirrored back into Keep — see
 * GoAlertSyncScheduler. GoAlert's generic incoming API (used the other direction, to page
 * someone) is a one-way fire-and-forget webhook with no response body, so this reverse direction
 * needs its own authenticated client against GoAlert's GraphQL API.
 *
 * Every method here lets RestClientException/IllegalStateException propagate rather than
 * swallowing them into an empty result — a real GoAlert outage needs to look different from "no
 * alerts matched", both in logs and to callers that decide whether to report a partial failure.
 */
@Slf4j
@Service
public class GoAlertClientImpl implements GoAlertClient {

    // Bounds how many pages of alerts a single call will walk (500 alerts at 50/page). GoAlert
    // services used by Fixora aren't expected to ever carry anywhere near that many open/recently
    // -closed alerts at once; this is a safety cap against an unbounded loop, not a expected limit.
    private static final int MAX_PAGES = 10;
    private static final int PAGE_SIZE = 50;

    private final RestTemplate restTemplate;
    private final String goalertUrl;
    private final String adminUser;
    private final String adminPass;

    public GoAlertClientImpl(RestTemplate restTemplate,
                              @Value("${goalert.api.url:}") String goalertUrl,
                              @Value("${goalert.admin.user:}") String adminUser,
                              @Value("${goalert.admin.pass:}") String adminPass) {
        this.restTemplate = restTemplate;
        this.goalertUrl = goalertUrl;
        this.adminUser = adminUser;
        this.adminPass = adminPass;
    }

    /**
     * Logs in fresh on every call rather than caching a session token. This poll runs at most
     * once every few seconds (see GoAlertSyncScheduler), so the extra login round-trip is cheap,
     * and it sidesteps having to detect and react to session-token expiry mid-poll.
     */
    private String login() {
        String loginUrl = goalertUrl + "/api/v2/identity/providers/basic?noRedirect=1";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Referer", goalertUrl);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", adminUser);
        form.add("password", adminPass);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        String token = restTemplate.postForObject(loginUrl, request, String.class);

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("GoAlert login returned no session token — check goalert.admin.user/pass");
        }
        return token.trim();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> graphql(String token, String query, Map<String, Object> variables) {
        String url = goalertUrl + "/api/graphql";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = Map.of("query", query, "variables", variables);
        HttpEntity<Object> request = new HttpEntity<>(body, headers);

        Map<?, ?> response = restTemplate.postForObject(url, request, Map.class);
        if (response == null) {
            throw new IllegalStateException("GoAlert GraphQL call returned no response body");
        }
        if (response.get("errors") != null) {
            throw new IllegalStateException("GoAlert GraphQL returned errors: " + response.get("errors"));
        }
        return (Map<String, Object>) response;
    }

    /**
     * Walks every page of alerts matching the given status filter on the given service (up to
     * MAX_PAGES), returning the combined node list. Both getRecentlyClosedAlerts and
     * closeAlertsByFingerprint need this same shape, just with a different status filter and
     * (for the closed-alerts case) a notClosedBefore bound.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchAllAlerts(String token, String statusFilterGraphql,
                                                        String serviceId, String notClosedBefore) {
        List<Map<String, Object>> allNodes = new ArrayList<>();
        String afterCursor = null;

        for (int page = 0; page < MAX_PAGES; page++) {
            StringBuilder query = new StringBuilder("query($serviceID: ID!, $after: String");
            if (notClosedBefore != null) {
                query.append(", $notClosedBefore: ISOTimestamp!");
            }
            query.append(") { alerts(input: { filterByServiceID: [$serviceID], ")
                    .append("filterByStatus: [").append(statusFilterGraphql).append("], ")
                    .append("first: ").append(PAGE_SIZE).append(", after: $after");
            if (notClosedBefore != null) {
                query.append(", notClosedBefore: $notClosedBefore");
            }
            query.append(" }) { nodes { id alertID status details } pageInfo { hasNextPage endCursor } } }");

            Map<String, Object> variables = new HashMap<>();
            variables.put("serviceID", serviceId);
            variables.put("after", afterCursor);
            if (notClosedBefore != null) {
                variables.put("notClosedBefore", notClosedBefore);
            }

            Map<String, Object> response = graphql(token, query.toString(), variables);

            Object dataObj = response.get("data");
            if (!(dataObj instanceof Map<?, ?> data) || !(data.get("alerts") instanceof Map<?, ?> alerts)) {
                break;
            }
            if (alerts.get("nodes") instanceof List<?> nodes) {
                for (Object node : nodes) {
                    allNodes.add((Map<String, Object>) node);
                }
            }

            Object pageInfoObj = alerts.get("pageInfo");
            boolean hasNextPage = pageInfoObj instanceof Map<?, ?> pageInfo
                    && Boolean.TRUE.equals(pageInfo.get("hasNextPage"));
            if (!hasNextPage) {
                break;
            }
            afterCursor = pageInfoObj instanceof Map<?, ?> pageInfo
                    ? String.valueOf(pageInfo.get("endCursor")) : null;
            if (afterCursor == null) {
                break;
            }
        }

        return allNodes;
    }

    @Override
    public List<Map<String, Object>> getRecentlyClosedAlerts(String serviceId, Instant notClosedBefore) {
        String token = login();
        return fetchAllAlerts(token, "StatusClosed", serviceId, DateTimeFormatter.ISO_INSTANT.format(notClosedBefore));
    }

    @Override
    public int closeAlertsByFingerprint(String serviceId, String fingerprint) {
        String token = login();
        List<Map<String, Object>> openAlerts =
                fetchAllAlerts(token, "StatusUnacknowledged, StatusAcknowledged", serviceId, null);

        List<Integer> matchingAlertIds = new ArrayList<>();
        for (Map<String, Object> alert : openAlerts) {
            String details = String.valueOf(alert.getOrDefault("details", ""));
            if (fingerprint.equals(GoAlertFingerprintTag.extract(details))
                    && alert.get("alertID") instanceof Number n) {
                matchingAlertIds.add(n.intValue());
            }
        }

        if (matchingAlertIds.isEmpty()) {
            return 0;
        }

        String closeMutation = "mutation($alertIDs: [Int!]!) { "
                + "updateAlerts(input: { alertIDs: $alertIDs, newStatus: StatusClosed }) { id } }";
        graphql(token, closeMutation, Map.of("alertIDs", matchingAlertIds));

        return matchingAlertIds.size();
    }
}
