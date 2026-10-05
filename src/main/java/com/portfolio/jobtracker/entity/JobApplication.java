package com.portfolio.jobtracker.entity;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity 
@Table (name = "applications")
@Setter
@Getter 
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

    @JdbcTypeCode (SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> missingSkills;
}
