package com.portfolio.jobtracker.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.portfolio.jobtracker.dto.AiAnalysisResponse;
import com.portfolio.jobtracker.dto.JobApplicationRequest;
import com.portfolio.jobtracker.dto.JobApplicationResponse;
import com.portfolio.jobtracker.entity.JobApplication;
import com.portfolio.jobtracker.entity.Users;
import com.portfolio.jobtracker.exception.RessourceNotFoundException;
import com.portfolio.jobtracker.repository.JobApplicationRepository;
import com.portfolio.jobtracker.repository.UserRepository;

@Service 
public class JobApplicationService {
    private final JobApplicationRepository jobApplicationRepository;
    private final AiAnalysisService aiAnalysisService;
    private final UserRepository userRepository;

    // Injection de dépendance via le constructeur
    public JobApplicationService(JobApplicationRepository jobApplicationRepository, AiAnalysisService aiAnalysisService, UserRepository userRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.aiAnalysisService = aiAnalysisService;
        this.userRepository = userRepository;
    }

    // Only the applications owned by the authenticated user
    public List<JobApplicationResponse> getAllApplications() {
        return jobApplicationRepository.findAllByUser(getCurrentUser())
        .stream()
        .map(JobApplicationResponse::fromEntity)
        .toList();
    }

    public JobApplicationResponse createApplication(JobApplicationRequest request){
        Users currentUser = getCurrentUser();
        JobApplication entity = new JobApplication();

        // Transformation du DTO en entité
        entity.setUser(currentUser);
        entity.setCompanyName(request.companyName());
        entity.setJobTitle(request.jobTitle());
        entity.setStatus(request.status() != null ? request.status() : "APPLIED");
        entity.setJobDescription(request.jobDescription());
        if(request.jobDescription() != null && !request.jobDescription().isBlank()) {
            AiAnalysisResponse aiResult = aiAnalysisService.analyseJobDescription(request.jobDescription(), currentUser.getResumeText());
            entity.setResumeMatchScore(aiResult.score());
            entity.setMissingSkills(aiResult.missingSkills());
        } else {
            entity.setResumeMatchScore(request.resumeMatchScore());
        }

        JobApplication savedEntity = jobApplicationRepository.save(entity);
        return JobApplicationResponse.fromEntity(savedEntity);
    }

    // Méthode de modification du statut
    public JobApplicationResponse updateStatus(UUID id, String status){
        JobApplication entity = findOwnedApplication(id);
        entity.setStatus(status);
        return JobApplicationResponse.fromEntity(jobApplicationRepository.save(entity));
    }

    // Méthode pour récupérer une candidature par son ID
    public JobApplicationResponse getApplicationById(UUID id){
        return JobApplicationResponse.fromEntity(findOwnedApplication(id));
    }

    public JobApplicationResponse updateApplication(UUID id, JobApplicationRequest request){
        JobApplication entity = findOwnedApplication(id);

        // Mise à jour des champs de l'entité avec les valeurs du DTO
        entity.setCompanyName(request.companyName());
        entity.setJobTitle(request.jobTitle());

        if(request.status() != null){
            entity.setStatus(request.status());
        }

        if(request.resumeMatchScore() != null){
            entity.setResumeMatchScore(request.resumeMatchScore());
        }

        JobApplication updatedEntity = jobApplicationRepository.save(entity);
        return JobApplicationResponse.fromEntity(updatedEntity);
    }

    // Méthode pour supprimer une candidature par son ID
    public void deleteApplication(UUID id){
        jobApplicationRepository.delete(findOwnedApplication(id));
    }

    // Another user's application is reported as not found to avoid leaking its existence
    private JobApplication findOwnedApplication(UUID id){
        return jobApplicationRepository.findByIdAndUser(id, getCurrentUser())
            .orElseThrow(() -> new RessourceNotFoundException("Application not found with id: " + id));
    }

    private Users getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        return userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new RessourceNotFoundException("Aucun utilisateur trouvé"));
    }
}
