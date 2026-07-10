package com.benattidev.lavixx.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benattidev.lavixx.dto.user.InviteResponse;
import com.benattidev.lavixx.dto.user.InviteUserRequest;
import com.benattidev.lavixx.dto.user.UserResponse;
import com.benattidev.lavixx.entity.Tenant;
import com.benattidev.lavixx.entity.User;
import com.benattidev.lavixx.entity.enums.UserRole;
import com.benattidev.lavixx.exception.BusinessException;
import com.benattidev.lavixx.exception.DuplicateResourceException;
import com.benattidev.lavixx.exception.NotFoundException;
import com.benattidev.lavixx.mapper.UserMapper;
import com.benattidev.lavixx.repository.UserRepository;
import com.benattidev.lavixx.security.SecurityUtils;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userRepository.findAllByTenantIdOrderByCreatedAtAsc(SecurityUtils.currentTenantId())
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    /** Convida um usuario: cria inativo, sem senha, com token de convite. */
    @Transactional
    public InviteResponse invite(InviteUserRequest request) {
        UUID tenantId = SecurityUtils.currentTenantId();
        if (userRepository.existsByTenantIdAndEmail(tenantId, request.email())
                || userRepository.findFirstByEmail(request.email()).isPresent()) {
            throw new DuplicateResourceException("Ja existe um usuario com este e-mail");
        }
        String token = newToken();
        User user = User.builder()
                .tenant(entityManager.getReference(Tenant.class, tenantId))
                .name(request.name())
                .email(request.email())
                .password(null)
                .active(false)
                .role(request.role())
                .inviteToken(token)
                .build();
        user = userRepository.save(user);
        return new InviteResponse(userMapper.toResponse(user), token);
    }

    /** Gera um novo token de convite para um usuario que ainda nao definiu a senha. */
    @Transactional
    public InviteResponse resendInvite(UUID id) {
        User user = loadOwned(id);
        if (user.getPassword() != null) {
            throw new BusinessException("Este usuario ja definiu a senha");
        }
        String token = newToken();
        user.setInviteToken(token);
        return new InviteResponse(userMapper.toResponse(user), token);
    }

    @Transactional
    public UserResponse updateRole(UUID id, UserRole role) {
        User user = loadOwned(id);
        // Impede rebaixar o ultimo administrador ativo (evita ficar sem admin).
        if (user.getRole() == UserRole.admin && role != UserRole.admin && user.isActive()) {
            ensureAnotherActiveAdminExists();
        }
        user.setRole(role);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse setActive(UUID id, boolean active) {
        User user = loadOwned(id);
        if (user.getPassword() == null) {
            throw new BusinessException("Convite ainda nao aceito: nao ha o que ativar/desativar");
        }
        if (!active) {
            if (user.getId().equals(SecurityUtils.currentUserId())) {
                throw new BusinessException("Voce nao pode desativar a si mesmo");
            }
            if (user.getRole() == UserRole.admin && user.isActive()) {
                ensureAnotherActiveAdminExists();
            }
        }
        user.setActive(active);
        return userMapper.toResponse(user);
    }

    @Transactional
    public void delete(UUID id) {
        User user = loadOwned(id);
        if (user.getId().equals(SecurityUtils.currentUserId())) {
            throw new BusinessException("Voce nao pode excluir a si mesmo");
        }
        if (user.getRole() == UserRole.admin && user.isActive()) {
            ensureAnotherActiveAdminExists();
        }
        userRepository.delete(user);
    }

    private void ensureAnotherActiveAdminExists() {
        long activeAdmins = userRepository.countByTenantIdAndRoleAndActiveTrue(
                SecurityUtils.currentTenantId(), UserRole.admin);
        if (activeAdmins <= 1) {
            throw new BusinessException("E necessario manter ao menos um administrador ativo");
        }
    }

    private String newToken() {
        return UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
    }

    private User loadOwned(UUID id) {
        return userRepository.findByIdAndTenantId(id, SecurityUtils.currentTenantId())
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));
    }
}
