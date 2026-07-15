package com.benattidev.lavixx.dto.serviceorder;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotNull;

/** Corrige a data de finalizacao de uma OS ja concluida/cancelada (uso administrativo). */
public record UpdateFinishedAtRequest(
        @NotNull(message = "Data de finalizacao e obrigatoria")
        OffsetDateTime finishedAt) {
}
