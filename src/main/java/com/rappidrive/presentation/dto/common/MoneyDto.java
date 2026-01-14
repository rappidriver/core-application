package com.rappidrive.presentation.dto.common;

import java.math.BigDecimal;

public record MoneyDto(
    BigDecimal amount,
    String currency
) {}
