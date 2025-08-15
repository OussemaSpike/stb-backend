package com.pfe.stb.auth.dto.request;

import com.pfe.stb.shared.validators.ValidPassword;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SetPasswordDto(
    @Min(value = 6, message = "Code must be at least {] characters long" + 6)
        @NotBlank(message = "Code is required")
        String code,
    @NotNull(message = "Password is required")
        @NotBlank(message = "Password is required")
        @ValidPassword
        String password,
    @NotNull(message = "Password is required")
        @NotBlank(message = "Password is required")
        @ValidPassword
        String confirmPassword) {}
