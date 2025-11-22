package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.Vehicle;
import com.homeexpress.home_express_api.entity.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    List<Vehicle> findByTransportTransportId(Long transportId);
    
    List<Vehicle> findByTransportTransportIdAndStatus(Long transportId, VehicleStatus status);
    
    List<Vehicle> findByStatus(VehicleStatus status);
    
    long countByTransportTransportIdAndStatusNot(Long transportId, VehicleStatus status);

    List<Vehicle> findByVehicleIdIn(List<Long> vehicleIds);
    
    @Query("SELECT v FROM Vehicle v WHERE v.transport.transportId = :transportId AND v.vehicleId = :vehicleId")
    Optional<Vehicle> findByTransportIdAndVehicleId(@Param("transportId") Long transportId, @Param("vehicleId") Long vehicleId);
    
    boolean existsByLicensePlateCompact(String licensePlateCompact);
    
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Vehicle v WHERE v.licensePlateCompact = :licensePlateCompact AND v.vehicleId <> :vehicleId")
    boolean existsByLicensePlateCompactAndVehicleIdNot(@Param("licensePlateCompact") String licensePlateCompact, @Param("vehicleId") Long vehicleId);
}
