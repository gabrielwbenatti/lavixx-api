package com.benattidev.lavixx.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benattidev.lavixx.dto.common.PageParams;
import com.benattidev.lavixx.dto.common.PageResponse;
import com.benattidev.lavixx.dto.vehicle.VehicleRequest;
import com.benattidev.lavixx.dto.vehicle.VehicleResponse;
import com.benattidev.lavixx.entity.Customer;
import com.benattidev.lavixx.entity.Tenant;
import com.benattidev.lavixx.entity.Vehicle;
import com.benattidev.lavixx.entity.enums.VehicleType;
import com.benattidev.lavixx.exception.BusinessException;
import com.benattidev.lavixx.exception.NotFoundException;
import com.benattidev.lavixx.mapper.VehicleMapper;
import com.benattidev.lavixx.repository.CustomerRepository;
import com.benattidev.lavixx.repository.VehicleRepository;
import com.benattidev.lavixx.security.SecurityUtils;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private static final Set<VehicleType> PLATE_REQUIRED = Set.of(VehicleType.car, VehicleType.motorcycle);
    private static final Set<VehicleType> IDENTIFIER_REQUIRED = Set.of(VehicleType.boat, VehicleType.bicycle);

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final VehicleMapper vehicleMapper;
    private final EntityManager entityManager;

    /**
     * Listagem paginada. `search` busca por placa, apelido, fabricante, modelo, identificador
     * ou nome do cliente; `customerId` restringe aos veiculos de um cliente.
     */
    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> list(String search, UUID customerId, Pageable pageable) {
        UUID tenantId = SecurityUtils.currentTenantId();
        String term = PageParams.search(search);
        Page<Vehicle> page;
        if (customerId != null) {
            page = vehicleRepository.findPageByTenantIdAndCustomerId(tenantId, customerId, pageable);
        } else if (term != null) {
            page = vehicleRepository.search(tenantId, PageParams.likePattern(term),
                    PageParams.platePattern(term), pageable);
        } else {
            page = vehicleRepository.findPageByTenantId(tenantId, pageable);
        }
        return PageResponse.of(page, vehicleMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public VehicleResponse findById(UUID id) {
        return vehicleMapper.toResponse(loadOwned(id));
    }

    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        UUID tenantId = SecurityUtils.currentTenantId();
        validate(request);
        Customer customer = customerRepository.findByIdAndTenantId(request.customerId(), tenantId)
                .orElseThrow(() -> new NotFoundException("Cliente nao encontrado"));

        Vehicle vehicle = Vehicle.builder()
                .tenant(entityManager.getReference(Tenant.class, tenantId))
                .customer(customer)
                .type(request.type())
                .plate(normalize(request.plate()))
                .identifier(normalize(request.identifier()))
                .nickname(normalize(request.nickname()))
                .manufacturer(normalize(request.manufacturer()))
                .model(normalize(request.model()))
                .color(normalize(request.color()))
                .year(request.year())
                .build();
        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public VehicleResponse update(UUID id, VehicleRequest request) {
        UUID tenantId = SecurityUtils.currentTenantId();
        validate(request);
        Vehicle vehicle = loadOwned(id);

        if (!vehicle.getCustomer().getId().equals(request.customerId())) {
            Customer newCustomer = customerRepository.findByIdAndTenantId(request.customerId(), tenantId)
                    .orElseThrow(() -> new NotFoundException("Cliente nao encontrado"));
            vehicle.setCustomer(newCustomer);
        }

        vehicle.setType(request.type());
        vehicle.setPlate(normalize(request.plate()));
        vehicle.setIdentifier(normalize(request.identifier()));
        vehicle.setNickname(normalize(request.nickname()));
        vehicle.setManufacturer(normalize(request.manufacturer()));
        vehicle.setModel(normalize(request.model()));
        vehicle.setColor(normalize(request.color()));
        vehicle.setYear(request.year());
        return vehicleMapper.toResponse(vehicle);
    }

    @Transactional
    public void delete(UUID id) {
        vehicleRepository.delete(loadOwned(id));
    }

    private Vehicle loadOwned(UUID id) {
        return vehicleRepository.findByIdAndTenantId(id, SecurityUtils.currentTenantId())
                .orElseThrow(() -> new NotFoundException("Veiculo nao encontrado"));
    }

    private void validate(VehicleRequest request) {
        boolean plateBlank = isBlank(request.plate());
        boolean idBlank = isBlank(request.identifier());

        if (PLATE_REQUIRED.contains(request.type()) && plateBlank) {
            throw new BusinessException("Placa e obrigatoria para veiculos do tipo " + request.type());
        }
        if (IDENTIFIER_REQUIRED.contains(request.type()) && idBlank) {
            throw new BusinessException("Identificador e obrigatorio para veiculos do tipo " + request.type());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalize(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
