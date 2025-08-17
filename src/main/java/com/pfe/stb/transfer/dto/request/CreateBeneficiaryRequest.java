package com.pfe.stb.transfer.dto.request;

import jakarta.validation.constraints.*;

public record CreateBeneficiaryRequest(
    @NotBlank(message = "Beneficiary name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    String name,
    
    @NotBlank(message = "RIB is required")
    @Pattern(regexp = "\\d{20}", message = "RIB must be exactly 20 digits")
    String rib
) {}
