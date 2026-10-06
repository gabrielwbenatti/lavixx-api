package com.benattidev.lavixx.dto.report;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Producao de um funcionario nas OS concluidas do periodo. {@code employeeId} nulo
 * representa os itens sem funcionario informado ("Sem funcionario").
 *
 * @param orders   OS distintas em que participou
 * @param quantity unidades dos itens em que participou (conta cheia para cada participante)
 * @param total    soma da sua parte nos itens (valor do item dividido igualmente entre os
 *                 funcionarios do item)
 */
public record EmployeeTotal(
        UUID employeeId,
        String name,
        long orders,
        long quantity,
        BigDecimal total) {
}
