package com.benattidev.lavixx.dto.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmployeeRequest(
        @NotBlank(message = "Nome do funcionario e obrigatorio")
        @Size(max = 150)
        String name,

        Boolean active) {
}
