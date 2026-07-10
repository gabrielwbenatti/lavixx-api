package com.benattidev.lavixx.dto.serviceorder;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServiceOrderRequest(
        @NotNull(message = "Veiculo e obrigatorio")
        UUID vehicleId,

        @Valid
        List<ServiceOrderItemRequest> items,

        @Size(max = 1000, message = "Observacoes muito longas (max 1000)")
        String observations) {
}
