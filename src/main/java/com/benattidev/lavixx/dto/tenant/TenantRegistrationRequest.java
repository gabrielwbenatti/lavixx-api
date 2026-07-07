package com.benattidev.lavixx.dto.tenant;

import java.util.Locale;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TenantRegistrationRequest(
        @NotBlank(message = "Nome do estabelecimento e obrigatorio")
        @Size(max = 150)
        String name,

        @NotBlank(message = "CPF/CNPJ e obrigatorio")
        @Pattern(regexp = "\\d{11}|\\d{14}", message = "Documento deve conter 11 (CPF) ou 14 (CNPJ) digitos")
        String document,

        @NotBlank(message = "Nome do administrador e obrigatorio")
        @Size(max = 150)
        String adminName,

        @NotBlank(message = "E-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        @Size(max = 150)
        String adminEmail,

        @NotBlank(message = "Senha e obrigatoria")
        @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
        String adminPassword) {

    // Normaliza o e-mail (trim + minusculas) para garantir cadastro/busca consistentes.
    public TenantRegistrationRequest {
        if (adminEmail != null) {
            adminEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        }
    }
}
