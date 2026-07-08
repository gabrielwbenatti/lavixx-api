package com.benattidev.lavixx.dto.serviceorder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.benattidev.lavixx.dto.payment.PaymentResponse;
import com.benattidev.lavixx.entity.enums.PaymentStatus;
import com.benattidev.lavixx.entity.enums.ServiceStatus;

public record ServiceOrderResponse(
        UUID id,
        UUID customerId,
        UUID vehicleId,
        ServiceStatus status,
        List<ServiceOrderItemResponse> items,
        BigDecimal subtotal,
        BigDecimal serviceTax,
        BigDecimal taxAmount,
        BigDecimal total,
        List<PaymentResponse> payments,
        BigDecimal paidTotal,
        PaymentStatus paymentStatus,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime finishedAt) {
}
