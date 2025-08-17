package com.pfe.stb.user.dto.request;

import com.pfe.stb.shared.validators.NotBlankEnum;
import com.pfe.stb.user.model.BankAccountType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateClientRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank String cin,
    @Pattern(regexp = "\\d{3}") @NotBlank String branchCode,
    @NotBlank @Email String email,
    @Pattern(regexp = "\\d{8}") String phone,
    @NotBlankEnum BankAccountType accountType) {}
