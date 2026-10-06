package com.portfolio.jobtracker.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    // Owner of the application. Nullable at DB level only so that schema update
    // succeeds on legacy rows created before ownership existed (they stay invisible).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

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

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    // DB default backfills existing rows: Hibernate maps this column as NOT NULL
    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(updatable = false)
    private Instant createdAt;
}
