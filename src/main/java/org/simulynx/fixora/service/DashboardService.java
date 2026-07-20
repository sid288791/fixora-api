package org.simulynx.fixora.service;

import org.simulynx.fixora.dto.ApplicationOnboardingDTO;
import org.simulynx.fixora.dto.DashboardDTO;
import org.simulynx.fixora.repository.ApplicationRepository;
import org.simulynx.fixora.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ApplicationService applicationService;
    
    public DashboardDTO getDashboard() {
        Long totalApplicationCount = applicationRepository.countAllApplications();
        Long activeApplicationCount = applicationRepository.countActiveApplications();
        Long uniqueUserCount = userRepository.countUniqueUsers();
        
        var registeredApplications = applicationRepository.findAll().stream()
            .map(app -> new ApplicationOnboardingDTO(
                app.getId(),
                app.getName(),
                app.getAlias(),
                app.getOwnerEmail(),
                app.getAdGroupMapping(),
                app.getDescription(),
                app.getStatus(),
                app.getCreatedAt(),
                app.getUpdatedAt()
            ))
            .collect(Collectors.toList());
        
        return new DashboardDTO(
            totalApplicationCount,
            activeApplicationCount,
            uniqueUserCount,
            registeredApplications
        );
    }
}
