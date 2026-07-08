package com.benattidev.lavixx.dto.tenant;

import java.math.BigDecimal;

public record TenantUpdateRequest(
    String name,
    String operatingHoursStart,
    String operatingHoursEnd,
    BigDecimal defaultServiceTax,
    Boolean loyaltyEnabled,
    Short loyaltyTarget,
    BigDecimal loyaltyRewardPercent
) {}
