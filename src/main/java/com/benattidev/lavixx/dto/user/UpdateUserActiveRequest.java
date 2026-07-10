package com.benattidev.lavixx.dto.user;

import jakarta.validation.constraints.NotNull;

public record UpdateUserActiveRequest(
        @NotNull(message = "Informe se o usuario deve ficar ativo")
        Boolean active) {
}
