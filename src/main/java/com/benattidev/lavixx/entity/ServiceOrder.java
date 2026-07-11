package com.benattidev.lavixx.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.benattidev.lavixx.entity.enums.ServiceStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "service_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceOrder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "service_status")
    private ServiceStatus status;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    /** Horario marcado com antecedencia para o cliente trazer o veiculo (status 'scheduled'). */
    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;

    /** Horario que o cliente informou que vira buscar o veiculo (independe do status). */
    @Column(name = "estimated_pickup_at")
    private OffsetDateTime estimatedPickupAt;

    /** Observacoes livres da OS (ex.: avarias, pedidos do cliente). */
    @Column(name = "observations", length = 1000)
    private String observations;

    /** Taxa de serviço (%) congelada nesta OS na criação; pode ser ajustada/zerada. */
    @Builder.Default
    @Column(name = "service_tax", nullable = false, precision = 5, scale = 2)
    private BigDecimal serviceTax = BigDecimal.ZERO;

    /** Desconto de fidelidade (%) aplicado a esta OS ao resgatar um prêmio (0 = nenhum). */
    @Builder.Default
    @Column(name = "loyalty_reward_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal loyaltyRewardPercent = BigDecimal.ZERO;

    @Builder.Default
    @OneToMany(mappedBy = "serviceOrder", fetch = FetchType.LAZY,
               cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceOrderItem> items = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "serviceOrder", fetch = FetchType.LAZY,
               cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();
}
