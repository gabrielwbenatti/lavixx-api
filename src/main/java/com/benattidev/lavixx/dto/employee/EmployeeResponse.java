package com.benattidev.lavixx.dto.employee;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EmployeeResponse(
        UUID id,
        String name,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
