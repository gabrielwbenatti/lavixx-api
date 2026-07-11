package com.benattidev.lavixx.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benattidev.lavixx.dto.payment.PaymentRequest;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderItemRequest;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderItemResponse;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderRequest;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderResponse;
import com.benattidev.lavixx.dto.serviceorder.UpdateItemRequest;
import com.benattidev.lavixx.dto.loyalty.LoyaltyStatusResponse;
import com.benattidev.lavixx.entity.Customer;
import com.benattidev.lavixx.entity.Payment;
import com.benattidev.lavixx.entity.PaymentMethod;
import com.benattidev.lavixx.entity.Product;
import com.benattidev.lavixx.entity.ServiceOrder;
import com.benattidev.lavixx.entity.ServiceOrderItem;
import com.benattidev.lavixx.entity.Tenant;
import com.benattidev.lavixx.entity.Vehicle;
import com.benattidev.lavixx.entity.enums.ServiceStatus;
import com.benattidev.lavixx.exception.BusinessException;
import com.benattidev.lavixx.exception.NotFoundException;
import com.benattidev.lavixx.mapper.ServiceOrderMapper;
import com.benattidev.lavixx.repository.PaymentMethodRepository;
import com.benattidev.lavixx.repository.PaymentRepository;
import com.benattidev.lavixx.repository.ProductRepository;
import com.benattidev.lavixx.repository.ServiceOrderItemRepository;
import com.benattidev.lavixx.repository.ServiceOrderRepository;
import com.benattidev.lavixx.repository.ServiceRepository;
import com.benattidev.lavixx.repository.TenantRepository;
import com.benattidev.lavixx.repository.VehicleRepository;
import com.benattidev.lavixx.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceOrderService {

    private static final Map<ServiceStatus, Set<ServiceStatus>> VALID_TRANSITIONS = Map.of(
            ServiceStatus.scheduled,   Set.of(ServiceStatus.waiting, ServiceStatus.cancelled),
            ServiceStatus.waiting,     Set.of(ServiceStatus.in_progress, ServiceStatus.cancelled),
            ServiceStatus.in_progress, Set.of(ServiceStatus.done, ServiceStatus.cancelled),
            ServiceStatus.done,        Set.of(),
            ServiceStatus.cancelled,   Set.of()
    );

    private static final Set<ServiceStatus> DELETABLE_STATUSES =
            Set.of(ServiceStatus.scheduled, ServiceStatus.waiting);

    private static final Set<ServiceStatus> EDITABLE_STATUSES =
            Set.of(ServiceStatus.waiting, ServiceStatus.in_progress);

    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderItemRepository serviceOrderItemRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceRepository serviceRepository;
    private final ProductRepository productRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository paymentRepository;
    private final TenantRepository tenantRepository;
    private final LoyaltyService loyaltyService;
    private final ServiceOrderMapper serviceOrderMapper;

    @Transactional(readOnly = true)
    public List<ServiceOrderResponse> list(
            ServiceStatus status,
            UUID customerId,
            UUID vehicleId,
            OffsetDateTime fromDate,
            OffsetDateTime toDate,
            BigDecimal minAmount,
            BigDecimal maxAmount) {
        UUID tenantId = SecurityUtils.currentTenantId();
        List<ServiceOrder> orders;

        if (customerId != null) {
            orders = serviceOrderRepository.findAllByTenantIdAndCustomerId(tenantId, customerId);
        } else if (vehicleId != null) {
            orders = serviceOrderRepository.findAllByTenantIdAndVehicleId(tenantId, vehicleId);
        } else if (status != null) {
            orders = serviceOrderRepository.findAllByTenantIdAndStatus(tenantId, status);
        } else {
            orders = serviceOrderRepository.findAllByTenantId(tenantId);
        }

        // Força carregamento dos relacionamentos lazy
        orders.forEach(o -> {
            o.getCustomer().getId();
            o.getVehicle().getId();
            o.getItems().size();
            o.getPayments().size();
        });

        return orders.stream()
                .filter(o -> filterByDateRange(o, fromDate, toDate))
                .filter(o -> filterByAmount(o, minAmount, maxAmount))
                .map(serviceOrderMapper::toResponse)
                .toList();
    }

    private boolean filterByDateRange(ServiceOrder order, OffsetDateTime fromDate, OffsetDateTime toDate) {
        if (fromDate == null && toDate == null) return true;
        if (fromDate != null && order.getCreatedAt().isBefore(fromDate)) return false;
        if (toDate != null && order.getCreatedAt().isAfter(toDate)) return false;
        return true;
    }

    private boolean filterByAmount(ServiceOrder order, BigDecimal minAmount, BigDecimal maxAmount) {
        if (minAmount == null && maxAmount == null) return true;

        BigDecimal subtotal = order.getItems().stream()
                .map(item -> item.getUnitPrice()
                        .subtract(item.getDiscount())
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal loyaltyPct = order.getLoyaltyRewardPercent() != null
                ? order.getLoyaltyRewardPercent() : BigDecimal.ZERO;
        BigDecimal base = subtotal.subtract(subtotal
                .multiply(loyaltyPct)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));

        BigDecimal taxRate = order.getServiceTax() != null ? order.getServiceTax() : BigDecimal.ZERO;
        BigDecimal total = base.add(base
                .multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));

        if (minAmount != null && total.compareTo(minAmount) < 0) return false;
        if (maxAmount != null && total.compareTo(maxAmount) > 0) return false;
        return true;
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

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Estabelecimento nao encontrado"));
        BigDecimal serviceTax = tenant.getDefaultServiceTax() != null
                ? tenant.getDefaultServiceTax()
                : BigDecimal.ZERO;

        boolean isScheduled = request.scheduledAt() != null;

        ServiceOrder order = ServiceOrder.builder()
                .tenant(tenant)
                .customer(vehicle.getCustomer())
                .vehicle(vehicle)
                .status(isScheduled ? ServiceStatus.scheduled : ServiceStatus.waiting)
                .scheduledAt(request.scheduledAt())
                .serviceTax(serviceTax)
                .observations(normalizeObservations(request.observations()))
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
    public ServiceOrderResponse updateTax(UUID id, BigDecimal serviceTax) {
        ServiceOrder order = loadOwned(id);
        if (order.getStatus() == ServiceStatus.cancelled) {
            throw new BusinessException("Nao e possivel ajustar a taxa de uma ordem cancelada");
        }
        if (serviceTax.compareTo(BigDecimal.ZERO) < 0
                || serviceTax.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException("A taxa de servico deve estar entre 0% e 100%");
        }
        order.setServiceTax(serviceTax);
        return serviceOrderMapper.toResponse(order);
    }

    @Transactional
    public ServiceOrderResponse updateObservations(UUID id, String observations) {
        ServiceOrder order = loadOwned(id);
        order.setObservations(normalizeObservations(observations));
        return serviceOrderMapper.toResponse(order);
    }

    /** Trim; string vazia vira null para nao guardar observacao "em branco". */
    private String normalizeObservations(String observations) {
        if (observations == null) {
            return null;
        }
        String trimmed = observations.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Transactional
    public ServiceOrderResponse redeemLoyalty(UUID orderId) {
        ServiceOrder order = loadOwned(orderId);
        if (order.getStatus() == ServiceStatus.cancelled) {
            throw new BusinessException("Nao e possivel aplicar premio em uma ordem cancelada");
        }
        if (order.getLoyaltyRewardPercent().signum() > 0) {
            throw new BusinessException("Esta ordem ja tem um premio de fidelidade aplicado");
        }
        Customer customer = order.getCustomer();
        Tenant tenant = order.getTenant();
        LoyaltyStatusResponse status = loyaltyService.computeStatus(customer, tenant);
        if (!status.enabled()) {
            throw new BusinessException("O programa de fidelidade nao esta habilitado");
        }
        if (status.rewardsAvailable() < 1) {
            throw new BusinessException("O cliente ainda nao possui premio de fidelidade disponivel");
        }
        order.setLoyaltyRewardPercent(tenant.getLoyaltyRewardPercent());
        customer.setLoyaltyRewardsRedeemed(customer.getLoyaltyRewardsRedeemed() + 1);
        return serviceOrderMapper.toResponse(order);
    }

    @Transactional
    public ServiceOrderResponse removeLoyalty(UUID orderId) {
        ServiceOrder order = loadOwned(orderId);
        if (order.getLoyaltyRewardPercent().signum() > 0) {
            order.setLoyaltyRewardPercent(BigDecimal.ZERO);
            Customer customer = order.getCustomer();
            int redeemed = customer.getLoyaltyRewardsRedeemed();
            if (redeemed > 0) {
                customer.setLoyaltyRewardsRedeemed(redeemed - 1);
            }
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
        if (!DELETABLE_STATUSES.contains(order.getStatus())) {
            throw new BusinessException("Apenas ordens agendadas ou com status 'waiting' podem ser removidas");
        }
        serviceOrderRepository.delete(order);
    }

    @Transactional
    public ServiceOrderResponse updatePickupEstimate(UUID id, OffsetDateTime estimatedPickupAt) {
        ServiceOrder order = loadOwned(id);
        if (order.getStatus() == ServiceStatus.cancelled) {
            throw new BusinessException("Nao e possivel definir previsao de retirada em uma ordem cancelada");
        }
        order.setEstimatedPickupAt(estimatedPickupAt);
        return serviceOrderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<ServiceOrderResponse> listScheduled(OffsetDateTime from, OffsetDateTime to) {
        UUID tenantId = SecurityUtils.currentTenantId();
        List<ServiceOrder> orders = serviceOrderRepository
                .findAllByTenantIdAndStatusAndScheduledAtBetweenOrderByScheduledAtAsc(
                        tenantId, ServiceStatus.scheduled, from, to);
        orders.forEach(o -> {
            o.getCustomer().getId();
            o.getVehicle().getId();
        });
        return orders.stream().map(serviceOrderMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceOrderResponse> listPickupEstimates(OffsetDateTime from, OffsetDateTime to) {
        UUID tenantId = SecurityUtils.currentTenantId();
        List<ServiceOrder> orders = serviceOrderRepository
                .findAllByTenantIdAndEstimatedPickupAtBetweenOrderByEstimatedPickupAtAsc(tenantId, from, to);
        orders.forEach(o -> {
            o.getCustomer().getId();
            o.getVehicle().getId();
        });
        return orders.stream()
                .filter(o -> o.getStatus() != ServiceStatus.cancelled)
                .map(serviceOrderMapper::toResponse)
                .toList();
    }

    @Transactional
    public ServiceOrderResponse addPayment(UUID orderId, PaymentRequest request) {
        UUID tenantId = SecurityUtils.currentTenantId();
        ServiceOrder order = loadOwned(orderId);
        if (order.getStatus() == ServiceStatus.cancelled) {
            throw new BusinessException("Nao e possivel registrar pagamento em uma ordem cancelada");
        }
        PaymentMethod method = paymentMethodRepository
                .findByIdAndTenantId(request.paymentMethodId(), tenantId)
                .orElseThrow(() -> new NotFoundException("Forma de pagamento nao encontrada"));

        Payment payment = Payment.builder()
                .tenant(order.getTenant())
                .serviceOrder(order)
                .paymentMethod(method)
                .methodName(method.getName())
                .amount(request.amount())
                .paidAt(OffsetDateTime.now())
                .build();
        order.getPayments().add(payment);
        return serviceOrderMapper.toResponse(order);
    }

    @Transactional
    public ServiceOrderResponse removePayment(UUID orderId, UUID paymentId) {
        UUID tenantId = SecurityUtils.currentTenantId();
        ServiceOrder order = loadOwned(orderId);
        Payment payment = paymentRepository.findByIdAndTenantId(paymentId, tenantId)
                .orElseThrow(() -> new NotFoundException("Pagamento nao encontrado"));
        if (!payment.getServiceOrder().getId().equals(orderId)) {
            throw new NotFoundException("Pagamento nao encontrado");
        }
        order.getPayments().removeIf(p -> p.getId().equals(payment.getId()));
        return serviceOrderMapper.toResponse(order);
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
        boolean hasService = request.serviceId() != null;
        boolean hasProduct = request.productId() != null;
        if (hasService == hasProduct) {
            throw new BusinessException("Informe exatamente um servico ou um produto para o item");
        }

        BigDecimal discount = request.discount() != null ? request.discount() : BigDecimal.ZERO;
        Short quantity = request.quantity() != null ? request.quantity() : 1;

        ServiceOrderItem.ServiceOrderItemBuilder builder = ServiceOrderItem.builder()
                .tenant(order.getTenant())
                .serviceOrder(order)
                .discount(discount)
                .quantity(quantity);

        if (hasService) {
            com.benattidev.lavixx.entity.Service service =
                    serviceRepository.findByIdAndTenantId(request.serviceId(), tenantId)
                            .orElseThrow(() -> new NotFoundException("Servico nao encontrado"));
            validateDiscount(service.getPrice(), discount);
            builder.service(service).name(service.getName()).unitPrice(service.getPrice());
        } else {
            Product product = productRepository.findByIdAndTenantId(request.productId(), tenantId)
                    .orElseThrow(() -> new NotFoundException("Produto nao encontrado"));
            validateDiscount(product.getPrice(), discount);
            builder.product(product).name(product.getName()).unitPrice(product.getPrice());
        }

        return builder.build();
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
