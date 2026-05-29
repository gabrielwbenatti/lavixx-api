package com.benattidev.lavixx.dto.serviceorder;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ServiceOrderItemRequest(
        @NotNull(message = "Servico e obrigatorio")
        UUID serviceId,

        BigDecimal discount,

        @Min(value = 1, message = "Quantidade minima e 1")
        Short quantity) {
}
