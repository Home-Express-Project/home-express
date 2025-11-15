package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.dto.request.VehiclePricingRequest;
import com.homeexpress.home_express_api.dto.response.VehiclePricingResponse;
import com.homeexpress.home_express_api.entity.Transport;
import com.homeexpress.home_express_api.entity.VehiclePricing;
import com.homeexpress.home_express_api.entity.VehicleType;
import com.homeexpress.home_express_api.repository.TransportRepository;
import com.homeexpress.home_express_api.repository.VehiclePricingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehiclePricingService {

    @Autowired
    private VehiclePricingRepository vehiclePricingRepository;

    @Autowired
    private TransportRepository transportRepository;

    @Transactional
    public VehiclePricingResponse createVehiclePricing(VehiclePricingRequest request) {
        Transport transport = transportRepository.findById(request.getTransportId())
                .orElseThrow(() -> new RuntimeException("Transport not found with ID: " + request.getTransportId()));

        VehicleType vehicleType;
        try {
            vehicleType = VehicleType.valueOf(request.getVehicleType());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid vehicle type: " + request.getVehicleType());
        }

        if (request.getValidTo() != null && request.getValidTo().isBefore(request.getValidFrom())) {
            throw new RuntimeException("Valid to date must be after valid from date");
        }

        boolean hasOverlap = vehiclePricingRepository.hasOverlappingActivePricing(
                request.getTransportId(),
                vehicleType,
                request.getValidFrom(),
                request.getValidTo(),
                null
        );

        if (hasOverlap) {
            throw new RuntimeException("Overlapping active pricing exists for this vehicle type and date range");
        }

        List<VehiclePricing> activePricings = vehiclePricingRepository.findActiveByTransportAndVehicleType(
                request.getTransportId(),
                vehicleType
        );

        for (VehiclePricing activePricing : activePricings) {
            activePricing.setIsActive(false);
            if (activePricing.getValidTo() == null) {
                activePricing.setValidTo(request.getValidFrom().minusSeconds(1));
            }
            vehiclePricingRepository.save(activePricing);
        }

        VehiclePricing vehiclePricing = new VehiclePricing();
        vehiclePricing.setTransport(transport);
        vehiclePricing.setVehicleType(vehicleType);
        vehiclePricing.setBasePriceVnd(request.getBasePriceVnd());
        vehiclePricing.setPerKmFirst4KmVnd(request.getPerKmFirst4KmVnd());
        vehiclePricing.setPerKm5To40KmVnd(request.getPerKm5To40KmVnd());
        vehiclePricing.setPerKmAfter40KmVnd(request.getPerKmAfter40KmVnd());
        vehiclePricing.setMinChargeVnd(request.getMinChargeVnd());
        vehiclePricing.setElevatorBonusVnd(request.getElevatorBonusVnd());
        vehiclePricing.setNoElevatorFeePerFloorVnd(request.getNoElevatorFeePerFloorVnd());
        vehiclePricing.setNoElevatorFloorThreshold(request.getNoElevatorFloorThreshold());
        vehiclePricing.setPeakHourMultiplier(request.getPeakHourMultiplier());
        vehiclePricing.setWeekendMultiplier(request.getWeekendMultiplier());
        vehiclePricing.setPeakHourStart1(request.getPeakHourStart1());
        vehiclePricing.setPeakHourEnd1(request.getPeakHourEnd1());
        vehiclePricing.setPeakHourStart2(request.getPeakHourStart2());
        vehiclePricing.setPeakHourEnd2(request.getPeakHourEnd2());
        vehiclePricing.setTimezone(request.getTimezone());
        vehiclePricing.setValidFrom(request.getValidFrom());
        vehiclePricing.setValidTo(request.getValidTo());
        vehiclePricing.setIsActive(true);

        VehiclePricing saved = vehiclePricingRepository.save(vehiclePricing);
        return VehiclePricingResponse.fromEntity(saved);
    }

    public List<VehiclePricingResponse> getAllVehiclePricing() {
        return vehiclePricingRepository.findAll().stream()
                .map(VehiclePricingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<VehiclePricingResponse> getVehiclePricingByTransport(Long transportId) {
        return vehiclePricingRepository.findByTransport_TransportId(transportId).stream()
                .map(VehiclePricingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<VehiclePricingResponse> getVehiclePricingByVehicleType(String vehicleType) {
        VehicleType type = VehicleType.valueOf(vehicleType);
        return vehiclePricingRepository.findByVehicleType(type).stream()
                .map(VehiclePricingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<VehiclePricingResponse> getActiveVehiclePricing() {
        return vehiclePricingRepository.findByIsActive(true).stream()
                .map(VehiclePricingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public VehiclePricingResponse getCurrentActivePricing(Long transportId, String vehicleType) {
        VehicleType type = VehicleType.valueOf(vehicleType);
        LocalDateTime now = LocalDateTime.now();
        
        return vehiclePricingRepository.findActiveByTransportAndVehicleTypeAndDate(transportId, type, now)
                .map(VehiclePricingResponse::fromEntity)
                .orElseThrow(() -> new RuntimeException("No active pricing found for vehicle type: " + vehicleType));
    }

    public VehiclePricingResponse getVehiclePricingById(Long id) {
        VehiclePricing pricing = vehiclePricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle pricing not found with ID: " + id));
        return VehiclePricingResponse.fromEntity(pricing);
    }

    @Transactional
    public VehiclePricingResponse updateVehiclePricing(Long id, VehiclePricingRequest request) {
        VehiclePricing existing = vehiclePricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle pricing not found with ID: " + id));

        if (request.getValidTo() != null && request.getValidTo().isBefore(request.getValidFrom())) {
            throw new RuntimeException("Valid to date must be after valid from date");
        }

        VehicleType vehicleType = VehicleType.valueOf(request.getVehicleType());

        boolean hasOverlap = vehiclePricingRepository.hasOverlappingActivePricing(
                request.getTransportId(),
                vehicleType,
                request.getValidFrom(),
                request.getValidTo(),
                id
        );

        if (hasOverlap) {
            throw new RuntimeException("Overlapping active pricing exists for this vehicle type and date range");
        }

        existing.setBasePriceVnd(request.getBasePriceVnd());
        existing.setPerKmFirst4KmVnd(request.getPerKmFirst4KmVnd());
        existing.setPerKm5To40KmVnd(request.getPerKm5To40KmVnd());
        existing.setPerKmAfter40KmVnd(request.getPerKmAfter40KmVnd());
        existing.setMinChargeVnd(request.getMinChargeVnd());
        existing.setElevatorBonusVnd(request.getElevatorBonusVnd());
        existing.setNoElevatorFeePerFloorVnd(request.getNoElevatorFeePerFloorVnd());
        existing.setNoElevatorFloorThreshold(request.getNoElevatorFloorThreshold());
        existing.setPeakHourMultiplier(request.getPeakHourMultiplier());
        existing.setWeekendMultiplier(request.getWeekendMultiplier());
        existing.setPeakHourStart1(request.getPeakHourStart1());
        existing.setPeakHourEnd1(request.getPeakHourEnd1());
        existing.setPeakHourStart2(request.getPeakHourStart2());
        existing.setPeakHourEnd2(request.getPeakHourEnd2());
        existing.setTimezone(request.getTimezone());
        existing.setValidFrom(request.getValidFrom());
        existing.setValidTo(request.getValidTo());

        VehiclePricing updated = vehiclePricingRepository.save(existing);
        return VehiclePricingResponse.fromEntity(updated);
    }

    @Transactional
    public void deactivateVehiclePricing(Long id) {
        VehiclePricing pricing = vehiclePricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle pricing not found with ID: " + id));
        
        pricing.setIsActive(false);
        if (pricing.getValidTo() == null) {
            pricing.setValidTo(LocalDateTime.now());
        }
        vehiclePricingRepository.save(pricing);
    }
}
