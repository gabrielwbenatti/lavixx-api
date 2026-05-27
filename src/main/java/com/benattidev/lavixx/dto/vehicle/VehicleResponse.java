package com.benattidev.lavixx.dto.vehicle;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.benattidev.lavixx.entity.enums.VehicleType;

public record VehicleResponse(
        UUID id,
        UUID customerId,
        VehicleType type,
        String plate,
        String identifier,
        String nickname,
        String manufacturer,
        String model,
        String color,
        Short year,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
