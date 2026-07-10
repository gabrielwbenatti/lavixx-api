package com.benattidev.lavixx.dto.user;

import java.util.Locale;

import com.benattidev.lavixx.entity.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InviteUserRequest(
        @NotBlank(message = "Nome e obrigatorio")
        @Size(max = 150)
        String name,

        @NotBlank(message = "E-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        @Size(max = 150)
        String email,

        @NotNull(message = "Papel e obrigatorio")
        UserRole role) {

    // Normaliza o e-mail (trim + minusculas) antes de validar/gravar.
    public InviteUserRequest {
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }
}
