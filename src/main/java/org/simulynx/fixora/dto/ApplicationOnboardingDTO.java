package org.simulynx.fixora.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationOnboardingDTO {
    
    private Long id;
    
    @NotBlank(message = "Application name is required")
    private String name;
    
    @NotBlank(message = "Alias is required")
    private String alias;
    
    @NotBlank(message = "Owner email is required")
    @Email(message = "Owner email should be valid")
    private String ownerEmail;
    
    @NotBlank(message = "AD Group Mapping is required")
    private String adGroupMapping;
    
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
