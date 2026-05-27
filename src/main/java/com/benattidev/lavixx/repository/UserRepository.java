package com.benattidev.lavixx.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.benattidev.lavixx.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<User> findByTenantIdAndEmail(UUID tenantId, String email);

    List<User> findAllByTenantId(UUID tenantId);

    boolean existsByTenantIdAndEmail(UUID tenantId, String email);
}
