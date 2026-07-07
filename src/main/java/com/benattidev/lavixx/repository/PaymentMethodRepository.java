package com.benattidev.lavixx.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.benattidev.lavixx.entity.PaymentMethod;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {

    Optional<PaymentMethod> findByIdAndTenantId(UUID id, UUID tenantId);

    List<PaymentMethod> findAllByTenantIdOrderByNameAsc(UUID tenantId);
}
