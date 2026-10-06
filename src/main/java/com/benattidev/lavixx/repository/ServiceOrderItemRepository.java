package com.benattidev.lavixx.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.benattidev.lavixx.entity.ServiceOrderItem;

public interface ServiceOrderItemRepository extends JpaRepository<ServiceOrderItem, UUID> {

    Optional<ServiceOrderItem> findByIdAndTenantId(UUID id, UUID tenantId);

    /** O funcionario ja foi vinculado a algum item de OS? */
    @Query("select count(i) > 0 from ServiceOrderItem i join i.employees e where e.id = :employeeId")
    boolean existsByEmployeeId(@Param("employeeId") UUID employeeId);
}
