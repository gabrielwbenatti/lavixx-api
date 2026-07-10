package com.benattidev.lavixx.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benattidev.lavixx.dto.report.ExpenseCategoryTotal;
import com.benattidev.lavixx.dto.report.PaymentMethodTotal;
import com.benattidev.lavixx.dto.report.ReportSummaryResponse;
import com.benattidev.lavixx.dto.report.ServiceTotal;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderItemResponse;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderResponse;
import com.benattidev.lavixx.entity.Expense;
import com.benattidev.lavixx.entity.Payment;
import com.benattidev.lavixx.entity.ServiceOrder;
import com.benattidev.lavixx.entity.enums.ExpenseCategory;
import com.benattidev.lavixx.entity.enums.ServiceStatus;
import com.benattidev.lavixx.mapper.ServiceOrderMapper;
import com.benattidev.lavixx.repository.ExpenseRepository;
import com.benattidev.lavixx.repository.PaymentRepository;
import com.benattidev.lavixx.repository.ServiceOrderRepository;
import com.benattidev.lavixx.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final ServiceOrderMapper serviceOrderMapper;

    @Transactional(readOnly = true)
    public ReportSummaryResponse summary(LocalDate from, LocalDate to) {
        UUID tenantId = SecurityUtils.currentTenantId();
        ZoneId zone = ZoneId.systemDefault();
        OffsetDateTime start = from.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime end = to.atTime(LocalTime.MAX).atZone(zone).toOffsetDateTime();

        // Ordens concluidas no periodo (por data de finalizacao).
        List<ServiceOrderResponse> completed = serviceOrderRepository
                .findAllByTenantIdAndStatusAndFinishedAtBetween(tenantId, ServiceStatus.done, start, end)
                .stream()
                .map(serviceOrderMapper::toResponse)
                .toList();

        BigDecimal revenue = completed.stream()
                .map(ServiceOrderResponse::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal receivable = completed.stream()
                .map(o -> o.total().subtract(o.paidTotal()).max(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long completedCount = completed.size();
        BigDecimal averageTicket = completedCount > 0
                ? revenue.divide(BigDecimal.valueOf(completedCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Faturamento por servico (itens das ordens concluidas).
        Map<String, BigDecimal> serviceTotals = new LinkedHashMap<>();
        Map<String, Long> serviceQty = new LinkedHashMap<>();
        for (ServiceOrderResponse order : completed) {
            for (ServiceOrderItemResponse item : order.items()) {
                serviceTotals.merge(item.name(), item.finalPrice(), BigDecimal::add);
                serviceQty.merge(item.name(), item.quantity().longValue(), Long::sum);
            }
        }
        List<ServiceTotal> byService = serviceTotals.entrySet().stream()
                .map(e -> new ServiceTotal(e.getKey(), serviceQty.get(e.getKey()), e.getValue()))
                .sorted(Comparator.comparing(ServiceTotal::total).reversed())
                .toList();

        // Recebido por forma de pagamento (pagamentos com data no periodo).
        List<Payment> payments =
                paymentRepository.findAllByTenantIdAndPaidAtBetween(tenantId, start, end);
        BigDecimal received = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> methodTotals = new LinkedHashMap<>();
        Map<String, Long> methodCount = new LinkedHashMap<>();
        for (Payment payment : payments) {
            methodTotals.merge(payment.getMethodName(), payment.getAmount(), BigDecimal::add);
            methodCount.merge(payment.getMethodName(), 1L, Long::sum);
        }
        List<PaymentMethodTotal> byPaymentMethod = methodTotals.entrySet().stream()
                .map(e -> new PaymentMethodTotal(e.getKey(), methodCount.get(e.getKey()), e.getValue()))
                .sorted(Comparator.comparing(PaymentMethodTotal::total).reversed())
                .toList();

        // Despesas do periodo (pela data da despesa) + lucro no caixa (recebido - despesas).
        List<Expense> periodExpenses = expenseRepository
                .findAllByTenantIdAndExpenseDateBetweenOrderByExpenseDateDesc(tenantId, from, to);
        BigDecimal expenses = periodExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal profit = received.subtract(expenses);

        Map<ExpenseCategory, BigDecimal> categoryTotals = new LinkedHashMap<>();
        Map<ExpenseCategory, Long> categoryCount = new LinkedHashMap<>();
        for (Expense expense : periodExpenses) {
            categoryTotals.merge(expense.getCategory(), expense.getAmount(), BigDecimal::add);
            categoryCount.merge(expense.getCategory(), 1L, Long::sum);
        }
        List<ExpenseCategoryTotal> byExpenseCategory = categoryTotals.entrySet().stream()
                .map(e -> new ExpenseCategoryTotal(e.getKey(), categoryCount.get(e.getKey()), e.getValue()))
                .sorted(Comparator.comparing(ExpenseCategoryTotal::total).reversed())
                .toList();

        return new ReportSummaryResponse(
                from, to,
                completedCount,
                revenue,
                received,
                receivable,
                averageTicket,
                expenses,
                profit,
                byPaymentMethod,
                byService,
                byExpenseCategory);
    }
}
