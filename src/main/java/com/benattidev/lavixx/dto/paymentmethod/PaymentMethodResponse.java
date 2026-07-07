package com.benattidev.lavixx.dto.paymentmethod;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentMethodResponse(
        UUID id,
        String name,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
