package com.pfe.stb.transfer.dto.response;

import java.math.BigDecimal;

public record MonthlyComparisonDto(
    Long currentMonthTransfers,
    BigDecimal currentMonthAmount,
    Long previousMonthTransfers,
    BigDecimal previousMonthAmount,
    Double transferGrowthPercentage,
    Double amountGrowthPercentage
) {}
