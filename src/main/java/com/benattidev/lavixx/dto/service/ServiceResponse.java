package com.benattidev.lavixx.dto.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ServiceResponse(
        UUID id,
        String name,
        BigDecimal price,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
