package com.benattidev.lavixx.dto.payment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID paymentMethodId,
        String methodName,
        BigDecimal amount,
        OffsetDateTime paidAt) {
}
