package com.benattidev.lavixx.dto.customer;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String document,
        String phone,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
