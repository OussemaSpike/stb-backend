package com.pfe.stb.auth.dto.request;

import com.pfe.stb.shared.validators.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangePasswordDto(
    String oldPassword,
    @NotNull(message = "Password is required")
        @NotBlank(message = "Password is required")
        @ValidPassword
        String newPassword,
    @NotNull(message = "Password is required")
        @NotBlank(message = "Password is required")
        @ValidPassword
        String confirmPassword) {}
