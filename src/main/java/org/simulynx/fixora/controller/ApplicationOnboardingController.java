package org.simulynx.fixora.controller;

import org.simulynx.fixora.dto.ApplicationOnboardingDTO;
import org.simulynx.fixora.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/applications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ApplicationOnboardingController {
    
    @Autowired
    private ApplicationService applicationService;
    
    @PostMapping("/onboard")
    public ResponseEntity<ApplicationOnboardingDTO> onboardApplication(
            @Valid @RequestBody ApplicationOnboardingDTO dto) {
        ApplicationOnboardingDTO createdApp = applicationService.onboardApplication(dto);
        return ResponseEntity.ok(createdApp);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationOnboardingDTO> getApplicationById(@PathVariable Long id) {
        ApplicationOnboardingDTO app = applicationService.getApplicationById(id);
        return ResponseEntity.ok(app);
    }
    
    @GetMapping("/alias/{alias}")
    public ResponseEntity<ApplicationOnboardingDTO> getApplicationByAlias(@PathVariable String alias) {
        ApplicationOnboardingDTO app = applicationService.getApplicationByAlias(alias);
        return ResponseEntity.ok(app);
    }
    
    @GetMapping
    public ResponseEntity<List<ApplicationOnboardingDTO>> getAllApplications() {
        List<ApplicationOnboardingDTO> apps = applicationService.getAllApplications();
        return ResponseEntity.ok(apps);
    }
    
    @GetMapping("/active/list")
    public ResponseEntity<List<ApplicationOnboardingDTO>> getActiveApplications() {
        List<ApplicationOnboardingDTO> apps = applicationService.getActiveApplications();
        return ResponseEntity.ok(apps);
    }
    
    @GetMapping("/ad-group/{adGroup}")
    public ResponseEntity<List<ApplicationOnboardingDTO>> getApplicationsByAdGroup(@PathVariable String adGroup) {
        List<ApplicationOnboardingDTO> apps = applicationService.getApplicationsByAdGroup(adGroup);
        return ResponseEntity.ok(apps);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApplicationOnboardingDTO> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationOnboardingDTO dto) {
        ApplicationOnboardingDTO updatedApp = applicationService.updateApplication(id, dto);
        return ResponseEntity.ok(updatedApp);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}
