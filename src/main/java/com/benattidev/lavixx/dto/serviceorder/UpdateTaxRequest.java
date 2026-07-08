package com.benattidev.lavixx.dto.serviceorder;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Ajusta (ou zera) a taxa de serviço (%) aplicada a uma OS específica. */
public record UpdateTaxRequest(
        @NotNull(message = "Informe a taxa de serviço")
        @DecimalMin(value = "0", message = "A taxa não pode ser negativa")
        @DecimalMax(value = "100", message = "A taxa não pode ser maior que 100%")
        BigDecimal serviceTax) {
}
