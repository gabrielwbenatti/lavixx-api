package com.benattidev.lavixx.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.benattidev.lavixx.entity.User;
import com.benattidev.lavixx.entity.enums.UserRole;

import lombok.Getter;

@Getter
public class AuthenticatedUser implements UserDetails {

    private final UUID userId;
    private final UUID tenantId;
    private final String email;
    private final String password;
    private final UserRole role;
    private final boolean active;

    public AuthenticatedUser(User user) {
        this.userId = user.getId();
        this.tenantId = user.getTenant().getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.active = user.isActive();
    }

    public AuthenticatedUser(UUID userId, UUID tenantId, String email, UserRole role) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.email = email;
        this.password = null;
        this.role = role;
        this.active = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name().toUpperCase()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
