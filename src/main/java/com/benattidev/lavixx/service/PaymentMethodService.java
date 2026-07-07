package com.benattidev.lavixx.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benattidev.lavixx.dto.paymentmethod.PaymentMethodRequest;
import com.benattidev.lavixx.dto.paymentmethod.PaymentMethodResponse;
import com.benattidev.lavixx.entity.PaymentMethod;
import com.benattidev.lavixx.entity.Tenant;
import com.benattidev.lavixx.exception.BusinessException;
import com.benattidev.lavixx.exception.NotFoundException;
import com.benattidev.lavixx.mapper.PaymentMethodMapper;
import com.benattidev.lavixx.repository.PaymentMethodRepository;
import com.benattidev.lavixx.repository.PaymentRepository;
import com.benattidev.lavixx.security.SecurityUtils;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    /** Formas de pagamento criadas para todo novo tenant. */
    public static final List<String> DEFAULT_METHODS =
            List.of("Dinheiro", "PIX", "Cartao de credito", "Cartao de debito");

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMethodMapper paymentMethodMapper;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> list() {
        return paymentMethodRepository
                .findAllByTenantIdOrderByNameAsc(SecurityUtils.currentTenantId()).stream()
                .map(paymentMethodMapper::toResponse)
                .toList();
    }

    @Transactional
    public PaymentMethodResponse create(PaymentMethodRequest request) {
        UUID tenantId = SecurityUtils.currentTenantId();
        PaymentMethod method = PaymentMethod.builder()
                .tenant(entityManager.getReference(Tenant.class, tenantId))
                .name(request.name())
                .active(request.active() == null || request.active())
                .build();
        return paymentMethodMapper.toResponse(paymentMethodRepository.save(method));
    }

    @Transactional
    public PaymentMethodResponse update(UUID id, PaymentMethodRequest request) {
        PaymentMethod method = loadOwned(id);
        method.setName(request.name());
        if (request.active() != null) {
            method.setActive(request.active());
        }
        return paymentMethodMapper.toResponse(method);
    }

    @Transactional
    public void delete(UUID id) {
        PaymentMethod method = loadOwned(id);
        if (paymentRepository.existsByPaymentMethodId(id)) {
            throw new BusinessException(
                    "Esta forma de pagamento ja foi usada; desative-a em vez de excluir");
        }
        paymentMethodRepository.delete(method);
    }

    /** Cria as formas padrao para um tenant recem-criado. */
    @Transactional
    public void seedDefaults(Tenant tenant) {
        for (String name : DEFAULT_METHODS) {
            paymentMethodRepository.save(PaymentMethod.builder()
                    .tenant(tenant)
                    .name(name)
                    .active(true)
                    .build());
        }
    }

    private PaymentMethod loadOwned(UUID id) {
        return paymentMethodRepository.findByIdAndTenantId(id, SecurityUtils.currentTenantId())
                .orElseThrow(() -> new NotFoundException("Forma de pagamento nao encontrada"));
    }
}
