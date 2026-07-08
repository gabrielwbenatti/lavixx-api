package com.benattidev.lavixx.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benattidev.lavixx.dto.loyalty.LoyaltyStatusResponse;
import com.benattidev.lavixx.entity.Customer;
import com.benattidev.lavixx.entity.Tenant;
import com.benattidev.lavixx.entity.enums.ServiceStatus;
import com.benattidev.lavixx.exception.NotFoundException;
import com.benattidev.lavixx.repository.CustomerRepository;
import com.benattidev.lavixx.repository.ServiceOrderRepository;
import com.benattidev.lavixx.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final CustomerRepository customerRepository;
    private final ServiceOrderRepository serviceOrderRepository;

    @Transactional(readOnly = true)
    public LoyaltyStatusResponse statusFor(UUID customerId) {
        UUID tenantId = SecurityUtils.currentTenantId();
        Customer customer = customerRepository.findByIdAndTenantId(customerId, tenantId)
                .orElseThrow(() -> new NotFoundException("Cliente nao encontrado"));
        return computeStatus(customer, customer.getTenant());
    }

    /** Calcula a situação do cartão. Reutilizado no resgate de prêmio. */
    public LoyaltyStatusResponse computeStatus(Customer customer, Tenant tenant) {
        short target = tenant.getLoyaltyTarget();
        BigDecimal rewardPercent = tenant.getLoyaltyRewardPercent();
        int redeemed = customer.getLoyaltyRewardsRedeemed();

        long completed = serviceOrderRepository.countByTenantIdAndCustomerIdAndStatus(
                tenant.getId(), customer.getId(), ServiceStatus.done);

        if (!tenant.isLoyaltyEnabled() || target < 1) {
            return new LoyaltyStatusResponse(false, target, rewardPercent, completed, redeemed, 0, 0, 0);
        }

        int earned = (int) (completed / target);
        int available = Math.max(0, earned - redeemed);
        int stampsInCard = (int) (completed % target);
        int untilNext = target - stampsInCard;

        return new LoyaltyStatusResponse(
                true, target, rewardPercent, completed, redeemed, available, stampsInCard, untilNext);
    }
}
