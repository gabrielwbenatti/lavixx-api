package com.benattidev.lavixx.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInviteRequest(
        @NotBlank(message = "Token e obrigatorio")
        String token,

        @NotBlank(message = "Senha e obrigatoria")
        @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
        String password) {
}
