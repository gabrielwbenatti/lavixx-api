package com.benattidev.lavixx.dto.serviceorder;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ServiceOrderRequest(
        @NotNull(message = "Veiculo e obrigatorio")
        UUID vehicleId,

        @Valid
        List<ServiceOrderItemRequest> items) {
}
