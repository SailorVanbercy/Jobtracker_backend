package com.portfolio.jobtracker.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.jobtracker.dto.AuthRequest;
import com.portfolio.jobtracker.dto.AuthResponse;
import com.portfolio.jobtracker.service.AuthenticationService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;


@RestController 
@RequestMapping ("/api/auth")
@CrossOrigin (origins = "*")
public class AuthController {
    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService){
        this.authenticationService = authenticationService;
    }

    @PostMapping ("/login")
    public AuthResponse login(@RequestBody AuthRequest request, HttpServletResponse response){
        // 1. On génère le token grace au service
        AuthResponse authResponse = authenticationService.authenticate(request);
        
        // 2. On crée le Cookie HttpOnly
        Cookie jwtCookie = new Cookie("jwt_token", authResponse.token());
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // true en production avec HTTPS
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(24*60*60);

        // 3. On attache le cookie à la réponse
        response.addCookie(jwtCookie);
        return authResponse;
    }
}
