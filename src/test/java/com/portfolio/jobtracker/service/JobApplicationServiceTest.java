package com.portfolio.jobtracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.portfolio.jobtracker.dto.AiAnalysisResponse;
import com.portfolio.jobtracker.dto.JobApplicationRequest;
import com.portfolio.jobtracker.dto.JobApplicationResponse;
import com.portfolio.jobtracker.entity.JobApplication;
import com.portfolio.jobtracker.entity.Users;
import com.portfolio.jobtracker.exception.RessourceNotFoundException;
import com.portfolio.jobtracker.repository.JobApplicationRepository;
import com.portfolio.jobtracker.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    private static final String USER_EMAIL = "jane.doe@example.com";
    private static final String JOB_DESCRIPTION = "We are looking for a Java developer with Spring Boot experience.";
    private static final String RESUME_TEXT = "Java developer";
    private static final int AI_SCORE = 80;
    private static final int MANUAL_SCORE = 50;
    private static final Instant CREATED_AT = Instant.parse("2026-10-06T10:15:30Z");

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private AiAnalysisService aiAnalysisService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobApplicationService jobApplicationService;

    private Users currentUser;

    @BeforeEach
    void authenticateCurrentUser() {
        currentUser = new Users();
        currentUser.setId(UUID.randomUUID());
        currentUser.setEmail(USER_EMAIL);
        currentUser.setResumeText(RESUME_TEXT);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(USER_EMAIL, null, List.of()));
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(currentUser));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void stubSaveSimulatingPersistence() {
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> {
            JobApplication entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            entity.setCreatedAt(CREATED_AT);
            return entity;
        });
    }

    private JobApplication ownedApplication(UUID id) {
        JobApplication entity = new JobApplication();
        entity.setId(id);
        entity.setUser(currentUser);
        entity.setCompanyName("Acme");
        entity.setJobTitle("Backend Engineer");
        entity.setStatus("APPLIED");
        return entity;
    }

    private JobApplicationRequest validRequest() {
        return new JobApplicationRequest("Acme", "Backend Engineer", "INTERVIEW", MANUAL_SCORE, null);
    }

    // --- createApplication ---

    @Test
    void createApplication_persistsJobDescription_andReturnsItWithCreatedAt() {
        when(aiAnalysisService.analyseJobDescription(JOB_DESCRIPTION, RESUME_TEXT))
            .thenReturn(new AiAnalysisResponse(AI_SCORE, List.of("Kubernetes")));
        stubSaveSimulatingPersistence();

        JobApplicationResponse response = jobApplicationService.createApplication(
            new JobApplicationRequest("Acme", "Backend Engineer", null, null, JOB_DESCRIPTION));

        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(jobApplicationRepository).save(captor.capture());
        assertThat(captor.getValue().getJobDescription()).isEqualTo(JOB_DESCRIPTION);
        assertThat(response.jobDescription()).isEqualTo(JOB_DESCRIPTION);
        assertThat(response.createdAt()).isEqualTo(CREATED_AT);
        assertThat(response.resumeMatchScore()).isEqualTo(AI_SCORE);
    }

    @Test
    void createApplication_storesNullJobDescription_whenNotProvided() {
        stubSaveSimulatingPersistence();

        JobApplicationResponse response = jobApplicationService.createApplication(validRequest());

        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(jobApplicationRepository).save(captor.capture());
        assertThat(captor.getValue().getJobDescription()).isNull();
        assertThat(response.jobDescription()).isNull();
        assertThat(response.createdAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void createApplication_assignsAuthenticatedUserAsOwner() {
        stubSaveSimulatingPersistence();

        jobApplicationService.createApplication(validRequest());

        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(jobApplicationRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(currentUser);
    }

    // --- getAllApplications ---

    @Test
    void getAllApplications_returnsOnlyApplicationsOfAuthenticatedUser() {
        JobApplication owned = ownedApplication(UUID.randomUUID());
        when(jobApplicationRepository.findAllByUser(currentUser)).thenReturn(List.of(owned));

        List<JobApplicationResponse> responses = jobApplicationService.getAllApplications();

        assertThat(responses).extracting(JobApplicationResponse::id).containsExactly(owned.getId());
        verify(jobApplicationRepository, never()).findAll();
    }

    // --- operations by id: owner ---

    @Test
    void getApplicationById_returnsApplication_whenOwnedByUser() {
        UUID id = UUID.randomUUID();
        when(jobApplicationRepository.findByIdAndUser(id, currentUser)).thenReturn(Optional.of(ownedApplication(id)));

        assertThat(jobApplicationService.getApplicationById(id).id()).isEqualTo(id);
    }

    @Test
    void updateApplication_updatesFields_whenOwnedByUser() {
        UUID id = UUID.randomUUID();
        when(jobApplicationRepository.findByIdAndUser(id, currentUser)).thenReturn(Optional.of(ownedApplication(id)));
        stubSaveSimulatingPersistence();

        JobApplicationResponse response = jobApplicationService.updateApplication(id, validRequest());

        assertThat(response.status()).isEqualTo("INTERVIEW");
        assertThat(response.resumeMatchScore()).isEqualTo(MANUAL_SCORE);
    }

    @Test
    void updateStatus_updatesStatus_whenOwnedByUser() {
        UUID id = UUID.randomUUID();
        when(jobApplicationRepository.findByIdAndUser(id, currentUser)).thenReturn(Optional.of(ownedApplication(id)));
        stubSaveSimulatingPersistence();

        assertThat(jobApplicationService.updateStatus(id, "OFFER").status()).isEqualTo("OFFER");
    }

    @Test
    void deleteApplication_deletesEntity_whenOwnedByUser() {
        UUID id = UUID.randomUUID();
        JobApplication owned = ownedApplication(id);
        when(jobApplicationRepository.findByIdAndUser(id, currentUser)).thenReturn(Optional.of(owned));

        jobApplicationService.deleteApplication(id);

        verify(jobApplicationRepository).delete(owned);
    }

    // --- operations by id: application of another user (or missing) ---

    @Test
    void getApplicationById_throwsNotFound_whenNotOwnedByUser() {
        UUID id = UUID.randomUUID();
        when(jobApplicationRepository.findByIdAndUser(id, currentUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobApplicationService.getApplicationById(id))
            .isInstanceOf(RessourceNotFoundException.class);
        verify(jobApplicationRepository, never()).findById(any());
    }

    @Test
    void updateApplication_throwsNotFound_andDoesNotSave_whenNotOwnedByUser() {
        UUID id = UUID.randomUUID();
        when(jobApplicationRepository.findByIdAndUser(id, currentUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobApplicationService.updateApplication(id, validRequest()))
            .isInstanceOf(RessourceNotFoundException.class);
        verify(jobApplicationRepository, never()).save(any());
    }

    @Test
    void updateStatus_throwsNotFound_andDoesNotSave_whenNotOwnedByUser() {
        UUID id = UUID.randomUUID();
        when(jobApplicationRepository.findByIdAndUser(id, currentUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobApplicationService.updateStatus(id, "OFFER"))
            .isInstanceOf(RessourceNotFoundException.class);
        verify(jobApplicationRepository, never()).save(any());
    }

    @Test
    void deleteApplication_throwsNotFound_andDoesNotDelete_whenNotOwnedByUser() {
        UUID id = UUID.randomUUID();
        when(jobApplicationRepository.findByIdAndUser(id, currentUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobApplicationService.deleteApplication(id))
            .isInstanceOf(RessourceNotFoundException.class);
        verify(jobApplicationRepository, never()).delete(any());
        verify(jobApplicationRepository, never()).deleteById(any());
    }
}
