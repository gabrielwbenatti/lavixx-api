package com.benattidev.lavixx.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.benattidev.lavixx.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    Optional<Vehicle> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Vehicle> findAllByTenantId(UUID tenantId);

    List<Vehicle> findAllByTenantIdAndCustomerId(UUID tenantId, UUID customerId);

    Optional<Vehicle> findByTenantIdAndPlate(UUID tenantId, String plate);
}
