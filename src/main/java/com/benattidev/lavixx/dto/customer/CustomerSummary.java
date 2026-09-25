package com.benattidev.lavixx.dto.customer;

import java.util.UUID;

/** Dados do cliente embutidos em outras respostas (ex.: ordem de servico). */
public record CustomerSummary(
        UUID id,
        String name,
        String document,
        String phone) {
}
