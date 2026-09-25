package com.benattidev.lavixx.mapper;

import org.springframework.stereotype.Component;

import com.benattidev.lavixx.dto.vehicle.VehicleResponse;
import com.benattidev.lavixx.dto.vehicle.VehicleSummary;
import com.benattidev.lavixx.entity.Vehicle;

@Component
public class VehicleMapper {

    public VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getCustomer().getId(),
                vehicle.getCustomer().getName(),
                vehicle.getType(),
                vehicle.getPlate(),
                vehicle.getIdentifier(),
                vehicle.getNickname(),
                vehicle.getManufacturer(),
                vehicle.getModel(),
                vehicle.getColor(),
                vehicle.getYear(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt());
    }

    public VehicleSummary toSummary(Vehicle vehicle) {
        return new VehicleSummary(
                vehicle.getId(),
                vehicle.getType(),
                vehicle.getPlate(),
                vehicle.getIdentifier(),
                vehicle.getNickname(),
                vehicle.getManufacturer(),
                vehicle.getModel(),
                vehicle.getColor(),
                vehicle.getYear());
    }
}
