package org.simulynx.fixora.service;

import org.simulynx.fixora.dto.ApplicationOnboardingDTO;
import org.simulynx.fixora.entity.Application;
import org.simulynx.fixora.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    public ApplicationOnboardingDTO onboardApplication(ApplicationOnboardingDTO dto) {
        Application application = new Application();
        application.setName(dto.getName());
        application.setAlias(dto.getAlias());
        application.setOwnerEmail(dto.getOwnerEmail());
        application.setAdGroupMapping(dto.getAdGroupMapping());
        application.setDescription(dto.getDescription());
        application.setStatus("ACTIVE");
        
        Application savedApp = applicationRepository.save(application);
        return convertToDTO(savedApp);
    }
    
    public ApplicationOnboardingDTO getApplicationById(Long id) {
        Application app = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found with id: " + id));
        return convertToDTO(app);
    }
    
    public ApplicationOnboardingDTO getApplicationByAlias(String alias) {
        Application app = applicationRepository.findByAlias(alias)
            .orElseThrow(() -> new RuntimeException("Application not found with alias: " + alias));
        return convertToDTO(app);
    }
    
    public List<ApplicationOnboardingDTO> getAllApplications() {
        return applicationRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<ApplicationOnboardingDTO> getActiveApplications() {
        return applicationRepository.findByStatus("ACTIVE").stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<ApplicationOnboardingDTO> getApplicationsByAdGroup(String adGroup) {
        return applicationRepository.findByAdGroup(adGroup).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public ApplicationOnboardingDTO updateApplication(Long id, ApplicationOnboardingDTO dto) {
        Application app = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found with id: " + id));
        
        app.setName(dto.getName());
        app.setAlias(dto.getAlias());
        app.setOwnerEmail(dto.getOwnerEmail());
        app.setAdGroupMapping(dto.getAdGroupMapping());
        app.setDescription(dto.getDescription());
        if (dto.getStatus() != null) {
            app.setStatus(dto.getStatus());
        }
        
        Application updatedApp = applicationRepository.save(app);
        return convertToDTO(updatedApp);
    }
    
    public void deleteApplication(Long id) {
        applicationRepository.deleteById(id);
    }
    
    private ApplicationOnboardingDTO convertToDTO(Application app) {
        return new ApplicationOnboardingDTO(
            app.getId(),
            app.getIntegrationId(),
            app.getName(),
            app.getAlias(),
            app.getOwnerEmail(),
            app.getAdGroupMapping(),
            app.getDescription(),
            app.getStatus(),
            app.getCreatedAt(),
            app.getUpdatedAt()
        );
    }
}
