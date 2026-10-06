package com.portfolio.jobtracker.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.portfolio.jobtracker.entity.JobApplication;
import com.portfolio.jobtracker.entity.Users;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    List<JobApplication> findAllByUser(Users user);

    Optional<JobApplication> findByIdAndUser(UUID id, Users user);
}
