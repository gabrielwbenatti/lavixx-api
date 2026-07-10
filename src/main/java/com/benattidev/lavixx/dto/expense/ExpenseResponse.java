package com.benattidev.lavixx.dto.expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.benattidev.lavixx.entity.enums.ExpenseCategory;

public record ExpenseResponse(
        UUID id,
        ExpenseCategory category,
        BigDecimal amount,
        LocalDate date,
        String description,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
