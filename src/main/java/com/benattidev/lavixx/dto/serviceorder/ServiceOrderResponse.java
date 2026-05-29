package com.benattidev.lavixx.dto.serviceorder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.benattidev.lavixx.entity.enums.ServiceStatus;

public record ServiceOrderResponse(
        UUID id,
        UUID customerId,
        UUID vehicleId,
        ServiceStatus status,
        List<ServiceOrderItemResponse> items,
        BigDecimal total,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime finishedAt) {
}
