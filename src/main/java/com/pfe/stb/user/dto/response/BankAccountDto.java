package com.pfe.stb.user.dto.response;

import com.pfe.stb.user.model.AccountStatus;
import com.pfe.stb.user.model.BankAccountType;
import java.util.UUID;
import lombok.Builder;

@Builder
public record BankAccountDto(
    UUID id,
    String rib,
    BankAccountType accountType,
    String balance,
    String availableBalance,
    String currency,
    AccountStatus status) {}
