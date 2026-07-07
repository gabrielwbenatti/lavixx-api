package com.benattidev.lavixx.dto.tenant;

import java.math.BigDecimal;
import java.util.UUID;

public record TenantResponse(
    UUID id,
    String name,
    String document,
    String operatingHoursStart,
    String operatingHoursEnd,
    BigDecimal defaultServiceTax
) {}
