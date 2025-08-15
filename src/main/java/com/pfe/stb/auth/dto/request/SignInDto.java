package com.pfe.stb.auth.dto.request;

import com.pfe.stb.shared.validators.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignInDto(
    @Email(message = "Invalid email address")
        @NotNull(message = "Email is required")
        @NotBlank(message = "Email is required")
        String email,
    @NotNull(message = "Password is required")
        @NotBlank(message = "Password is required")
        @ValidPassword
        String password) {}
