package com.navio.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 50) String password,
        @NotBlank @Size(max = 50) String name,
        @Size(max = 20) String phone
) {}
