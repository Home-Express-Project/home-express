package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.dto.request.VehicleRequest;
import com.homeexpress.home_express_api.dto.request.VehicleStatusUpdateRequest;
import com.homeexpress.home_express_api.dto.response.VehicleResponse;
import com.homeexpress.home_express_api.entity.*;
import com.homeexpress.home_express_api.exception.ResourceNotFoundException;
import com.homeexpress.home_express_api.exception.UnauthorizedException;
import com.homeexpress.home_express_api.repository.TransportRepository;
import com.homeexpress.home_express_api.repository.UserRepository;
import com.homeexpress.home_express_api.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private static final Pattern VN_LICENSE_PLATE_PATTERN = Pattern.compile("^[0-9]{2}[A-Z]{1,3}[0-9]{4,6}$");

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private TransportRepository transportRepository;

    @Autowired
    private UserRepository userRepository;

    public String normalizeLicensePlate(String licensePlate) {
        if (licensePlate == null) {
            return null;
        }
        return licensePlate.trim().toUpperCase().replaceAll("\\s+", "");
    }

    public String generateLicensePlateCompact(String licensePlate) {
        if (licensePlate == null) {
            return null;
        }
        return licensePlate.trim().toUpperCase()
                .replaceAll("\\s+", "")
                .replaceAll("-", "")
                .replaceAll("\\.", "");
    }

    public boolean validateVietnameseLicensePlate(String licensePlate) {
        if (licensePlate == null || licensePlate.isEmpty()) {
            return false;
        }
        String compact = generateLicensePlateCompact(licensePlate);
        return VN_LICENSE_PLATE_PATTERN.matcher(compact).matches();
    }

    private void checkOwnership(Vehicle vehicle, Long transportId) {
        if (!vehicle.getTransport().getTransportId().equals(transportId)) {
            throw new UnauthorizedException("You can only manage your own vehicles");
        }
    }

    @Transactional
    public VehicleResponse createVehicle(VehicleRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != UserRole.TRANSPORT) {
            throw new UnauthorizedException("Only TRANSPORT role can create vehicles");
        }

        Transport transport = transportRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport profile not found"));

        if (!validateVietnameseLicensePlate(request.getLicensePlate())) {
            throw new IllegalArgumentException("Invalid Vietnamese license plate format. Expected format: 29A-12345 or 51D-123.45");
        }

        String licensePlateCompact = generateLicensePlateCompact(request.getLicensePlate());
        if (vehicleRepository.existsByLicensePlateCompact(licensePlateCompact)) {
            throw new IllegalArgumentException("License plate already exists");
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setTransport(transport);
        vehicle.setType(request.getType());
        vehicle.setModel(request.getModel());
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setCapacityKg(request.getCapacityKg());
        vehicle.setCapacityM3(request.getCapacityM3());
        vehicle.setLengthCm(request.getLengthCm());
        vehicle.setWidthCm(request.getWidthCm());
        vehicle.setHeightCm(request.getHeightCm());
        vehicle.setYear(request.getYear());
        vehicle.setColor(request.getColor());
        vehicle.setHasTailLift(Boolean.TRUE.equals(request.getHasTailLift()));
        vehicle.setHasTools(request.getHasTools() == null || Boolean.TRUE.equals(request.getHasTools()));
        vehicle.setImageUrl(request.getImageUrl());
        vehicle.setDescription(request.getDescription());
        vehicle.setStatus(VehicleStatus.ACTIVE);
        vehicle.setCreatedBy(user);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return VehicleResponse.fromEntity(savedVehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByTransport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != UserRole.TRANSPORT) {
            throw new UnauthorizedException("Only TRANSPORT role can access vehicles");
        }

        List<Vehicle> vehicles = vehicleRepository.findByTransportTransportId(userId);
        return vehicles.stream()
                .map(VehicleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long vehicleId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != UserRole.TRANSPORT) {
            throw new UnauthorizedException("Only TRANSPORT role can access vehicles");
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        checkOwnership(vehicle, userId);

        return VehicleResponse.fromEntity(vehicle);
    }

    @Transactional
    public VehicleResponse updateVehicle(Long vehicleId, VehicleRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != UserRole.TRANSPORT) {
            throw new UnauthorizedException("Only TRANSPORT role can update vehicles");
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        checkOwnership(vehicle, userId);

        if (!validateVietnameseLicensePlate(request.getLicensePlate())) {
            throw new IllegalArgumentException("Invalid Vietnamese license plate format. Expected format: 29A-12345 or 51D-123.45");
        }

        String licensePlateCompact = generateLicensePlateCompact(request.getLicensePlate());
        if (vehicleRepository.existsByLicensePlateCompactAndVehicleIdNot(licensePlateCompact, vehicleId)) {
            throw new IllegalArgumentException("License plate already exists");
        }

        vehicle.setType(request.getType());
        vehicle.setModel(request.getModel());
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setCapacityKg(request.getCapacityKg());
        vehicle.setCapacityM3(request.getCapacityM3());
        vehicle.setLengthCm(request.getLengthCm());
        vehicle.setWidthCm(request.getWidthCm());
        vehicle.setHeightCm(request.getHeightCm());
        vehicle.setYear(request.getYear());
        vehicle.setColor(request.getColor());
        vehicle.setHasTailLift(request.getHasTailLift());
        vehicle.setHasTools(request.getHasTools());
        vehicle.setImageUrl(request.getImageUrl());
        vehicle.setDescription(request.getDescription());
        vehicle.setUpdatedBy(user);

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return VehicleResponse.fromEntity(updatedVehicle);
    }

    @Transactional
    public void deleteVehicle(Long vehicleId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != UserRole.TRANSPORT) {
            throw new UnauthorizedException("Only TRANSPORT role can delete vehicles");
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        checkOwnership(vehicle, userId);

        vehicleRepository.delete(vehicle);
    }

    @Transactional
    public VehicleResponse updateVehicleStatus(Long vehicleId, VehicleStatusUpdateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != UserRole.TRANSPORT) {
            throw new UnauthorizedException("Only TRANSPORT role can update vehicle status");
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        checkOwnership(vehicle, userId);

        vehicle.setStatus(request.getStatus());
        vehicle.setUpdatedBy(user);

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return VehicleResponse.fromEntity(updatedVehicle);
    }

    /**
     * Get eligible vehicles for a booking based on weight, volume, and equipment requirements
     */
    @Transactional(readOnly = true)
    public List<VehicleResponse> getEligibleVehicles(
            BigDecimal totalWeight,
            BigDecimal totalVolume,
            boolean requiresTailLift,
            boolean requiresTools) {
        
        // Get all active vehicles
        List<Vehicle> allVehicles = vehicleRepository.findByStatus(VehicleStatus.ACTIVE);

        // Filter by requirements
        List<Vehicle> eligibleVehicles = allVehicles.stream()
                .filter(vehicle -> {
                    // Check weight capacity
                    if (vehicle.getCapacityKg() == null || vehicle.getCapacityKg().compareTo(totalWeight) < 0) {
                        return false;
                    }

                    // Check volume capacity if specified
                    if (vehicle.getCapacityM3() != null && totalVolume != null) {
                        if (vehicle.getCapacityM3().compareTo(totalVolume) < 0) {
                            return false;
                        }
                    }

                    // Check tail lift requirement
                    if (requiresTailLift && !vehicle.getHasTailLift()) {
                        return false;
                    }

                    // Check tools requirement
                    if (requiresTools && !vehicle.getHasTools()) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());

        return eligibleVehicles.stream()
                .map(VehicleResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
