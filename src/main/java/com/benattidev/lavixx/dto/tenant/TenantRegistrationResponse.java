package com.benattidev.lavixx.dto.tenant;

import java.util.UUID;

public record TenantRegistrationResponse(
        UUID tenantId,
        String tenantName,
        UUID adminUserId,
        String adminEmail,
        String token) {
}
