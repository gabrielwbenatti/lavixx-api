package com.benattidev.lavixx.dto.serviceorder;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceOrderItemResponse(
        UUID id,
        UUID serviceId,
        UUID productId,
        String name,
        BigDecimal unitPrice,
        BigDecimal discount,
        Short quantity,
        BigDecimal finalPrice) {
}
