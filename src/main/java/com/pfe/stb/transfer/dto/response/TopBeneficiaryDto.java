package com.pfe.stb.transfer.dto.response;

import java.math.BigDecimal;

public record TopBeneficiaryDto(
    String beneficiaryName,
    String beneficiaryRib,
    Long transferCount,
    BigDecimal totalAmount
) {}
