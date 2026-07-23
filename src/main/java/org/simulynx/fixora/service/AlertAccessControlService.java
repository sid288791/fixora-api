package org.simulynx.fixora.service;

import lombok.extern.slf4j.Slf4j;
import org.simulynx.fixora.dto.ApplicationOnboardingDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enforces that only users belonging to an application's configured AD group(s)
 * may view or modify its alert & notification configurations.
 */
@Slf4j
@Service
public class AlertAccessControlService {

    @Autowired
    private ApplicationService applicationService;

    /**
     * Verifies that the given user AD group is a member of the application's
     * configured ad-group mapping. Throws 403 Forbidden if not authorized,
     * or 400 Bad Request if no group header was supplied.
     */
    public void assertAuthorized(Long applicationId, String userAdGroup) {
        if (userAdGroup == null || userAdGroup.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "X-AD-Group header is required to access alert configurations");
        }

        ApplicationOnboardingDTO application = applicationService.getApplicationById(applicationId);
        Set<String> allowedGroups = Arrays.stream(application.getAdGroupMapping().split(","))
                .map(String::trim)
                .filter(g -> !g.isEmpty())
                .collect(Collectors.toSet());

        if (!allowedGroups.contains(userAdGroup.trim())) {
            log.warn("Access denied: user group '{}' is not in allowed groups {} for application {}",
                    userAdGroup, allowedGroups, applicationId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not belong to the AD group configured for this application's alerts");
        }
    }
}
