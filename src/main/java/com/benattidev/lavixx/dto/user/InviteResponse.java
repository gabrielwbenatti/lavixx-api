package com.benattidev.lavixx.dto.user;

/**
 * Resposta ao convidar (ou reenviar convite): o usuario criado + o token bruto,
 * para o front montar o link compartilhavel (ex.: /definir-senha?token=...).
 */
public record InviteResponse(
        UserResponse user,
        String inviteToken) {
}
