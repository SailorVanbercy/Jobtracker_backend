package com.portfolio.jobtracker.dto;

import java.util.UUID;

import com.portfolio.jobtracker.entity.Users;

// Public profile of the authenticated user: never exposes resumeText nor passwordHash
public record UserProfileResponse(
    UUID id,
    String email,
    boolean hasResume
) {
    public static UserProfileResponse fromEntity(Users user){
        String resumeText = user.getResumeText();
        return new UserProfileResponse(
            user.getId(),
            user.getEmail(),
            resumeText != null && !resumeText.isBlank()
        );
    }
}
