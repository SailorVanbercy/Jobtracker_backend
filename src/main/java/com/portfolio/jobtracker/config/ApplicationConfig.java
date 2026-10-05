package com.portfolio.jobtracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.portfolio.jobtracker.repository.UserRepository;

@Configuration 
public class ApplicationConfig {
    private final UserRepository userRepository;

    public ApplicationConfig(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Bean 
    public UserDetailsService userDetailsService(){
        return username -> userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Utilisateur Introuvale" + username));
    }
    @Bean 
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
        
    }
}
