package com.benattidev.lavixx.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benattidev.lavixx.dto.service.ServiceRequest;
import com.benattidev.lavixx.dto.service.ServiceResponse;
import com.benattidev.lavixx.entity.Tenant;
import com.benattidev.lavixx.exception.NotFoundException;
import com.benattidev.lavixx.mapper.ServiceMapper;
import com.benattidev.lavixx.repository.ServiceRepository;
import com.benattidev.lavixx.security.SecurityUtils;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<ServiceResponse> list() {
        return serviceRepository.findAllByTenantId(SecurityUtils.currentTenantId()).stream()
                .map(serviceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceResponse findById(UUID id) {
        return serviceMapper.toResponse(loadOwned(id));
    }

    @Transactional
    public ServiceResponse create(ServiceRequest request) {
        UUID tenantId = SecurityUtils.currentTenantId();
        com.benattidev.lavixx.entity.Service service = com.benattidev.lavixx.entity.Service.builder()
                .tenant(entityManager.getReference(Tenant.class, tenantId))
                .name(request.name())
                .price(request.price())
                .build();
        return serviceMapper.toResponse(serviceRepository.save(service));
    }

    @Transactional
    public ServiceResponse update(UUID id, ServiceRequest request) {
        com.benattidev.lavixx.entity.Service service = loadOwned(id);
        service.setName(request.name());
        service.setPrice(request.price());
        return serviceMapper.toResponse(service);
    }

    @Transactional
    public void delete(UUID id) {
        serviceRepository.delete(loadOwned(id));
    }

    private com.benattidev.lavixx.entity.Service loadOwned(UUID id) {
        return serviceRepository.findByIdAndTenantId(id, SecurityUtils.currentTenantId())
                .orElseThrow(() -> new NotFoundException("Servico nao encontrado"));
    }
}
