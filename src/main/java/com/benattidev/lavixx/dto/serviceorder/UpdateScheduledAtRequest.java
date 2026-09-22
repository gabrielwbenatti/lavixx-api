package com.benattidev.lavixx.dto.serviceorder;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotNull;

/** Reagenda uma OS ainda nao chegada (cliente pediu para mudar o horario, ou lancamento errado). */
public record UpdateScheduledAtRequest(
        @NotNull(message = "Data do agendamento e obrigatoria")
        OffsetDateTime scheduledAt) {
}
