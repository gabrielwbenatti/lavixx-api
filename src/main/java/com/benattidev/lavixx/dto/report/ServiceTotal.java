package com.benattidev.lavixx.dto.report;

import java.math.BigDecimal;

public record ServiceTotal(
        String name,
        long quantity,
        BigDecimal total) {
}
