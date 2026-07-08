package com.benattidev.lavixx.dto.serviceorder;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.Min;

/** Um item da OS referencia OU um serviço OU um produto (exatamente um). */
public record ServiceOrderItemRequest(
        UUID serviceId,

        UUID productId,

        BigDecimal discount,

        @Min(value = 1, message = "Quantidade minima e 1")
        Short quantity) {
}
