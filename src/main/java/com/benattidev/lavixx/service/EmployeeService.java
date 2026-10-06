package com.benattidev.lavixx.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benattidev.lavixx.dto.employee.EmployeeRequest;
import com.benattidev.lavixx.dto.employee.EmployeeResponse;
import com.benattidev.lavixx.entity.Employee;
import com.benattidev.lavixx.entity.Tenant;
import com.benattidev.lavixx.exception.BusinessException;
import com.benattidev.lavixx.exception.NotFoundException;
import com.benattidev.lavixx.mapper.EmployeeMapper;
import com.benattidev.lavixx.repository.EmployeeRepository;
import com.benattidev.lavixx.repository.ServiceOrderItemRepository;
import com.benattidev.lavixx.security.SecurityUtils;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ServiceOrderItemRepository serviceOrderItemRepository;
    private final EmployeeMapper employeeMapper;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<EmployeeResponse> list() {
        return employeeRepository
                .findAllByTenantIdOrderByNameAsc(SecurityUtils.currentTenantId()).stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        UUID tenantId = SecurityUtils.currentTenantId();
        Employee employee = Employee.builder()
                .tenant(entityManager.getReference(Tenant.class, tenantId))
                .name(request.name().trim())
                .active(request.active() == null || request.active())
                .build();
        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeResponse update(UUID id, EmployeeRequest request) {
        Employee employee = loadOwned(id);
        employee.setName(request.name().trim());
        if (request.active() != null) {
            employee.setActive(request.active());
        }
        return employeeMapper.toResponse(employee);
    }

    @Transactional
    public void delete(UUID id) {
        Employee employee = loadOwned(id);
        if (serviceOrderItemRepository.existsByEmployeeId(id)) {
            throw new BusinessException(
                    "Este funcionario ja foi vinculado a servicos; desative-o em vez de excluir");
        }
        employeeRepository.delete(employee);
    }

    private Employee loadOwned(UUID id) {
        return employeeRepository.findByIdAndTenantId(id, SecurityUtils.currentTenantId())
                .orElseThrow(() -> new NotFoundException("Funcionario nao encontrado"));
    }
}
