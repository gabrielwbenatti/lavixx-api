package com.benattidev.lavixx.dto.serviceorder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.benattidev.lavixx.entity.enums.ServiceStatus;

/**
 * Filtros da listagem de ordens (todos opcionais). fromDate/toDate se referem a issuedAt;
 * finishedFrom/finishedTo, a data de conclusao; min/maxAmount, ao total da OS.
 */
public record ServiceOrderFilter(
        ServiceStatus status,
        UUID customerId,
        UUID vehicleId,
        OffsetDateTime fromDate,
        OffsetDateTime toDate,
        OffsetDateTime finishedFrom,
        OffsetDateTime finishedTo,
        BigDecimal minAmount,
        BigDecimal maxAmount) {
}
