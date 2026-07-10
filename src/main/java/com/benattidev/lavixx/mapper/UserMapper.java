package com.benattidev.lavixx.mapper;

import org.springframework.stereotype.Component;

import com.benattidev.lavixx.dto.user.UserResponse;
import com.benattidev.lavixx.entity.User;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getPassword() == null, // pending: ainda nao definiu a senha
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
