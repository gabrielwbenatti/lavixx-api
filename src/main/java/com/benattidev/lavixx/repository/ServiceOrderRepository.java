package com.benattidev.lavixx.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.benattidev.lavixx.entity.ServiceOrder;
import com.benattidev.lavixx.entity.enums.ServiceStatus;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, UUID> {

    Optional<ServiceOrder> findByIdAndTenantId(UUID id, UUID tenantId);

    List<ServiceOrder> findAllByTenantId(UUID tenantId);

    List<ServiceOrder> findAllByTenantIdAndStatus(UUID tenantId, ServiceStatus status);

    List<ServiceOrder> findAllByTenantIdAndCustomerId(UUID tenantId, UUID customerId);

    List<ServiceOrder> findAllByTenantIdAndVehicleId(UUID tenantId, UUID vehicleId);

    List<ServiceOrder> findAllByTenantIdAndStatusAndFinishedAtBetween(
            UUID tenantId, ServiceStatus status, OffsetDateTime from, OffsetDateTime to);

    long countByTenantIdAndCustomerIdAndStatus(UUID tenantId, UUID customerId, ServiceStatus status);

    List<ServiceOrder> findAllByTenantIdAndStatusAndScheduledAtBetweenOrderByScheduledAtAsc(
            UUID tenantId, ServiceStatus status, OffsetDateTime from, OffsetDateTime to);

    List<ServiceOrder> findAllByTenantIdAndEstimatedPickupAtBetweenOrderByEstimatedPickupAtAsc(
            UUID tenantId, OffsetDateTime from, OffsetDateTime to);
}
