package com.benattidev.lavixx.dto.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank(message = "Nome do cliente e obrigatorio")
        @Size(max = 150)
        String name,

        @Pattern(regexp = "\\d{11}|\\d{14}|", message = "Documento deve conter 11 (CPF) ou 14 (CNPJ) digitos")
        String document) {
}
