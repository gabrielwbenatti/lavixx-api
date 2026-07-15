package com.benattidev.lavixx.dto.payment;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotNull;

/** Corrige a data de um pagamento ja registrado (uso administrativo). */
public record UpdatePaymentDateRequest(
        @NotNull(message = "Data do pagamento e obrigatoria")
        OffsetDateTime paidAt) {
}
