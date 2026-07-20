package org.simulynx.fixora.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private Long totalApplicationCount;
    private Long activeApplicationCount;
    private Long uniqueUserCount;
    private List<ApplicationOnboardingDTO> registeredApplications;
}
