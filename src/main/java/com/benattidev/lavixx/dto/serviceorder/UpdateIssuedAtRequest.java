package com.benattidev.lavixx.dto.serviceorder;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotNull;

/** Corrige a data de emissao de uma OS (uso administrativo: lancamento retroativo). */
public record UpdateIssuedAtRequest(
        @NotNull(message = "Data de emissao e obrigatoria")
        OffsetDateTime issuedAt) {
}
