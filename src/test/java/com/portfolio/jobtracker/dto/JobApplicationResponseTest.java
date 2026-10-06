package com.portfolio.jobtracker.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.portfolio.jobtracker.entity.JobApplication;

class JobApplicationResponseTest {

    @Test
    void fromEntity_mapsAllFieldsIncludingCreatedAtAndJobDescription() {
        JobApplication entity = new JobApplication();
        entity.setId(UUID.randomUUID());
        entity.setCompanyName("Acme");
        entity.setJobTitle("Backend Engineer");
        entity.setStatus("APPLIED");
        entity.setResumeMatchScore(75);
        entity.setMissingSkills(List.of("Kafka"));
        entity.setCreatedAt(Instant.parse("2026-10-06T10:15:30Z"));
        entity.setJobDescription("Build resilient APIs with Spring Boot.");

        JobApplicationResponse response = JobApplicationResponse.fromEntity(entity);

        assertThat(response.id()).isEqualTo(entity.getId());
        assertThat(response.companyName()).isEqualTo("Acme");
        assertThat(response.jobTitle()).isEqualTo("Backend Engineer");
        assertThat(response.status()).isEqualTo("APPLIED");
        assertThat(response.resumeMatchScore()).isEqualTo(75);
        assertThat(response.missingSkills()).containsExactly("Kafka");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-10-06T10:15:30Z"));
        assertThat(response.jobDescription()).isEqualTo("Build resilient APIs with Spring Boot.");
    }
}
