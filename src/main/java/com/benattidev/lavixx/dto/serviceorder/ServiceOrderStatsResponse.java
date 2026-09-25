package com.benattidev.lavixx.dto.serviceorder;

import java.math.BigDecimal;

/**
 * Resumo das ordens que atendem a um filtro (ex.: historico de um cliente ou veiculo).
 * `paidTotal` soma os pagamentos das ordens concluidas.
 */
public record ServiceOrderStatsResponse(
        long totalOrders,
        long completedOrders,
        BigDecimal paidTotal) {
}
