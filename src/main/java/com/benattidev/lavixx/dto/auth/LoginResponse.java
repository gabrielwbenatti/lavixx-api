package com.benattidev.lavixx.dto.auth;

import java.util.UUID;

public record LoginResponse(
        String token,
        UUID userId,
        UUID tenantId,
        String email,
        String role) {
}
