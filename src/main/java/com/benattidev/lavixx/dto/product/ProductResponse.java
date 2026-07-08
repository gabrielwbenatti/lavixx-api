package com.benattidev.lavixx.dto.product;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        BigDecimal price,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
