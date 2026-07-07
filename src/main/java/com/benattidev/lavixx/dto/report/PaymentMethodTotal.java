package com.benattidev.lavixx.dto.report;

import java.math.BigDecimal;

public record PaymentMethodTotal(
        String methodName,
        long count,
        BigDecimal total) {
}
