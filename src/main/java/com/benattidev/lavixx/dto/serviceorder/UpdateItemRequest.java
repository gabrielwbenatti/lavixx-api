package com.benattidev.lavixx.dto.serviceorder;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;

public record UpdateItemRequest(
        /** Corrige o preco unitario cobrado neste item (nao altera o catalogo). */
        @DecimalMin(value = "0.00", message = "Preco unitario nao pode ser negativo")
        @Digits(integer = 8, fraction = 2)
        BigDecimal unitPrice,

        BigDecimal discount,

        @Min(value = 1, message = "Quantidade minima e 1")
        Short quantity) {
}
