package com.portfolio.jobtracker.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.jobtracker.dto.JobApplicationRequest;
import com.portfolio.jobtracker.dto.JobApplicationResponse;
import com.portfolio.jobtracker.service.JobApplicationService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping ("/api/applications")
@CrossOrigin (origins = "*")
public class JobApplicationController {
    private final JobApplicationService service;

    //Injection de dépendance via le constructeur
    public JobApplicationController(JobApplicationService service){
        this.service = service;
    }

    //Endpoint de récupération de toutes les candidatures
    @GetMapping
    public List<JobApplicationResponse> getAllApplications() {
        return service.getAllApplications();
    }

    // Endpoint pour créer une nouvelle candidature
    @PostMapping 
    public JobApplicationResponse createApplication( @Valid @RequestBody JobApplicationRequest request){
        return service.createApplication(request);
    }

    // Endpoint pour récupérer une candidature par son ID
    @GetMapping ("/{id}")
    public JobApplicationResponse getApplicationById(@PathVariable UUID id){
        return service.getApplicationById(id);
    }

    // Endpoint pour mettre à jour une candidature existante
    @PostMapping ("/{id}")
    public JobApplicationResponse updateApplication(@PathVariable UUID id, @Valid @RequestBody JobApplicationRequest request){
        return service.updateApplication(id, request);
    }

    // Endpoint pour supprimer une candidature par son ID
    @DeleteMapping ("/{id}")
    public void deleteApplication(@PathVariable UUID id){
        service.deleteApplication(id);
    }
}
