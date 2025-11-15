package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.VehiclePricing;
import com.homeexpress.home_express_api.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehiclePricingRepository extends JpaRepository<VehiclePricing, Long> {

    List<VehiclePricing> findByTransport_TransportIdAndVehicleType(Long transportId, VehicleType vehicleType);

    @Query("SELECT vp FROM VehiclePricing vp WHERE vp.transport.transportId = :transportId " +
           "AND vp.vehicleType = :vehicleType AND vp.isActive = true " +
           "AND vp.validFrom <= :checkDate " +
           "AND (vp.validTo IS NULL OR vp.validTo >= :checkDate)")
    Optional<VehiclePricing> findActiveByTransportAndVehicleTypeAndDate(
            @Param("transportId") Long transportId,
            @Param("vehicleType") VehicleType vehicleType,
            @Param("checkDate") LocalDateTime checkDate);

    @Query("SELECT vp FROM VehiclePricing vp WHERE vp.transport.transportId = :transportId " +
           "AND vp.vehicleType = :vehicleType AND vp.isActive = true")
    List<VehiclePricing> findActiveByTransportAndVehicleType(
            @Param("transportId") Long transportId,
            @Param("vehicleType") VehicleType vehicleType);

    @Query("SELECT CASE WHEN COUNT(vp) > 0 THEN true ELSE false END FROM VehiclePricing vp " +
           "WHERE vp.transport.transportId = :transportId " +
           "AND vp.vehicleType = :vehicleType " +
           "AND vp.isActive = true " +
           "AND (:excludeId IS NULL OR vp.vehiclePricingId <> :excludeId) " +
           "AND (:validTo IS NULL OR vp.validFrom <= :validTo) " +
           "AND (vp.validTo IS NULL OR :validFrom <= vp.validTo)")
    boolean hasOverlappingActivePricing(
            @Param("transportId") Long transportId,
            @Param("vehicleType") VehicleType vehicleType,
            @Param("validFrom") LocalDateTime validFrom,
            @Param("validTo") LocalDateTime validTo,
            @Param("excludeId") Long excludeId);

    List<VehiclePricing> findByIsActive(Boolean isActive);

    List<VehiclePricing> findByVehicleType(VehicleType vehicleType);

    List<VehiclePricing> findByTransport_TransportId(Long transportId);

    @Query("SELECT vp FROM VehiclePricing vp WHERE vp.transport.transportId = :transportId " +
           "AND vp.vehicleType = :vehicleType " +
           "ORDER BY vp.validFrom DESC")
    List<VehiclePricing> findByTransportAndVehicleTypeOrderByValidFromDesc(
            @Param("transportId") Long transportId,
            @Param("vehicleType") VehicleType vehicleType);
}
