package com.benattidev.lavixx.dto.serviceorder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.benattidev.lavixx.dto.employee.EmployeeSummary;

public record ServiceOrderItemResponse(
        UUID id,
        UUID serviceId,
        UUID productId,
        String name,
        BigDecimal unitPrice,
        BigDecimal discount,
        Short quantity,
        BigDecimal finalPrice,
        List<EmployeeSummary> employees) {
}
