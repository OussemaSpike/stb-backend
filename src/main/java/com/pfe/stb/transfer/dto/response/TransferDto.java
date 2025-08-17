package com.pfe.stb.transfer.dto.response;

import com.pfe.stb.transfer.model.TransferStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransferDto(
    UUID id,
    String reference,
    String fromAccountRib,
    String beneficiaryName,
    String beneficiaryRib,
    BigDecimal amount,
    String currency,
    String reason,
    TransferStatus status,
    BigDecimal fees,
    BigDecimal totalAmount,
    Instant createdAt,
    LocalDateTime executionDate,
    String rejectionReason) {}
