package com.benattidev.lavixx.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.benattidev.lavixx.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByPaymentMethodId(UUID paymentMethodId);

    List<Payment> findAllByTenantIdAndPaidAtBetween(
            UUID tenantId, OffsetDateTime from, OffsetDateTime to);
}
