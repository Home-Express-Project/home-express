package com.homeexpress.home_express_api.service.map;

import com.homeexpress.home_express_api.dto.location.MapPlaceDTO;
import java.util.List;

public interface MapService {
    /**
     * Tìm kiếm địa chỉ theo từ khóa (Autocomplete)
     */
    List<MapPlaceDTO> searchPlaces(String query);

    /**
     * Lấy chi tiết tọa độ từ Place ID
     */
    MapPlaceDTO getPlaceDetails(String placeId);

    /**
     * Lấy địa chỉ từ tọa độ (Reverse Geocoding)
     */
    MapPlaceDTO getAddressFromCoordinates(double lat, double lng);

    /**
     * Tính khoảng cách giữa 2 điểm (trả về mét)
     */
    long calculateDistanceInMeters(double originLat, double originLng, double destLat, double destLng);
}
