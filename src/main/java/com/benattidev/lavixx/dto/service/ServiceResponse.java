package com.benattidev.lavixx.dto.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ServiceResponse(
        UUID id,
        String name,
        BigDecimal price,
        BigDecimal priceSmall,
        BigDecimal priceMedium,
        BigDecimal priceLarge,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
