package com.benattidev.lavixx.dto.payment;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull(message = "Forma de pagamento e obrigatoria")
        UUID paymentMethodId,

        @NotNull(message = "Valor e obrigatorio")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        @Digits(integer = 8, fraction = 2)
        BigDecimal amount) {
}
