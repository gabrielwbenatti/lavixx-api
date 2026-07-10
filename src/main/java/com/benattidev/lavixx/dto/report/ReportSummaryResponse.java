package com.benattidev.lavixx.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReportSummaryResponse(
        LocalDate from,
        LocalDate to,
        long completedOrders,
        BigDecimal revenue,       // faturado: soma dos totais das OS concluidas no periodo
        BigDecimal received,      // recebido: soma dos pagamentos no periodo
        BigDecimal receivable,    // a receber: restante das OS concluidas no periodo
        BigDecimal averageTicket,
        BigDecimal expenses,      // despesas: soma das despesas no periodo (por data da despesa)
        BigDecimal profit,        // lucro (caixa): recebido - despesas
        List<PaymentMethodTotal> byPaymentMethod,
        List<ServiceTotal> byService,
        List<ExpenseCategoryTotal> byExpenseCategory) {
}
