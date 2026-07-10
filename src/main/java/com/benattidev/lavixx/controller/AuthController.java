package com.benattidev.lavixx.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.benattidev.lavixx.dto.auth.AcceptInviteRequest;
import com.benattidev.lavixx.dto.auth.InvitePreviewResponse;
import com.benattidev.lavixx.dto.auth.LoginRequest;
import com.benattidev.lavixx.dto.auth.LoginResponse;
import com.benattidev.lavixx.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/invite/{token}")
    public ResponseEntity<InvitePreviewResponse> previewInvite(@PathVariable String token) {
        return ResponseEntity.ok(authService.previewInvite(token));
    }

    @PostMapping("/accept-invite")
    public ResponseEntity<LoginResponse> acceptInvite(@Valid @RequestBody AcceptInviteRequest request) {
        return ResponseEntity.ok(authService.acceptInvite(request));
    }
}
