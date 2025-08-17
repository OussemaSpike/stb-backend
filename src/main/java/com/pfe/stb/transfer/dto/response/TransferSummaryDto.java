package com.pfe.stb.transfer.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record TransferSummaryDto(
    UUID transferId,
    String reference,
    String fromAccountRib,
    String beneficiaryName,
    String beneficiaryRib,
    BigDecimal amount,
    String currency,
    String reason,
    BigDecimal fees,
    BigDecimal totalAmount,
    Instant initiatedAt) {}
