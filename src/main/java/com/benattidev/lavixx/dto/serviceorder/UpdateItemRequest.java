package com.benattidev.lavixx.dto.serviceorder;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;

public record UpdateItemRequest(
        BigDecimal discount,

        @Min(value = 1, message = "Quantidade minima e 1")
        Short quantity) {
}
