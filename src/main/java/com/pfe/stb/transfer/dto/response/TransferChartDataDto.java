package com.pfe.stb.transfer.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransferChartDataDto(
    LocalDate date,
    Long count,
    BigDecimal amount
) {}
