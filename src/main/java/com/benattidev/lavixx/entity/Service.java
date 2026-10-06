package com.benattidev.lavixx.entity;

import java.math.BigDecimal;

import com.benattidev.lavixx.entity.enums.VehicleSize;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Preco padrao: vale quando o veiculo nao tem porte ou nao ha preco para o porte dele. */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "price_small", precision = 10, scale = 2)
    private BigDecimal priceSmall;

    @Column(name = "price_medium", precision = 10, scale = 2)
    private BigDecimal priceMedium;

    @Column(name = "price_large", precision = 10, scale = 2)
    private BigDecimal priceLarge;

    /** Preco de tabela para o porte informado (ou o padrao, se nao houver preco especifico). */
    public BigDecimal priceFor(VehicleSize size) {
        BigDecimal specific = size == null ? null : switch (size) {
            case small -> priceSmall;
            case medium -> priceMedium;
            case large -> priceLarge;
        };
        return specific != null ? specific : price;
    }
}
