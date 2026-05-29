package com.benattidev.lavixx.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benattidev.lavixx.dto.serviceorder.ServiceOrderItemRequest;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderItemResponse;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderRequest;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderResponse;
import com.benattidev.lavixx.dto.serviceorder.UpdateItemRequest;
import com.benattidev.lavixx.entity.ServiceOrder;
import com.benattidev.lavixx.entity.ServiceOrderItem;
import com.benattidev.lavixx.entity.Tenant;
import com.benattidev.lavixx.entity.Vehicle;
import com.benattidev.lavixx.entity.enums.ServiceStatus;
import com.benattidev.lavixx.exception.BusinessException;
import com.benattidev.lavixx.exception.NotFoundException;
import com.benattidev.lavixx.mapper.ServiceOrderMapper;
import com.benattidev.lavixx.repository.ServiceOrderItemRepository;
import com.benattidev.lavixx.repository.ServiceOrderRepository;
import com.benattidev.lavixx.repository.ServiceRepository;
import com.benattidev.lavixx.repository.VehicleRepository;
import com.benattidev.lavixx.security.SecurityUtils;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceOrderService {

    private static final Map<ServiceStatus, Set<ServiceStatus>> VALID_TRANSITIONS = Map.of(
            ServiceStatus.waiting,     Set.of(ServiceStatus.in_progress, ServiceStatus.cancelled),
            ServiceStatus.in_progress, Set.of(ServiceStatus.done, ServiceStatus.cancelled),
            ServiceStatus.done,        Set.of(),
            ServiceStatus.cancelled,   Set.of()
    );

    private static final Set<ServiceStatus> EDITABLE_STATUSES =
            Set.of(ServiceStatus.waiting, ServiceStatus.in_progress);

    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderItemRepository serviceOrderItemRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceOrderMapper serviceOrderMapper;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<ServiceOrderResponse> list(ServiceStatus status) {
        UUID tenantId = SecurityUtils.currentTenantId();
        List<ServiceOrder> orders = status != null
                ? serviceOrderRepository.findAllByTenantIdAndStatus(tenantId, status)
                : serviceOrderRepository.findAllByTenantId(tenantId);
        return orders.stream().map(serviceOrderMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ServiceOrderResponse findById(UUID id) {
        return serviceOrderMapper.toResponse(loadOwned(id));
    }

    @Transactional
    public ServiceOrderResponse create(ServiceOrderRequest request) {
        UUID tenantId = SecurityUtils.currentTenantId();

        Vehicle vehicle = vehicleRepository.findByIdAndTenantId(request.vehicleId(), tenantId)
                .orElseThrow(() -> new NotFoundException("Veiculo nao encontrado"));

        ServiceOrder order = ServiceOrder.builder()
                .tenant(entityManager.getReference(Tenant.class, tenantId))
                .customer(vehicle.getCustomer())
                .vehicle(vehicle)
                .status(ServiceStatus.waiting)
                .build();

        if (request.items() != null) {
            for (ServiceOrderItemRequest itemReq : request.items()) {
                order.getItems().add(buildItem(order, itemReq, tenantId));
            }
        }

        return serviceOrderMapper.toResponse(serviceOrderRepository.save(order));
    }

    @Transactional
    public ServiceOrderResponse updateStatus(UUID id, ServiceStatus newStatus) {
        ServiceOrder order = loadOwned(id);
        ServiceStatus current = order.getStatus();

        if (!VALID_TRANSITIONS.get(current).contains(newStatus)) {
            throw new BusinessException(
                    "Transicao de status invalida: " + current + " -> " + newStatus);
        }

        order.setStatus(newStatus);
        if (newStatus == ServiceStatus.done || newStatus == ServiceStatus.cancelled) {
            order.setFinishedAt(OffsetDateTime.now());
        }

        return serviceOrderMapper.toResponse(order);
    }

    @Transactional
    public ServiceOrderResponse addItem(UUID orderId, ServiceOrderItemRequest request) {
        UUID tenantId = SecurityUtils.currentTenantId();
        ServiceOrder order = loadOwned(orderId);
        validateEditable(order);
        order.getItems().add(buildItem(order, request, tenantId));
        return serviceOrderMapper.toResponse(order);
    }

    @Transactional
    public ServiceOrderItemResponse updateItem(UUID orderId, UUID itemId, UpdateItemRequest request) {
        UUID tenantId = SecurityUtils.currentTenantId();
        loadOwned(orderId);
        ServiceOrderItem item = loadOwnedItem(itemId, orderId, tenantId);
        validateEditable(item.getServiceOrder());

        if (request.discount() != null) {
            validateDiscount(item.getUnitPrice(), request.discount());
            item.setDiscount(request.discount());
        }
        if (request.quantity() != null) {
            item.setQuantity(request.quantity());
        }

        return serviceOrderMapper.toItemResponse(item);
    }

    @Transactional
    public ServiceOrderResponse removeItem(UUID orderId, UUID itemId) {
        UUID tenantId = SecurityUtils.currentTenantId();
        ServiceOrder order = loadOwned(orderId);
        validateEditable(order);
        ServiceOrderItem item = loadOwnedItem(itemId, orderId, tenantId);
        order.getItems().removeIf(i -> i.getId().equals(item.getId()));
        return serviceOrderMapper.toResponse(order);
    }

    @Transactional
    public void delete(UUID id) {
        ServiceOrder order = loadOwned(id);
        if (order.getStatus() != ServiceStatus.waiting) {
            throw new BusinessException("Apenas ordens com status 'waiting' podem ser removidas");
        }
        serviceOrderRepository.delete(order);
    }

    private ServiceOrder loadOwned(UUID id) {
        return serviceOrderRepository.findByIdAndTenantId(id, SecurityUtils.currentTenantId())
                .orElseThrow(() -> new NotFoundException("Ordem de servico nao encontrada"));
    }

    private ServiceOrderItem loadOwnedItem(UUID itemId, UUID orderId, UUID tenantId) {
        ServiceOrderItem item = serviceOrderItemRepository.findByIdAndTenantId(itemId, tenantId)
                .orElseThrow(() -> new NotFoundException("Item nao encontrado"));
        if (!item.getServiceOrder().getId().equals(orderId)) {
            throw new NotFoundException("Item nao encontrado");
        }
        return item;
    }

    private ServiceOrderItem buildItem(ServiceOrder order, ServiceOrderItemRequest request, UUID tenantId) {
        com.benattidev.lavixx.entity.Service service =
                serviceRepository.findByIdAndTenantId(request.serviceId(), tenantId)
                        .orElseThrow(() -> new NotFoundException("Servico nao encontrado"));

        BigDecimal discount = request.discount() != null ? request.discount() : BigDecimal.ZERO;
        Short quantity = request.quantity() != null ? request.quantity() : 1;

        validateDiscount(service.getPrice(), discount);

        return ServiceOrderItem.builder()
                .tenant(order.getTenant())
                .serviceOrder(order)
                .service(service)
                .name(service.getName())
                .unitPrice(service.getPrice())
                .discount(discount)
                .quantity(quantity)
                .build();
    }

    private void validateDiscount(BigDecimal unitPrice, BigDecimal discount) {
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Desconto nao pode ser negativo");
        }
        if (discount.compareTo(unitPrice) > 0) {
            throw new BusinessException("Desconto nao pode ser maior que o preco unitario");
        }
    }

    private void validateEditable(ServiceOrder order) {
        if (!EDITABLE_STATUSES.contains(order.getStatus())) {
            throw new BusinessException(
                    "Nao e possivel alterar itens de uma ordem com status '" + order.getStatus() + "'");
        }
    }
}
