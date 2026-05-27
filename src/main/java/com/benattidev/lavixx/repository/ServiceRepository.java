package com.benattidev.lavixx.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.benattidev.lavixx.entity.Service;

public interface ServiceRepository extends JpaRepository<Service, UUID> {

    Optional<Service> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Service> findAllByTenantId(UUID tenantId);
}
