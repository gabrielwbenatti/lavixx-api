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
        // CPF: 11 digitos. CNPJ: 12 alfanumericos + 2 digitos verificadores (CNPJ alfanumerico).
        @Pattern(regexp = "\\d{11}|[A-Z0-9]{12}\\d{2}", message = "Documento deve ser um CPF (11 digitos) ou CNPJ (14 caracteres)")
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

    // Normaliza e-mail (trim + minusculas) e documento (alfanumerico em maiusculas) antes de validar/gravar.
    public TenantRegistrationRequest {
        if (adminEmail != null) {
            adminEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        }
        if (document != null) {
            document = document.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        }
    }
}
