package com.benattidev.lavixx.mapper;

import org.springframework.stereotype.Component;

import com.benattidev.lavixx.dto.customer.CustomerResponse;
import com.benattidev.lavixx.entity.Customer;

@Component
public class CustomerMapper {

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getDocument(),
                customer.getPhone(),
                customer.getCreatedAt(),
                customer.getUpdatedAt());
    }
}
