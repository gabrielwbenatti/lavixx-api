package com.benattidev.lavixx.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benattidev.lavixx.dto.tenant.TenantResponse;
import com.benattidev.lavixx.dto.tenant.TenantUpdateRequest;
import com.benattidev.lavixx.entity.Tenant;
import com.benattidev.lavixx.exception.NotFoundException;
import com.benattidev.lavixx.repository.TenantRepository;
import com.benattidev.lavixx.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public TenantResponse getCurrentTenant() {
        UUID tenantId = SecurityUtils.currentTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Estabelecimento não encontrado"));
        return toResponse(tenant);
    }

    @Transactional
    public TenantResponse updateCurrent(TenantUpdateRequest request) {
        UUID tenantId = SecurityUtils.currentTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Estabelecimento não encontrado"));

        if (request.name() != null) {
            tenant.setName(request.name());
        }
        if (request.operatingHoursStart() != null) {
            tenant.setOperatingHoursStart(request.operatingHoursStart());
        }
        if (request.operatingHoursEnd() != null) {
            tenant.setOperatingHoursEnd(request.operatingHoursEnd());
        }
        if (request.defaultServiceTax() != null) {
            tenant.setDefaultServiceTax(request.defaultServiceTax());
        }
        if (request.loyaltyEnabled() != null) {
            tenant.setLoyaltyEnabled(request.loyaltyEnabled());
        }
        if (request.loyaltyTarget() != null) {
            if (request.loyaltyTarget() < 1) {
                throw new com.benattidev.lavixx.exception.BusinessException(
                        "A meta de fidelidade deve ser de pelo menos 1 lavagem");
            }
            tenant.setLoyaltyTarget(request.loyaltyTarget());
        }
        if (request.loyaltyRewardPercent() != null) {
            BigDecimal pct = request.loyaltyRewardPercent();
            if (pct.compareTo(BigDecimal.ZERO) < 0 || pct.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new com.benattidev.lavixx.exception.BusinessException(
                        "O prêmio de fidelidade deve estar entre 0% e 100%");
            }
            tenant.setLoyaltyRewardPercent(pct);
        }

        tenant = tenantRepository.save(tenant);
        return toResponse(tenant);
    }

    private TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getDocument(),
                tenant.getOperatingHoursStart(),
                tenant.getOperatingHoursEnd(),
                tenant.getDefaultServiceTax(),
                tenant.isLoyaltyEnabled(),
                tenant.getLoyaltyTarget(),
                tenant.getLoyaltyRewardPercent());
    }
}
