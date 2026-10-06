package com.benattidev.lavixx.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.benattidev.lavixx.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Employee> findAllByTenantIdOrderByNameAsc(UUID tenantId);
}
