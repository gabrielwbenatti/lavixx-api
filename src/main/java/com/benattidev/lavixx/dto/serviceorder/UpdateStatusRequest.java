package com.benattidev.lavixx.dto.serviceorder;

import com.benattidev.lavixx.entity.enums.ServiceStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull(message = "Status e obrigatorio")
        ServiceStatus status) {
}
