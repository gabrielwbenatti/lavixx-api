package com.benattidev.lavixx.dto.auth;

/** Dados exibidos na tela publica de aceite do convite (definir senha). */
public record InvitePreviewResponse(
        String name,
        String email,
        String tenantName) {
}
