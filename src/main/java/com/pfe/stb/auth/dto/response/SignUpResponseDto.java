package com.pfe.stb.auth.dto.response;

import java.util.List;
import java.util.UUID;

public record SignUpResponseDto(
    UUID id,
    String email,
    String firstName,
    String lastName,
    String phoneNumber,
    boolean enabled,
    boolean emailVerified,
    boolean accountLocked,
    List<String> roles) {}
