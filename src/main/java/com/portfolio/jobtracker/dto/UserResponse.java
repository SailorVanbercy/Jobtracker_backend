package com.portfolio.jobtracker.dto;

import java.util.UUID;

import com.portfolio.jobtracker.entity.Users;

public record UserResponse(
    UUID id,
    String email,
    String resumeText
) {
    public static UserResponse fromEntity(Users user){
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getResumeText()
        );
    }
}
