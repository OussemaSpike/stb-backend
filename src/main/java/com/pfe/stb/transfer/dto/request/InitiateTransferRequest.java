package com.pfe.stb.transfer.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record InitiateTransferRequest(
    @NotNull(message = "Beneficiary ID is required")
    UUID beneficiaryId,
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "1000000.00", message = "Amount exceeds maximum limit")
    BigDecimal amount,
    
    @NotBlank(message = "Transfer reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    String reason
) {}
