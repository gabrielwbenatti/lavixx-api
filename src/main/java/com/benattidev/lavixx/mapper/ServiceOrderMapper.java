package com.benattidev.lavixx.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.benattidev.lavixx.dto.serviceorder.ServiceOrderItemResponse;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderResponse;
import com.benattidev.lavixx.entity.ServiceOrder;
import com.benattidev.lavixx.entity.ServiceOrderItem;

@Component
public class ServiceOrderMapper {

    public ServiceOrderResponse toResponse(ServiceOrder order) {
        List<ServiceOrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();
        BigDecimal total = items.stream()
                .map(ServiceOrderItemResponse::finalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ServiceOrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getVehicle().getId(),
                order.getStatus(),
                items,
                total,
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
}
