package com.portfolio.jobtracker.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.portfolio.jobtracker.dto.AuthRequest;
import com.portfolio.jobtracker.dto.AuthResponse;
import com.portfolio.jobtracker.entity.Users;
import com.portfolio.jobtracker.repository.UserRepository;

@Service 
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository){
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public AuthResponse authenticate(AuthRequest request){
        // 1. Spring security vérifie l'email et le mdp crypté automatiquement
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        // 2. Si ça passe on récup le user
        Users user = userRepository.findByEmail(request.email()).orElseThrow();

        // 3. on génère et renvoie le jwt
        String jwtToken = jwtService.generateToken(user.getUsername());
        return new AuthResponse(jwtToken);
    }
}
