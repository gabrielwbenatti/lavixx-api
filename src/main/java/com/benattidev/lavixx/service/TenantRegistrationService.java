package com.benattidev.lavixx.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benattidev.lavixx.dto.tenant.TenantRegistrationRequest;
import com.benattidev.lavixx.dto.tenant.TenantRegistrationResponse;
import com.benattidev.lavixx.entity.Tenant;
import com.benattidev.lavixx.entity.User;
import com.benattidev.lavixx.entity.enums.UserRole;
import com.benattidev.lavixx.exception.DuplicateResourceException;
import com.benattidev.lavixx.repository.TenantRepository;
import com.benattidev.lavixx.repository.UserRepository;
import com.benattidev.lavixx.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantRegistrationService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PaymentMethodService paymentMethodService;

    @Transactional
    public TenantRegistrationResponse register(TenantRegistrationRequest request) {
        if (tenantRepository.existsByDocument(request.document())) {
            throw new DuplicateResourceException("Ja existe um estabelecimento cadastrado com este CPF/CNPJ");
        }

        Tenant tenant = Tenant.builder()
                .name(request.name())
                .document(request.document())
                .build();
        tenant = tenantRepository.save(tenant);

        if (userRepository.findFirstByEmail(request.adminEmail()).isPresent()) {
            throw new DuplicateResourceException("Ja existe um usuario com este e-mail");
        }

        User admin = User.builder()
                .tenant(tenant)
                .name(request.adminName())
                .email(request.adminEmail())
                .password(passwordEncoder.encode(request.adminPassword()))
                .active(true)
                .role(UserRole.admin)
                .build();
        admin = userRepository.save(admin);

        // Cria as formas de pagamento padrao para o novo estabelecimento.
        paymentMethodService.seedDefaults(tenant);

        String token = jwtService.generateToken(admin);

        return new TenantRegistrationResponse(
                tenant.getId(),
                tenant.getName(),
                admin.getId(),
                admin.getEmail(),
                token);
    }
}
