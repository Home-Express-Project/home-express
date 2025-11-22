package com.homeexpress.home_express_api.controller;

import com.homeexpress.home_express_api.dto.location.MapPlaceDTO;
import com.homeexpress.home_express_api.service.map.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    @GetMapping("/autocomplete")
    public ResponseEntity<List<MapPlaceDTO>> autocomplete(@RequestParam String query) {
        return ResponseEntity.ok(mapService.searchPlaces(query));
    }

    @GetMapping("/details")
    public ResponseEntity<MapPlaceDTO> getPlaceDetails(@RequestParam String placeId) {
        return ResponseEntity.ok(mapService.getPlaceDetails(placeId));
    }

    @GetMapping("/reverse")
    public ResponseEntity<MapPlaceDTO> reverseGeocode(@RequestParam double lat, @RequestParam double lng) {
        return ResponseEntity.ok(mapService.getAddressFromCoordinates(lat, lng));
    }

    @GetMapping("/distance")
    public ResponseEntity<Map<String, Object>> calculateDistance(
            @RequestParam double originLat, @RequestParam double originLng,
            @RequestParam double destLat, @RequestParam double destLng) {
        
        long distanceMeters = mapService.calculateDistanceInMeters(originLat, originLng, destLat, destLng);
        return ResponseEntity.ok(Map.of(
                "distanceMeters", distanceMeters,
                "distanceKm", String.format("%.2f", distanceMeters / 1000.0)
        ));
    }
}
