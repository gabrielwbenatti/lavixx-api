package com.benattidev.lavixx.dto.customer;

import java.util.Locale;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank(message = "Nome do cliente e obrigatorio")
        @Size(max = 150)
        String name,

        // CPF: 11 digitos. CNPJ: 12 alfanumericos + 2 digitos verificadores (CNPJ alfanumerico).
        @Pattern(regexp = "\\d{11}|[A-Z0-9]{12}\\d{2}|", message = "Documento deve ser um CPF (11 digitos) ou CNPJ (14 caracteres)")
        String document) {

    // Normaliza o documento removendo formatacao (mantem alfanumerico, em maiusculas).
    public CustomerRequest {
        if (document != null) {
            document = document.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        }
    }
}
