package com.portfolio.jobtracker.dto;

import jakarta.validation.constraints.NotBlank;

public record UserRequest(
    @NotBlank(message = "email is required")
    String email,
    @NotBlank (message = "password is required")
    String passwordHash
) {

}
