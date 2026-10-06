package com.benattidev.lavixx.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import com.benattidev.lavixx.entity.Tenant;
import com.benattidev.lavixx.exception.BusinessException;
import com.benattidev.lavixx.exception.NotFoundException;
import com.benattidev.lavixx.repository.TenantRepository;
import com.benattidev.lavixx.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

/**
 * Relogio do estabelecimento logado. O servidor pode estar em qualquer fuso (na nuvem,
 * normalmente UTC); tudo que depende de "dia" (hoje, inicio/fim de um periodo) deve passar
 * por aqui em vez de {@code ZoneId.systemDefault()} / {@code LocalDate.now()}.
 */
@Component
@RequiredArgsConstructor
public class TenantTime {

    public static final String DEFAULT_ZONE = "America/Sao_Paulo";

    private final TenantRepository tenantRepository;

    /** Fuso do estabelecimento do usuario autenticado. */
    public ZoneId zone() {
        Tenant tenant = tenantRepository.findById(SecurityUtils.currentTenantId())
                .orElseThrow(() -> new NotFoundException("Estabelecimento nao encontrado"));
        return parse(tenant.getTimezone());
    }

    public LocalDate today() {
        return LocalDate.now(zone());
    }

    public OffsetDateTime startOfDay(LocalDate date, ZoneId zone) {
        return date.atStartOfDay(zone).toOffsetDateTime();
    }

    public OffsetDateTime endOfDay(LocalDate date, ZoneId zone) {
        return date.atTime(LocalTime.MAX).atZone(zone).toOffsetDateTime();
    }

    /** Converte um id IANA em {@link ZoneId}; cai no padrao se vier vazio/invalido. */
    public static ZoneId parseOrDefault(String id) {
        try {
            return parse(id);
        } catch (BusinessException e) {
            return ZoneId.of(DEFAULT_ZONE);
        }
    }

    /** Valida e converte um id IANA em {@link ZoneId}. */
    public static ZoneId parse(String id) {
        if (id == null || id.isBlank()) {
            return ZoneId.of(DEFAULT_ZONE);
        }
        String trimmed = id.trim();
        // So ids da base IANA (ex.: America/Manaus); recusa formas como "+03:00" ou "EST".
        if (!ZoneId.getAvailableZoneIds().contains(trimmed)) {
            throw new BusinessException("Fuso horario invalido: " + id);
        }
        return ZoneId.of(trimmed);
    }
}
