package com.benattidev.lavixx.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.benattidev.lavixx.dto.payment.PaymentRequest;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderItemRequest;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderItemResponse;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderRequest;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderResponse;
import com.benattidev.lavixx.dto.serviceorder.UpdateItemRequest;
import com.benattidev.lavixx.dto.serviceorder.UpdateStatusRequest;
import com.benattidev.lavixx.entity.enums.ServiceStatus;
import com.benattidev.lavixx.service.ServiceOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/service-orders")
@RequiredArgsConstructor
public class ServiceOrderController {

    private final ServiceOrderService serviceOrderService;

    @GetMapping
    public ResponseEntity<List<ServiceOrderResponse>> list(
            @RequestParam(required = false) ServiceStatus status) {
        return ResponseEntity.ok(serviceOrderService.list(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOrderResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceOrderService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ServiceOrderResponse> create(@Valid @RequestBody ServiceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceOrderService.create(request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ServiceOrderResponse> updateStatus(@PathVariable UUID id,
                                                             @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(serviceOrderService.updateStatus(id, request.status()));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ServiceOrderResponse> addItem(@PathVariable UUID id,
                                                        @Valid @RequestBody ServiceOrderItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceOrderService.addItem(id, request));
    }

    @PutMapping("/{id}/items/{itemId}")
    public ResponseEntity<ServiceOrderItemResponse> updateItem(@PathVariable UUID id,
                                                               @PathVariable UUID itemId,
                                                               @Valid @RequestBody UpdateItemRequest request) {
        return ResponseEntity.ok(serviceOrderService.updateItem(id, itemId, request));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable UUID id, @PathVariable UUID itemId) {
        serviceOrderService.removeItem(id, itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<ServiceOrderResponse> addPayment(@PathVariable UUID id,
                                                           @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceOrderService.addPayment(id, request));
    }

    @DeleteMapping("/{id}/payments/{paymentId}")
    public ResponseEntity<ServiceOrderResponse> removePayment(@PathVariable UUID id,
                                                              @PathVariable UUID paymentId) {
        return ResponseEntity.ok(serviceOrderService.removePayment(id, paymentId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        serviceOrderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
