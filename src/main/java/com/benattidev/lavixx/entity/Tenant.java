package com.benattidev.lavixx.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "document", nullable = false, length = 14, unique = true)
    private String document;

    @Column(name = "operating_hours_start", length = 5)
    private String operatingHoursStart;

    @Column(name = "operating_hours_end", length = 5)
    private String operatingHoursEnd;

    @Column(name = "default_service_tax")
    private java.math.BigDecimal defaultServiceTax;

    /** Programa de fidelidade (cartão) habilitado. */
    @Builder.Default
    @Column(name = "loyalty_enabled", nullable = false)
    private boolean loyaltyEnabled = false;

    /** Nº de lavagens concluídas para ganhar um prêmio. */
    @Builder.Default
    @Column(name = "loyalty_target", nullable = false)
    private short loyaltyTarget = 10;

    /** Prêmio: % de desconto aplicado na OS ao resgatar (100 = grátis). */
    @Builder.Default
    @Column(name = "loyalty_reward_percent", nullable = false)
    private java.math.BigDecimal loyaltyRewardPercent = java.math.BigDecimal.valueOf(100);
}
