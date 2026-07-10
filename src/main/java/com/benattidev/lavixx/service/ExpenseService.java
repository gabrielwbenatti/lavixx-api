package com.benattidev.lavixx.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benattidev.lavixx.dto.expense.ExpenseRequest;
import com.benattidev.lavixx.dto.expense.ExpenseResponse;
import com.benattidev.lavixx.entity.Expense;
import com.benattidev.lavixx.entity.Tenant;
import com.benattidev.lavixx.exception.NotFoundException;
import com.benattidev.lavixx.mapper.ExpenseMapper;
import com.benattidev.lavixx.repository.ExpenseRepository;
import com.benattidev.lavixx.security.SecurityUtils;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final EntityManager entityManager;

    /** Lista as despesas do tenant; se um periodo for informado, filtra por data. */
    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(LocalDate from, LocalDate to) {
        UUID tenantId = SecurityUtils.currentTenantId();
        List<Expense> expenses = (from != null && to != null)
                ? expenseRepository.findAllByTenantIdAndExpenseDateBetweenOrderByExpenseDateDesc(tenantId, from, to)
                : expenseRepository.findAllByTenantIdOrderByExpenseDateDesc(tenantId);
        return expenses.stream().map(expenseMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse findById(UUID id) {
        return expenseMapper.toResponse(loadOwned(id));
    }

    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        UUID tenantId = SecurityUtils.currentTenantId();
        Expense expense = Expense.builder()
                .tenant(entityManager.getReference(Tenant.class, tenantId))
                .category(request.category())
                .amount(request.amount())
                .expenseDate(request.date())
                .description(request.description())
                .build();
        return expenseMapper.toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse update(UUID id, ExpenseRequest request) {
        Expense expense = loadOwned(id);
        expense.setCategory(request.category());
        expense.setAmount(request.amount());
        expense.setExpenseDate(request.date());
        expense.setDescription(request.description());
        return expenseMapper.toResponse(expense);
    }

    @Transactional
    public void delete(UUID id) {
        expenseRepository.delete(loadOwned(id));
    }

    private Expense loadOwned(UUID id) {
        return expenseRepository.findByIdAndTenantId(id, SecurityUtils.currentTenantId())
                .orElseThrow(() -> new NotFoundException("Despesa nao encontrada"));
    }
}
