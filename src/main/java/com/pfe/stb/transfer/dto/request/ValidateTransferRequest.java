package com.pfe.stb.transfer.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ValidateTransferRequest(
    @NotNull(message = "Transfer ID is required") 
    UUID transferId
) {}
