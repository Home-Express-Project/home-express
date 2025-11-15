package com.homeexpress.home_express_api.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeexpress.home_express_api.dto.ai.EnhancedDetectedItem;
import com.homeexpress.home_express_api.entity.BookingItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility component that maps enhanced AI detection payloads into {@link BookingItem}
 * entities and serialised metadata suitable for persistence.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIDetectionMapper {

    private final ObjectMapper objectMapper;

    /**
     * Map an {@link EnhancedDetectedItem} into a partially populated {@link BookingItem}.
     *
     * @param aiItem     AI detection payload for a single item
     * @param bookingId  owning booking identifier
     * @param categoryId detected category identifier (optional, may be null)
     * @param sizeId     mapped size identifier (optional, may be null)
     * @return booking item instance populated with core fields
     */
    public BookingItem toBookingItem(EnhancedDetectedItem aiItem,
                                     Long bookingId,
                                     Long categoryId,
                                     Long sizeId) {
        BookingItem item = new BookingItem();
        item.setBookingId(bookingId);
        item.setCategoryId(categoryId);
        item.setSizeId(sizeId);
        item.setName(aiItem.getName());
        item.setDescription(aiItem.getNotes());
        item.setQuantity(1);

        if (aiItem.getDimsCm() != null) {
            EnhancedDetectedItem.Dimensions dims = aiItem.getDimsCm();
            item.setHeightCm(toBigDecimal(dims.getHeight()));
            item.setWidthCm(toBigDecimal(dims.getWidth()));
            item.setDepthCm(toBigDecimal(dims.getLength()));
        }

        item.setWeightKg(toBigDecimal(aiItem.getWeightKg()));

        if (aiItem.getFragile() != null) {
            item.setIsFragile(aiItem.getFragile());
        }
        if (aiItem.getDisassemblyRequired() != null) {
            item.setRequiresDisassembly(aiItem.getDisassemblyRequired());
        }

        return item;
    }

    /**
     * Serialise auxiliary AI attributes to JSON for storage in {@code booking_items.ai_metadata}.
     *
     * @param aiItem AI detection payload
     * @return JSON string or {@code null} when the payload is empty / serialisation fails
     */
    public String toAIMetadataJson(EnhancedDetectedItem aiItem) {
        Map<String, Object> metadata = new HashMap<>();

        putIfNotNull(metadata, "confidence", aiItem.getConfidence());
        putIfNotNull(metadata, "subcategory", aiItem.getSubcategory());
        putIfNotNull(metadata, "bbox_norm", toBoundingBoxMap(aiItem));
        putIfNotNull(metadata, "dims_confidence", aiItem.getDimsConfidence());
        putIfNotNull(metadata, "dimensions_basis", aiItem.getDimensionsBasis());
        putIfNotNull(metadata, "volume_m3", aiItem.getVolumeM3());
        putIfNotNull(metadata, "weight_confidence", aiItem.getWeightConfidence());
        putIfNotNull(metadata, "weight_basis", aiItem.getWeightBasis());
        putIfNotNull(metadata, "weight_model", aiItem.getWeightModel());
        putIfNotNull(metadata, "occluded_fraction", aiItem.getOccludedFraction());
        putIfNotNull(metadata, "orientation", aiItem.getOrientation());
        putIfNotNull(metadata, "material", aiItem.getMaterial());
        putIfNotNull(metadata, "color", aiItem.getColor());
        putIfNotNull(metadata, "room_hint", aiItem.getRoomHint());
        putIfNotNull(metadata, "brand", aiItem.getBrand());
        putIfNotNull(metadata, "model", aiItem.getModel());
        putIfNotNull(metadata, "two_person_lift", aiItem.getTwoPersonLift());
        putIfNotNull(metadata, "stackable", aiItem.getStackable());
        putIfNotNull(metadata, "notes", aiItem.getNotes());
        putIfNotNull(metadata, "image_index", aiItem.getImageIndex());

        if (metadata.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialise AI metadata for item '{}': {}", aiItem.getName(), e.getMessage());
            return null;
        }
    }

    private Map<String, Object> toBoundingBoxMap(EnhancedDetectedItem item) {
        if (item.getBboxNorm() == null) {
            return null;
        }
        EnhancedDetectedItem.BoundingBox box = item.getBboxNorm();
        if (box.getXMin() == null && box.getYMin() == null
                && box.getXMax() == null && box.getYMax() == null) {
            return null;
        }
        Map<String, Object> bbox = new HashMap<>();
        putIfNotNull(bbox, "x_min", box.getXMin());
        putIfNotNull(bbox, "y_min", box.getYMin());
        putIfNotNull(bbox, "x_max", box.getXMax());
        putIfNotNull(bbox, "y_max", box.getYMax());
        return bbox;
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            if (value instanceof Iterable<?> iterable) {
                if (!iterable.iterator().hasNext()) {
                    return;
                }
            }
            map.put(key, value);
        }
    }

    private BigDecimal toBigDecimal(Number value) {
        return value == null ? null : BigDecimal.valueOf(value.doubleValue());
    }
}
