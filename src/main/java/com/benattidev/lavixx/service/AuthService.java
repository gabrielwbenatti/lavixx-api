package com.benattidev.lavixx.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.benattidev.lavixx.dto.auth.LoginRequest;
import com.benattidev.lavixx.dto.auth.LoginResponse;
import com.benattidev.lavixx.entity.User;
import com.benattidev.lavixx.repository.UserRepository;
import com.benattidev.lavixx.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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
}
