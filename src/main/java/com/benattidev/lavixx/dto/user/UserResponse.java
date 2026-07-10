package com.benattidev.lavixx.dto.user;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.benattidev.lavixx.entity.enums.UserRole;

public record UserResponse(
        UUID id,
        String name,
        String email,
        UserRole role,
        boolean active,
        boolean pending, // convite ainda nao aceito (nunca definiu senha)
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
