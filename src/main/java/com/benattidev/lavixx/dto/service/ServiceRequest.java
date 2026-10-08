package com.benattidev.lavixx.dto.service;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServiceRequest(
        @NotBlank(message = "Nome do servico e obrigatorio")
        @Size(max = 150)
        String name,

        /** Preco padrao: vale quando nao ha preco especifico para o porte do veiculo. */
        @NotNull(message = "Preco e obrigatorio")
        @DecimalMin(value = "0.00", inclusive = true, message = "Preco nao pode ser negativo")
        @Digits(integer = 8, fraction = 2)
        BigDecimal price,

        /** Precos por porte (opcionais). Em branco = usa o preco padrao. */
        @DecimalMin(value = "0.00", message = "Preco nao pode ser negativo")
        @Digits(integer = 8, fraction = 2)
        BigDecimal priceSmall,

        @DecimalMin(value = "0.00", message = "Preco nao pode ser negativo")
        @Digits(integer = 8, fraction = 2)
        BigDecimal priceMedium,

        @DecimalMin(value = "0.00", message = "Preco nao pode ser negativo")
        @Digits(integer = 8, fraction = 2)
        BigDecimal priceLarge,

        /** Duracao estimada em minutos (opcional). */
        @Min(value = 1, message = "Duracao minima e 1 minuto")
        @Max(value = 1440, message = "Duracao maxima e 1440 minutos (24 horas)")
        Short durationMinutes) {
}
