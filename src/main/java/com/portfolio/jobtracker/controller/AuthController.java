package com.portfolio.jobtracker.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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
public class AuthController {
    private static final String JWT_COOKIE_NAME = "jwt_token";
    private static final int EXPIRED_COOKIE_MAX_AGE = 0;
    private static final long MILLIS_PER_SECOND = 1000L;

    private final AuthenticationService authenticationService;
    private final boolean cookieSecure;
    private final int cookieMaxAgeSeconds;

    public AuthController(
            AuthenticationService authenticationService,
            @Value("${security.jwt.cookie-secure}") boolean cookieSecure,
            @Value("${security.jwt.expiration-time}") long jwtExpirationMillis){
        this.authenticationService = authenticationService;
        this.cookieSecure = cookieSecure;
        // Cookie lifetime follows the JWT lifetime
        this.cookieMaxAgeSeconds = (int) (jwtExpirationMillis / MILLIS_PER_SECOND);
    }

    @PostMapping ("/login")
    public AuthResponse login(@RequestBody AuthRequest request, HttpServletResponse response){
        // 1. On génère le token grace au service
        AuthResponse authResponse = authenticationService.authenticate(request);

        // 2. On crée le Cookie HttpOnly et on l'attache à la réponse
        response.addCookie(buildJwtCookie(authResponse.token(), cookieMaxAgeSeconds));
        return authResponse;
    }

    @PostMapping ("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response){
        // Expire the JWT cookie with the same attributes as the login cookie
        response.addCookie(buildJwtCookie("", EXPIRED_COOKIE_MAX_AGE));
        return ResponseEntity.noContent().build();
    }

    private Cookie buildJwtCookie(String value, int maxAgeSeconds){
        Cookie cookie = new Cookie(JWT_COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure); // true in production (HTTPS), see JWT_COOKIE_SECURE
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        return cookie;
    }
}
