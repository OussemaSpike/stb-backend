package com.pfe.stb.user.dto.response;

import com.pfe.stb.user.model.AccountStatus;
import com.pfe.stb.user.model.BankAccountType;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record BankAccountAdminDto(
    UUID id,
    String rib,
    String branchCode,
    BankAccountType accountType,
    UUID userId,
    String userFullName,
    String balance,
    String availableBalance,
    String blockedAmount,
    String currency,
    AccountStatus status,
    String blockReason,
    LocalDate limitResetDate,
    String createdAt,
    String updatedAt) {}
