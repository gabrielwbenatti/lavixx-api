package com.benattidev.lavixx.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benattidev.lavixx.dto.customer.CustomerRequest;
import com.benattidev.lavixx.dto.customer.CustomerResponse;
import com.benattidev.lavixx.entity.Customer;
import com.benattidev.lavixx.entity.Tenant;
import com.benattidev.lavixx.exception.NotFoundException;
import com.benattidev.lavixx.mapper.CustomerMapper;
import com.benattidev.lavixx.repository.CustomerRepository;
import com.benattidev.lavixx.security.SecurityUtils;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<CustomerResponse> list() {
        UUID tenantId = SecurityUtils.currentTenantId();
        return customerRepository.findAllByTenantId(tenantId).stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(UUID id) {
        return customerMapper.toResponse(loadOwned(id));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        UUID tenantId = SecurityUtils.currentTenantId();
        Customer customer = Customer.builder()
                .tenant(entityManager.getReference(Tenant.class, tenantId))
                .name(request.name())
                .document(blankToNull(request.document()))
                .phone(blankToNull(request.phone()))
                .build();
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = loadOwned(id);
        customer.setName(request.name());
        customer.setDocument(blankToNull(request.document()));
        customer.setPhone(blankToNull(request.phone()));
        return customerMapper.toResponse(customer);
    }

    @Transactional
    public void delete(UUID id) {
        Customer customer = loadOwned(id);
        customerRepository.delete(customer);
    }

    private Customer loadOwned(UUID id) {
        UUID tenantId = SecurityUtils.currentTenantId();
        return customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Cliente nao encontrado"));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
