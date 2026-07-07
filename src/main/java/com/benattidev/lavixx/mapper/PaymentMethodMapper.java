package com.benattidev.lavixx.mapper;

import org.springframework.stereotype.Component;

import com.benattidev.lavixx.dto.paymentmethod.PaymentMethodResponse;
import com.benattidev.lavixx.entity.PaymentMethod;

@Component
public class PaymentMethodMapper {

    public PaymentMethodResponse toResponse(PaymentMethod method) {
        return new PaymentMethodResponse(
                method.getId(),
                method.getName(),
                method.isActive(),
                method.getCreatedAt(),
                method.getUpdatedAt());
    }
}
