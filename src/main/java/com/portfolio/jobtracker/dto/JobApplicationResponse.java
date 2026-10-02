package com.portfolio.jobtracker.dto;

import java.util.UUID;

import com.portfolio.jobtracker.entity.JobApplication;

public record JobApplicationResponse(
    UUID id,
    String companyName,
    String jobTitle,
    String status,
    Integer resumeMatchScore
) {
    // constructeur pour convertir une entité en dto
    public static JobApplicationResponse fromEntity(JobApplication jobApplication) {
        return new JobApplicationResponse(
            jobApplication.getId(),
            jobApplication.getCompanyName(),
            jobApplication.getJobTitle(),
            jobApplication.getStatus(),
            jobApplication.getResumeMatchScore()
        );
    }
}
