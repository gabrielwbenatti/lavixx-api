package com.benattidev.lavixx.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.benattidev.lavixx.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByPaymentMethodId(UUID paymentMethodId);
}
