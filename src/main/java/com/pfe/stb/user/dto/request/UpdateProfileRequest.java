package com.pfe.stb.user.dto.request;

import jakarta.annotation.Nullable;
import java.util.UUID;

public record UpdateProfileRequest(
    UUID id,
    String firstName,
    String lastName,
    // @Min(value = 8, message = "Phone number must be exactly 8 digits long")
    @Nullable String phoneNumber) {}
