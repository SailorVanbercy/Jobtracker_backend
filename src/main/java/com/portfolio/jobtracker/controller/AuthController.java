package com.portfolio.jobtracker.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.jobtracker.dto.AuthRequest;
import com.portfolio.jobtracker.dto.AuthResponse;
import com.portfolio.jobtracker.service.AuthenticationService;


@RestController 
@RequestMapping ("/api/auth")
@CrossOrigin (origins = "*")
public class AuthController {
    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService){
        this.authenticationService = authenticationService;
    }

    @PostMapping ("/login")
    public AuthResponse login(@RequestBody AuthRequest request){
        return authenticationService.authenticate(request);
    }
}
