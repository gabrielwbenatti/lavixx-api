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
        String document,

        // Telefone/celular: vazio ou 10 (fixo) / 11 (celular) digitos com DDD.
        @Pattern(regexp = "\\d{10,11}|", message = "Telefone deve ter 10 ou 11 digitos (com DDD)")
        String phone) {

    // Normaliza documento (alfanumerico em maiusculas) e telefone (apenas digitos).
    public CustomerRequest {
        if (document != null) {
            document = document.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        }
        if (phone != null) {
            phone = phone.replaceAll("\\D", "");
        }
    }
}
