package com.benattidev.lavixx.mapper;

import org.springframework.stereotype.Component;

import com.benattidev.lavixx.dto.employee.EmployeeResponse;
import com.benattidev.lavixx.entity.Employee;

@Component
public class EmployeeMapper {

    public EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.isActive(),
                employee.getCreatedAt(),
                employee.getUpdatedAt());
    }
}
