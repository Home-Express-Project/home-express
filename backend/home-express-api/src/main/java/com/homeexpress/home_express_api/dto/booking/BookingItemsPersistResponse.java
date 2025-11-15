package com.homeexpress.home_express_api.dto.booking;

import com.homeexpress.home_express_api.entity.BookingItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload summarising booking items persisted from AI detections.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingItemsPersistResponse {

    private Long bookingId;
    private boolean replacedExisting;
    private int savedCount;
    private List<BookingItemSummary> items;

    public static BookingItemsPersistResponse fromEntities(Long bookingId,
                                                           boolean replacedExisting,
                                                           List<BookingItem> entities) {
        List<BookingItemSummary> summaries = entities.stream()
            .map(BookingItemSummary::fromEntity)
            .collect(Collectors.toList());

        return BookingItemsPersistResponse.builder()
            .bookingId(bookingId)
            .replacedExisting(replacedExisting)
            .savedCount(entities.size())
            .items(summaries)
            .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingItemSummary {
        private Long itemId;
        private Long categoryId;
        private Long sizeId;
        private String name;
        private String description;
        private Integer quantity;
        private BigDecimal weightKg;
        private BigDecimal heightCm;
        private BigDecimal widthCm;
        private BigDecimal depthCm;
        private Boolean fragile;
        private Boolean requiresDisassembly;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String aiMetadata;
        private LocalDateTime createdAt;

        public static BookingItemSummary fromEntity(BookingItem entity) {
            return BookingItemSummary.builder()
                .itemId(entity.getItemId())
                .categoryId(entity.getCategoryId())
                .sizeId(entity.getSizeId())
                .name(entity.getName())
                .description(entity.getDescription())
                .quantity(entity.getQuantity())
                .weightKg(entity.getWeightKg())
                .heightCm(entity.getHeightCm())
                .widthCm(entity.getWidthCm())
                .depthCm(entity.getDepthCm())
                .fragile(entity.getIsFragile())
                .requiresDisassembly(entity.getRequiresDisassembly())
                .unitPrice(entity.getUnitPrice())
                .totalPrice(entity.getTotalPrice())
                .aiMetadata(entity.getAiMetadata())
                .createdAt(entity.getCreatedAt())
                .build();
        }
    }
}
