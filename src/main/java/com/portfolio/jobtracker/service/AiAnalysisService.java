package com.portfolio.jobtracker.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.portfolio.jobtracker.dto.AiAnalysisResponse;

@Service 
public class AiAnalysisService {
    ChatClient chatClient;

    // Ton CV codé en dur pour l'instant (nous pourrons le stocker en base ou dans un fichier plus tard)
    private final String myResume = """
        Développeur Full-Stack avec de l'expérience en Java, Spring Boot, React, Next.js.
        Bases de données : PostgreSQL, MySQL.
        Outils : Git, Docker, GitHub Actions, AWS.
        """;

    public AiAnalysisService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public AiAnalysisResponse analyseJobDescription(String jobDescription, String userResume ){
        String prompt = """
                Tu es un Tech Lead et recruteur expert. 
            Compare le CV suivant avec la description de l'offre d'emploi.
            Donne un score de correspondance sur 100 (uniquement le nombre) et liste un maximum de 5 compétences clés manquantes.
            
            CV : {resume}
            
            Offre d'emploi : {job}
                """;

        return chatClient.prompt()
        .user(u -> u.text(prompt)
                    .param("resume", userResume)
                    .param("job", jobDescription))
        .call()
        .entity(AiAnalysisResponse.class); // mappage automatique de la réponse JSON à notre DTO AiAnalysisResponse
    }
}
