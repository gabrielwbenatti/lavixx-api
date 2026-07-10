package com.benattidev.lavixx.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benattidev.lavixx.dto.auth.AcceptInviteRequest;
import com.benattidev.lavixx.dto.auth.InvitePreviewResponse;
import com.benattidev.lavixx.dto.auth.LoginRequest;
import com.benattidev.lavixx.dto.auth.LoginResponse;
import com.benattidev.lavixx.entity.User;
import com.benattidev.lavixx.exception.NotFoundException;
import com.benattidev.lavixx.repository.UserRepository;
import com.benattidev.lavixx.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findFirstByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("invalid"));

        if (!user.isActive() || user.getPassword() == null) {
            throw new BadCredentialsException("invalid");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("invalid");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(
                token,
                user.getId(),
                user.getTenant().getId(),
                user.getEmail(),
                user.getRole().name());
    }

    /** Dados do convite (para a tela publica de definir senha). */
    @Transactional(readOnly = true)
    public InvitePreviewResponse previewInvite(String token) {
        User user = userRepository.findByInviteToken(token)
                .orElseThrow(() -> new NotFoundException("Convite invalido ou ja utilizado"));
        return new InvitePreviewResponse(user.getName(), user.getEmail(), user.getTenant().getName());
    }

    /** Aceita o convite: define a senha, ativa o usuario, invalida o token e ja autentica. */
    @Transactional
    public LoginResponse acceptInvite(AcceptInviteRequest request) {
        User user = userRepository.findByInviteToken(request.token())
                .orElseThrow(() -> new NotFoundException("Convite invalido ou ja utilizado"));

        user.setPassword(passwordEncoder.encode(request.password()));
        user.setActive(true);
        user.setInviteToken(null);
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new LoginResponse(
                token,
                user.getId(),
                user.getTenant().getId(),
                user.getEmail(),
                user.getRole().name());
    }
}
