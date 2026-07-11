package com.benattidev.lavixx.dto.serviceorder;

import java.time.OffsetDateTime;

/** Atualiza a previsao de retirada informada pelo cliente (null limpa). */
public record UpdatePickupEstimateRequest(
        OffsetDateTime estimatedPickupAt) {
}
