package com.benattidev.lavixx.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.benattidev.lavixx.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    Optional<Expense> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Expense> findAllByTenantIdOrderByExpenseDateDesc(UUID tenantId);

    List<Expense> findAllByTenantIdAndExpenseDateBetweenOrderByExpenseDateDesc(
            UUID tenantId, LocalDate from, LocalDate to);
}
