package com.benattidev.lavixx.dto.paymentmethod;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PaymentMethodRequest(
        @NotBlank(message = "Nome da forma de pagamento e obrigatorio")
        @Size(max = 80)
        String name,

        Boolean active) {
}
