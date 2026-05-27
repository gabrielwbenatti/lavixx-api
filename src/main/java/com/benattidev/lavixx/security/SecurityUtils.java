package com.benattidev.lavixx.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.benattidev.lavixx.exception.BusinessException;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthenticatedUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new BusinessException("Usuario nao autenticado");
        }
        return user;
    }

    public static UUID currentTenantId() {
        return currentUser().getTenantId();
    }

    public static UUID currentUserId() {
        return currentUser().getUserId();
    }
}
