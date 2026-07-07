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
}
