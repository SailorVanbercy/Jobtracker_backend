package com.portfolio.jobtracker.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.portfolio.jobtracker.entity.JobApplication;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

}
