package com.benattidev.lavixx.dto.user;

import com.benattidev.lavixx.entity.enums.UserRole;

import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull(message = "Papel e obrigatorio")
        UserRole role) {
}
