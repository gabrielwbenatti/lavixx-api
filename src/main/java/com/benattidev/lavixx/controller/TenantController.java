package com.benattidev.lavixx.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.benattidev.lavixx.dto.tenant.TenantRegistrationRequest;
import com.benattidev.lavixx.dto.tenant.TenantRegistrationResponse;
import com.benattidev.lavixx.dto.tenant.TenantResponse;
import com.benattidev.lavixx.dto.tenant.TenantUpdateRequest;
import com.benattidev.lavixx.service.TenantRegistrationService;
import com.benattidev.lavixx.service.TenantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantRegistrationService tenantRegistrationService;
    private final TenantService tenantService;

    @PostMapping
    public ResponseEntity<TenantRegistrationResponse> register(@Valid @RequestBody TenantRegistrationRequest request) {
        TenantRegistrationResponse response = tenantRegistrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<TenantResponse> getCurrent() {
        return ResponseEntity.ok(tenantService.getCurrentTenant());
    }

    @PatchMapping("/me")
    public ResponseEntity<TenantResponse> updateCurrent(@Valid @RequestBody TenantUpdateRequest request) {
        return ResponseEntity.ok(tenantService.updateCurrent(request));
    }
}
