package com.benattidev.lavixx.dto.loyalty;

import java.math.BigDecimal;

/** Situação do cartão-fidelidade de um cliente. */
public record LoyaltyStatusResponse(
        boolean enabled,
        short target,
        BigDecimal rewardPercent,
        long completedWashes,
        int rewardsRedeemed,
        int rewardsAvailable,
        int stampsInCurrentCard,
        int washesUntilNextReward) {
}
