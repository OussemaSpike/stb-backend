package com.pfe.stb.user.dto.response;

import java.util.UUID;

public record SimpleUserResponseDto(
    UUID id, String fullName, String firstName, String lastName, String profilePicture) {}
