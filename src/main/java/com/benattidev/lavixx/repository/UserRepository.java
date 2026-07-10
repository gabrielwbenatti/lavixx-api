package com.benattidev.lavixx.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.benattidev.lavixx.entity.User;
import com.benattidev.lavixx.entity.enums.UserRole;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<User> findByTenantIdAndEmail(UUID tenantId, String email);

    Optional<User> findFirstByEmail(String email);

    Optional<User> findByInviteToken(String inviteToken);

    List<User> findAllByTenantIdOrderByCreatedAtAsc(UUID tenantId);

    boolean existsByTenantIdAndEmail(UUID tenantId, String email);

    long countByTenantIdAndRoleAndActiveTrue(UUID tenantId, UserRole role);
}
