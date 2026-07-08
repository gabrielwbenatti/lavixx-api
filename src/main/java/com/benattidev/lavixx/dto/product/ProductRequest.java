package com.benattidev.lavixx.dto.product;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotBlank(message = "Nome do produto e obrigatorio")
        @Size(max = 150)
        String name,

        @NotNull(message = "Preco e obrigatorio")
        @DecimalMin(value = "0.00", inclusive = true, message = "Preco nao pode ser negativo")
        @Digits(integer = 8, fraction = 2)
        BigDecimal price) {
}
