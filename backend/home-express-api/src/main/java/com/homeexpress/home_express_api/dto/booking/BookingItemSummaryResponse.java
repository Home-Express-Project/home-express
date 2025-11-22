package com.homeexpress.home_express_api.dto.booking;

import com.homeexpress.home_express_api.entity.BookingItem;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class BookingItemSummaryResponse {
    Long itemId;
    Long bookingId;
    Long categoryId;
    String categoryName;
    Long sizeId;
    String sizeName;
    String name;
    String description;
    Integer quantity;
    BigDecimal weightKg;
    BigDecimal heightCm;
    BigDecimal widthCm;
    BigDecimal depthCm;
    Boolean fragile;
    Boolean requiresDisassembly;
    BigDecimal unitPrice;
    BigDecimal totalPrice;
    LocalDateTime createdAt;

    public static BookingItemSummaryResponse fromEntity(BookingItem item) {
        return BookingItemSummaryResponse.builder()
                .itemId(item.getItemId())
                .bookingId(item.getBookingId())
                .categoryId(item.getCategoryId())
                .categoryName(item.getCategory() != null ? item.getCategory().getName() : null)
                .sizeId(item.getSizeId())
                .sizeName(item.getSize() != null ? item.getSize().getName() : null)
                .name(item.getName())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .weightKg(item.getWeightKg())
                .heightCm(item.getHeightCm())
                .widthCm(item.getWidthCm())
                .depthCm(item.getDepthCm())
                .fragile(item.getIsFragile())
                .requiresDisassembly(item.getRequiresDisassembly())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
