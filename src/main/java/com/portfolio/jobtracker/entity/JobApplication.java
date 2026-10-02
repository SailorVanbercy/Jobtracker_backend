package com.portfolio.jobtracker.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity 
@Table (name = "applications")
@Data 
public class JobApplication {
    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column (nullable = false)
    private String companyName;

    @Column (nullable = false)
    private String jobTitle;

    @Column (nullable = false)
    private String status;

    private Integer resumeMatchScore;
}
