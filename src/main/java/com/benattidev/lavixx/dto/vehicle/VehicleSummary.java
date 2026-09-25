package com.benattidev.lavixx.dto.vehicle;

import java.util.UUID;

import com.benattidev.lavixx.entity.enums.VehicleType;

/** Dados do veiculo embutidos em outras respostas (ex.: ordem de servico). */
public record VehicleSummary(
        UUID id,
        VehicleType type,
        String plate,
        String identifier,
        String nickname,
        String manufacturer,
        String model,
        String color,
        Short year) {
}
