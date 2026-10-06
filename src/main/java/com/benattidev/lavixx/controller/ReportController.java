package com.benattidev.lavixx.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.benattidev.lavixx.dto.report.ReportSummaryResponse;
import com.benattidev.lavixx.service.ReportService;
import com.benattidev.lavixx.service.TenantTime;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final TenantTime tenantTime;

    @GetMapping("/summary")
    public ResponseEntity<ReportSummaryResponse> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        // "Hoje" no fuso do estabelecimento, nao no do servidor.
        LocalDate today = tenantTime.today();
        LocalDate effectiveFrom = from != null ? from : today;
        LocalDate effectiveTo = to != null ? to : today;
        return ResponseEntity.ok(reportService.summary(effectiveFrom, effectiveTo));
    }
}
