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
 * Service chuyển đổi dữ liệu (Mapper).
 * Nhiệm vụ: Chuyển kết quả từ AI (EnhancedDetectedItem) thành BookingItem để lưu xuống DB.
 * Đồng thời đóng gói các thông tin phụ (confidence, bounding box...) thành chuỗi JSON.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIDetectionMapper {

    private final ObjectMapper objectMapper;

    /**
     * Chuyển đổi một item từ AI sang BookingItem.
     *
     * @param aiItem     Dữ liệu gốc từ AI
     * @param bookingId  ID của đơn hàng
     * @param categoryId ID danh mục (đã map trước đó)
     * @param sizeId     ID kích thước (đã map trước đó)
     * @return BookingItem entity sẵn sàng để lưu
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
        item.setQuantity(1); // Mặc định là 1, số lượng sẽ được gộp sau

        // Chuyển đổi kích thước (nếu AI đo được)
        if (aiItem.getDimsCm() != null) {
            EnhancedDetectedItem.Dimensions dims = aiItem.getDimsCm();
            item.setHeightCm(toBigDecimal(dims.getHeight()));
            item.setWidthCm(toBigDecimal(dims.getWidth()));
            item.setDepthCm(toBigDecimal(dims.getLength()));
        }

        item.setWeightKg(toBigDecimal(aiItem.getWeightKg()));

        // Các cờ xử lý đặc biệt
        if (aiItem.getFragile() != null) {
            item.setIsFragile(aiItem.getFragile());
        }
        if (aiItem.getDisassemblyRequired() != null) {
            item.setRequiresDisassembly(aiItem.getDisassemblyRequired());
        }

        return item;
    }

    /**
     * Đóng gói toàn bộ thông tin phụ của AI vào chuỗi JSON.
     * Chuỗi này sẽ được lưu vào cột `ai_metadata` trong bảng `booking_items`.
     * Giúp giữ lại bằng chứng nhận diện (độ tin cậy, vị trí trong ảnh...) để debug sau này.
     */
    public String toAIMetadataJson(EnhancedDetectedItem aiItem) {
        Map<String, Object> metadata = new HashMap<>();

        putIfNotNull(metadata, "confidence", aiItem.getConfidence());
        putIfNotNull(metadata, "subcategory", aiItem.getSubcategory());
        putIfNotNull(metadata, "bbox_norm", toBoundingBoxMap(aiItem)); // Vị trí trong ảnh
        putIfNotNull(metadata, "dims_confidence", aiItem.getDimsConfidence());
        putIfNotNull(metadata, "dimensions_basis", aiItem.getDimensionsBasis());
        putIfNotNull(metadata, "volume_m3", aiItem.getVolumeM3());
        putIfNotNull(metadata, "weight_confidence", aiItem.getWeightConfidence());
        putIfNotNull(metadata, "weight_basis", aiItem.getWeightBasis());
        putIfNotNull(metadata, "weight_model", aiItem.getWeightModel());
        putIfNotNull(metadata, "occluded_fraction", aiItem.getOccludedFraction()); // Độ bị che khuất
        putIfNotNull(metadata, "orientation", aiItem.getOrientation());
        putIfNotNull(metadata, "material", aiItem.getMaterial());
        putIfNotNull(metadata, "color", aiItem.getColor());
        putIfNotNull(metadata, "room_hint", aiItem.getRoomHint()); // Gợi ý phòng (Bếp/Ngủ)
        putIfNotNull(metadata, "brand", aiItem.getBrand());
        putIfNotNull(metadata, "model", aiItem.getModel());
        putIfNotNull(metadata, "two_person_lift", aiItem.getTwoPersonLift()); // Cần 2 người khiêng?
        putIfNotNull(metadata, "stackable", aiItem.getStackable());
        putIfNotNull(metadata, "notes", aiItem.getNotes());
        putIfNotNull(metadata, "image_index", aiItem.getImageIndex());

        if (metadata.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.warn("Lỗi khi đóng gói metadata cho item '{}': {}", aiItem.getName(), e.getMessage());
            return null;
        }
    }

    // Chuyển đổi Bounding Box (Vị trí khung hình chữ nhật bao quanh vật thể)
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
                    return; // Bỏ qua list rỗng
                }
            }
            map.put(key, value);
        }
    }

    private BigDecimal toBigDecimal(Number value) {
        return value == null ? null : BigDecimal.valueOf(value.doubleValue());
    }
}
