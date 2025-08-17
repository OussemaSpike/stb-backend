package com.pfe.stb.user.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserForAdminDto(
    UUID id,
    String email,
    String fullName,
    String firstName,
    String lastName,
    String phoneNumber,
    String cin,
    BankAccountAdminDto bankAccount,
    boolean enabled,
    boolean emailVerified,
    boolean accountLocked,
    List<String> roles,
    Instant createdAt,
    Instant updatedAt) {}
