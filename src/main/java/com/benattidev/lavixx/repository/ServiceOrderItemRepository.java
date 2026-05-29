package com.benattidev.lavixx.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.benattidev.lavixx.entity.ServiceOrderItem;

public interface ServiceOrderItemRepository extends JpaRepository<ServiceOrderItem, UUID> {

    Optional<ServiceOrderItem> findByIdAndTenantId(UUID id, UUID tenantId);
}
