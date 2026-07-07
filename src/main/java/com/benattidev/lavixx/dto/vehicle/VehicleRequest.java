package com.benattidev.lavixx.dto.vehicle;

import java.util.Locale;
import java.util.UUID;

import com.benattidev.lavixx.entity.enums.VehicleType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehicleRequest(
        @NotNull(message = "Cliente e obrigatorio")
        UUID customerId,

        @NotNull(message = "Tipo de veiculo e obrigatorio")
        VehicleType type,

        @Size(max = 10)
        String plate,

        @Size(max = 60)
        String identifier,

        @Size(max = 80)
        String nickname,

        @Size(max = 60)
        String manufacturer,

        @Size(max = 60)
        String model,

        @Size(max = 30)
        String color,

        Short year) {

    // Normaliza a placa removendo formatacao (apenas alfanumerico, em maiusculas).
    public VehicleRequest {
        if (plate != null) {
            plate = plate.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        }
    }
}
