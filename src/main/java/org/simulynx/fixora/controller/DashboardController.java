package org.simulynx.fixora.controller;

import org.simulynx.fixora.dto.DashboardDTO;
import org.simulynx.fixora.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboard() {
        DashboardDTO dashboard = dashboardService.getDashboard();
        return ResponseEntity.ok(dashboard);
    }
}
