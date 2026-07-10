package com.benattidev.lavixx.mapper;

import org.springframework.stereotype.Component;

import com.benattidev.lavixx.dto.expense.ExpenseResponse;
import com.benattidev.lavixx.entity.Expense;

@Component
public class ExpenseMapper {

    public ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getCategory(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getDescription(),
                expense.getCreatedAt(),
                expense.getUpdatedAt());
    }
}
