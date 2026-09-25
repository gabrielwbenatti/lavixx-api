package com.benattidev.lavixx.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benattidev.lavixx.dto.common.PageResponse;
import com.benattidev.lavixx.dto.payment.PaymentRequest;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderItemRequest;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderItemResponse;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderRequest;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderFilter;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderResponse;
import com.benattidev.lavixx.dto.serviceorder.ServiceOrderStatsResponse;
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
    private final EntityManager entityManager;

    /**
     * Total da OS calculado no banco, com a mesma formula do ServiceOrderMapper:
     * base = subtotal - round(subtotal * fidelidade% / 100, 2); total = base + round(base * taxa% / 100, 2).
     */
    private static final String SUBTOTAL_JPQL =
            "(select coalesce(sum((i.unitPrice - i.discount) * i.quantity), 0) "
                    + "from ServiceOrderItem i where i.serviceOrder = o)";
    private static final String BASE_JPQL =
            "(" + SUBTOTAL_JPQL + " - round(" + SUBTOTAL_JPQL + " * coalesce(o.loyaltyRewardPercent, 0) / 100, 2))";
    private static final String TOTAL_JPQL =
            "(" + BASE_JPQL + " + round(" + BASE_JPQL + " * coalesce(o.serviceTax, 0) / 100, 2))";

    /** Listagem paginada; todos os filtros sao aplicados no banco. Mais recentes primeiro. */
    @Transactional(readOnly = true)
    public PageResponse<ServiceOrderResponse> list(ServiceOrderFilter filter, Pageable pageable) {
        OrderWhere where = buildWhere(filter);

        TypedQuery<Long> countQuery = entityManager.createQuery(
                "select count(o) from ServiceOrder o" + where.jpql(), Long.class);
        // Cliente e veiculo vem no mesmo SELECT; itens e pagamentos sao carregados em lote
        // (hibernate.default_batch_fetch_size), evitando uma consulta por ordem.
        TypedQuery<ServiceOrder> contentQuery = entityManager.createQuery(
                "select o from ServiceOrder o join fetch o.customer join fetch o.vehicle" + where.jpql()
                        + " order by o.issuedAt desc, o.createdAt desc",
                ServiceOrder.class);
        where.bind(countQuery);
        where.bind(contentQuery);

        List<ServiceOrder> orders = contentQuery
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
        Page<ServiceOrder> page = new PageImpl<>(orders, pageable, countQuery.getSingleResult());
        return PageResponse.of(page, serviceOrderMapper::toResponse);
    }

    /** Totais das ordens que atendem ao filtro, calculados no banco. */
    @Transactional(readOnly = true)
    public ServiceOrderStatsResponse stats(ServiceOrderFilter filter) {
        OrderWhere where = buildWhere(filter);

        TypedQuery<Object[]> countsQuery = entityManager.createQuery(
                "select count(o), coalesce(sum(case when o.status = :done then 1 else 0 end), 0)"
                        + " from ServiceOrder o" + where.jpql(),
                Object[].class);
        where.bind(countsQuery);
        countsQuery.setParameter("done", ServiceStatus.done);
        Object[] counts = countsQuery.getSingleResult();

        TypedQuery<BigDecimal> paidQuery = entityManager.createQuery(
                "select coalesce(sum(p.amount), 0) from Payment p join p.serviceOrder o"
                        + where.jpql() + " and o.status = :done",
                BigDecimal.class);
        where.bind(paidQuery);
        paidQuery.setParameter("done", ServiceStatus.done);

        return new ServiceOrderStatsResponse(
                ((Number) counts[0]).longValue(),
                ((Number) counts[1]).longValue(),
                paidQuery.getSingleResult());
    }

    /** Clausula WHERE (alias `o`) e seus parametros; sempre restrita ao tenant atual. */
    private record OrderWhere(String jpql, Map<String, Object> params) {
        void bind(TypedQuery<?> query) {
            params.forEach(query::setParameter);
        }
    }

    private OrderWhere buildWhere(ServiceOrderFilter filter) {
        StringBuilder where = new StringBuilder(" where o.tenant.id = :tenantId");
        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", SecurityUtils.currentTenantId());

        if (filter.status() != null) {
            where.append(" and o.status = :status");
            params.put("status", filter.status());
        }
        if (filter.customerId() != null) {
            where.append(" and o.customer.id = :customerId");
            params.put("customerId", filter.customerId());
        }
        if (filter.vehicleId() != null) {
            where.append(" and o.vehicle.id = :vehicleId");
            params.put("vehicleId", filter.vehicleId());
        }
        if (filter.fromDate() != null) {
            where.append(" and o.issuedAt >= :fromDate");
            params.put("fromDate", filter.fromDate());
        }
        if (filter.toDate() != null) {
            where.append(" and o.issuedAt <= :toDate");
            params.put("toDate", filter.toDate());
        }
        if (filter.finishedFrom() != null) {
            where.append(" and o.finishedAt >= :finishedFrom");
            params.put("finishedFrom", filter.finishedFrom());
        }
        if (filter.finishedTo() != null) {
            where.append(" and o.finishedAt <= :finishedTo");
            params.put("finishedTo", filter.finishedTo());
        }
        if (filter.minAmount() != null) {
            where.append(" and ").append(TOTAL_JPQL).append(" >= :minAmount");
            params.put("minAmount", filter.minAmount());
        }
        if (filter.maxAmount() != null) {
            where.append(" and ").append(TOTAL_JPQL).append(" <= :maxAmount");
            params.put("maxAmount", filter.maxAmount());
        }
        return new OrderWhere(where.toString(), params);
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
                .issuedAt(OffsetDateTime.now())
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

    /** Corrige a data de emissao da OS (uso administrativo: lancamento retroativo). */
    @Transactional
    public ServiceOrderResponse updateIssuedAt(UUID id, OffsetDateTime issuedAt) {
        ServiceOrder order = loadOwned(id);
        if (order.getFinishedAt() != null && issuedAt.isAfter(order.getFinishedAt())) {
            throw new BusinessException("A data de emissao nao pode ser posterior a data de finalizacao");
        }
        order.setIssuedAt(issuedAt);
        return serviceOrderMapper.toResponse(order);
    }

    /** Corrige a data de finalizacao de uma OS ja concluida/cancelada (uso administrativo). */
    @Transactional
    public ServiceOrderResponse updateFinishedAt(UUID id, OffsetDateTime finishedAt) {
        ServiceOrder order = loadOwned(id);
        if (order.getFinishedAt() == null) {
            throw new BusinessException("Esta ordem ainda nao foi finalizada");
        }
        if (finishedAt.isBefore(order.getIssuedAt())) {
            throw new BusinessException("A data de finalizacao nao pode ser anterior a data de emissao");
        }
        order.setFinishedAt(finishedAt);
        return serviceOrderMapper.toResponse(order);
    }

    /** Corrige a data de um pagamento ja registrado (uso administrativo). */
    @Transactional
    public ServiceOrderResponse updatePaymentDate(UUID orderId, UUID paymentId, OffsetDateTime paidAt) {
        UUID tenantId = SecurityUtils.currentTenantId();
        ServiceOrder order = loadOwned(orderId);
        Payment payment = paymentRepository.findByIdAndTenantId(paymentId, tenantId)
                .orElseThrow(() -> new NotFoundException("Pagamento nao encontrado"));
        if (!payment.getServiceOrder().getId().equals(orderId)) {
            throw new NotFoundException("Pagamento nao encontrado");
        }
        payment.setPaidAt(paidAt);
        return serviceOrderMapper.toResponse(order);
    }

    /** Reagenda uma OS ainda nao chegada (cliente pediu para mudar o horario, ou lancamento errado). */
    @Transactional
    public ServiceOrderResponse updateScheduledAt(UUID id, OffsetDateTime scheduledAt) {
        ServiceOrder order = loadOwned(id);
        if (order.getStatus() != ServiceStatus.scheduled) {
            throw new BusinessException(
                    "Somente ordens agendadas (aguardando chegada) podem ter o agendamento alterado");
        }
        order.setScheduledAt(scheduledAt);
        return serviceOrderMapper.toResponse(order);
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

        OffsetDateTime paidAt = request.paidAt() != null && SecurityUtils.isAdmin()
                ? request.paidAt()
                : OffsetDateTime.now();

        Payment payment = Payment.builder()
                .tenant(order.getTenant())
                .serviceOrder(order)
                .paymentMethod(method)
                .methodName(method.getName())
                .amount(request.amount())
                .paidAt(paidAt)
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
