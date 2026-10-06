package com.benattidev.lavixx.dto.employee;

import java.util.UUID;

/** Funcionario resumido, embutido nos itens da OS. */
public record EmployeeSummary(UUID id, String name) {
}
