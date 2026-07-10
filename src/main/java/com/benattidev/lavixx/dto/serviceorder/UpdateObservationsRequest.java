package com.benattidev.lavixx.dto.serviceorder;

import jakarta.validation.constraints.Size;

/** Atualiza as observacoes livres de uma OS (vazio limpa). */
public record UpdateObservationsRequest(
        @Size(max = 1000, message = "Observacoes muito longas (max 1000)")
        String observations) {
}
