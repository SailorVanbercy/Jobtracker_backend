package com.portfolio.jobtracker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record JobApplicationRequest(
    @NotBlank (message = "Company name is required")
    String companyName,

    @NotBlank (message = "Job title is required")
    String jobTitle,

    String status,
    
    @Min (value = 0, message = "Resume match score must be a positive integer")
    @Max (value = 100, message = "Resume match score must be less than or equal to 100")
    Integer resumeMatchScore
) {}
