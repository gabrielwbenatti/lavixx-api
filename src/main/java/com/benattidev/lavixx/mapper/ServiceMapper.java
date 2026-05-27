package com.benattidev.lavixx.mapper;

import org.springframework.stereotype.Component;

import com.benattidev.lavixx.dto.service.ServiceResponse;
import com.benattidev.lavixx.entity.Service;

@Component
public class ServiceMapper {

    public ServiceResponse toResponse(Service service) {
        return new ServiceResponse(
                service.getId(),
                service.getName(),
                service.getPrice(),
                service.getCreatedAt(),
                service.getUpdatedAt());
    }
}
