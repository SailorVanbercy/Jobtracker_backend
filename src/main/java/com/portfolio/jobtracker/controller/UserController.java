package com.portfolio.jobtracker.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.portfolio.jobtracker.dto.UserProfileResponse;
import com.portfolio.jobtracker.dto.UserRequest;
import com.portfolio.jobtracker.dto.UserResponse;
import com.portfolio.jobtracker.entity.Users;
import com.portfolio.jobtracker.exception.RessourceNotFoundException;
import com.portfolio.jobtracker.repository.UserRepository;
import com.portfolio.jobtracker.service.PdfService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping ("/api/users")
public class UserController {
    private final PdfService pdfService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(PdfService pdfService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.pdfService = pdfService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping 
    public UserResponse createUser(@Valid @RequestBody UserRequest request) {
        Users user = new Users();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.passwordHash()));

        Users savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    @GetMapping("/me")
    public UserProfileResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        Users user = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new RessourceNotFoundException("Utilisateur non trouvé avec l'email : " + currentUserEmail));

        return UserProfileResponse.fromEntity(user);
    }

    @PostMapping(value ="/me/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public String uploadResume(@RequestParam("file") MultipartFile file) {
    // 1. Récupérer l'utilisateur connecté via le token JWT
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUserEmail = authentication.getName();

    // 2. Extraire le texte du pdf
    String extractedText = pdfService.extractTextFromPdf(file);

    // 3. Récupérer l'utilisateur par son email
    Users user = userRepository.findByEmail(currentUserEmail)
        .orElseThrow(() -> new RessourceNotFoundException("Utilisateur non trouvé avec l'email : " + currentUserEmail));

    // 4. Sauvegarder le CV
    user.setResumeText(extractedText);
    userRepository.save(user);
    
    return "CV sauvegardé et extrait avec succès !";
}
}
