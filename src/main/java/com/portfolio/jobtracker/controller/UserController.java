package com.portfolio.jobtracker.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.portfolio.jobtracker.dto.UserRequest;
import com.portfolio.jobtracker.dto.UserResponse;
import com.portfolio.jobtracker.entity.Users;
import com.portfolio.jobtracker.exception.RessourceNotFoundException;
import com.portfolio.jobtracker.repository.UserRepository;
import com.portfolio.jobtracker.service.PdfService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping ("/api/users")
@CrossOrigin (origins = "*")
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

    @PostMapping(value ="/{userId}/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadResume(@PathVariable UUID userId, @RequestParam ("file") MultipartFile file){
        // Extraire le texte du pdf
        String extractedText = pdfService.extractTextFromPdf(file);

        // Récupérer l'utilisateur par son ID
        Users user = userRepository.findById(userId).orElseThrow(() -> new RessourceNotFoundException("User not found with id :" + userId));

        user.setResumeText(extractedText);
        userRepository.save(user);
        return "Resume uploaded and text extracted successfully for user with ID: " + userId;
    }
}
