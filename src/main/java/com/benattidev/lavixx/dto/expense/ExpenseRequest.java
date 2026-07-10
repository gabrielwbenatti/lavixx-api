package com.benattidev.lavixx.dto.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.benattidev.lavixx.entity.enums.ExpenseCategory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExpenseRequest(
        @NotNull(message = "Categoria e obrigatoria")
        ExpenseCategory category,

        @NotNull(message = "Valor e obrigatorio")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        @Digits(integer = 8, fraction = 2)
        BigDecimal amount,

        @NotNull(message = "Data e obrigatoria")
        LocalDate date,

        @Size(max = 255, message = "Descricao muito longa (max 255)")
        String description) {
}
