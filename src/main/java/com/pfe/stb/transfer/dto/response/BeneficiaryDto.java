package com.pfe.stb.transfer.dto.response;

import java.util.UUID;

public record BeneficiaryDto(
    UUID id, String name, String rib, Boolean isVerified, Boolean isActive) {}
