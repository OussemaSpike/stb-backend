package com.pfe.stb.user.dto.response;

import lombok.Builder;

@Builder
public record ProfileDto(
    String firstName,
    String lastName,
    String cin,
    String email,
    String phoneNumber,
    String address,
    boolean emailVerified,
    boolean accountLocked,
    BankAccountDto bankAccount
) {}
