package com.portfolio.jobtracker.service;

import java.util.List;
import java.util.UUID;

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

    public List<JobApplicationResponse> getAllApplications() {
        return jobApplicationRepository.findAll()
        .stream()
        .map(JobApplicationResponse::fromEntity)
        .toList();
    }

    public JobApplicationResponse createApplication(JobApplicationRequest request){
        JobApplication entity = new JobApplication();

        // Transformation du DTO en entité
        entity.setCompanyName(request.companyName());
        entity.setJobTitle(request.jobTitle());
        entity.setStatus(request.status() != null ? request.status() : "Applied");
        if(request.jobDescription() != null && !request.jobDescription().isBlank()) {
            Users user = userRepository.findAll().stream().findFirst().orElseThrow(() -> new RessourceNotFoundException("Aucun utilisateur trouvé. Veuillez créer un profil et uploader un CV"));
            AiAnalysisResponse aiResult = aiAnalysisService.analyseJobDescription(request.jobDescription(), user.getResumeText());
            entity.setResumeMatchScore(aiResult.score());
            entity.setMissingSkills(aiResult.missingSkills());
        } else {
            entity.setResumeMatchScore(request.resumeMatchScore());
        }

        JobApplication savedEntity = jobApplicationRepository.save(entity);
        return JobApplicationResponse.fromEntity(savedEntity);
    }

    // Méthode pour récupérer une candidature par son ID
    public JobApplicationResponse getApplicationById(UUID id){
        JobApplication entity = jobApplicationRepository.findById(id)
        .orElseThrow(() ->new RessourceNotFoundException("Application not found with id: " + id));
        return JobApplicationResponse.fromEntity(entity);
    }

    public JobApplicationResponse updateApplication(UUID id, JobApplicationRequest request){
        JobApplication entity = jobApplicationRepository.findById(id).orElseThrow(
            () -> new RessourceNotFoundException("Application not found with id: " + id)
        );

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
        if(!jobApplicationRepository.existsById(id))
            throw new RessourceNotFoundException("Application not found with id: " + id);
        jobApplicationRepository.deleteById(id);
    }
}