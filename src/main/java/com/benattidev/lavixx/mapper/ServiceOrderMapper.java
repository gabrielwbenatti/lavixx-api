package com.benattidev.lavixx.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Component;

import com.benattidev.lavixx.dto.payment.PaymentResponse;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderItemResponse;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderResponse;
import com.benattidev.lavixx.entity.Payment;
import com.benattidev.lavixx.entity.ServiceOrder;
import com.benattidev.lavixx.entity.ServiceOrderItem;
import com.benattidev.lavixx.entity.enums.PaymentStatus;

@Component
public class ServiceOrderMapper {

    public ServiceOrderResponse toResponse(ServiceOrder order) {
        List<ServiceOrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();
        BigDecimal subtotal = items.stream()
                .map(ServiceOrderItemResponse::finalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxRate = order.getServiceTax() != null ? order.getServiceTax() : BigDecimal.ZERO;
        BigDecimal taxAmount = subtotal
                .multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(taxAmount);

        List<PaymentResponse> payments = order.getPayments().stream()
                .map(this::toPaymentResponse)
                .toList();
        BigDecimal paidTotal = payments.stream()
                .map(PaymentResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ServiceOrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getVehicle().getId(),
                order.getStatus(),
                items,
                subtotal,
                taxRate,
                taxAmount,
                total,
                payments,
                paidTotal,
                resolvePaymentStatus(total, paidTotal),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getFinishedAt());
    }

    public ServiceOrderItemResponse toItemResponse(ServiceOrderItem item) {
        BigDecimal finalPrice = item.getUnitPrice()
                .subtract(item.getDiscount())
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        return new ServiceOrderItemResponse(
                item.getId(),
                item.getService() != null ? item.getService().getId() : null,
                item.getName(),
                item.getUnitPrice(),
                item.getDiscount(),
                item.getQuantity(),
                finalPrice);
    }

    public PaymentResponse toPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentMethod() != null ? payment.getPaymentMethod().getId() : null,
                payment.getMethodName(),
                payment.getAmount(),
                payment.getPaidAt());
    }

    /** pending: nada pago. paid: pago cobre o total. partial: pago parcial (> 0 e < total). */
    private PaymentStatus resolvePaymentStatus(BigDecimal total, BigDecimal paidTotal) {
        if (paidTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return PaymentStatus.pending;
        }
        if (paidTotal.compareTo(total) >= 0) {
            return PaymentStatus.paid;
        }
        return PaymentStatus.partial;
    }
}
