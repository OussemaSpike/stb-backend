package com.pfe.stb.transfer.dto.response;

import com.pfe.stb.transfer.model.TransferStatus;

public record TransferStatusStatsDto(
    TransferStatus status,
    Long count,
    Double percentage
) {}
