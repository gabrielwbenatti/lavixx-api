package com.benattidev.lavixx.dto.report;

import java.math.BigDecimal;

import com.benattidev.lavixx.entity.enums.ExpenseCategory;

public record ExpenseCategoryTotal(
        ExpenseCategory category,
        long count,
        BigDecimal total) {
}
