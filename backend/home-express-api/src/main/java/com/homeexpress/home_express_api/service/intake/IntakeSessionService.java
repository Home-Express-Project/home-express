package com.homeexpress.home_express_api.service.intake;

import com.homeexpress.home_express_api.dto.intake.ItemCandidateDto;
import com.homeexpress.home_express_api.entity.IntakeSession;
import com.homeexpress.home_express_api.entity.IntakeSessionItem;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.repository.IntakeSessionRepository;
import com.homeexpress.home_express_api.repository.IntakeSessionItemRepository;
import com.homeexpress.home_express_api.constants.BookingConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý phiên làm việc (Session) cho quá trình nhập liệu.
 * Giúp lưu tạm danh sách đồ đạc trước khi khách chốt đơn.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntakeSessionService {
    
    private final IntakeSessionRepository sessionRepository;
    private final IntakeSessionItemRepository itemRepository;
    
    /**
     * Tạo session mới hoặc lấy lại session đang hoạt động.
     * Nếu khách đang nhập dở thì trả về cái cũ để làm tiếp.
     */
    @Transactional
    public IntakeSession createOrGetSession(String sessionId, User user) {
        return sessionRepository.findBySessionIdAndStatus(sessionId, "active")
            .orElseGet(() -> {
                IntakeSession session = IntakeSession.builder()
                    .sessionId(sessionId)
                    .user(user)
                    .status("active")
                    .totalItems(0)
                    .createdAt(LocalDateTime.now())
                    // Set thời gian hết hạn (VD: 24h) để tránh rác database
                    .expiresAt(LocalDateTime.now().plusHours(BookingConstants.INTAKE_SESSION_EXPIRY_HOURS))
                    .build();
                
                log.info("Đã tạo phiên làm việc mới: {}", sessionId);
                return sessionRepository.save(session);
            });
    }
    
    /**
     * Lưu danh sách đồ vào session.
     * Đồng thời tính toán lại tổng thể tích dự kiến.
     */
    @Transactional
    public void saveItems(String sessionId, List<ItemCandidateDto> candidates, String aiService, Double confidence) {
        IntakeSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy session: " + sessionId));
        
        // Xóa đồ cũ đi lưu lại từ đầu (hoặc có thể sửa thành append nếu muốn)
        session.getItems().clear();
        
        for (ItemCandidateDto candidate : candidates) {
            IntakeSessionItem item = mapCandidateToItem(candidate);
            session.addItem(item);
        }
        
        // Lưu thông tin meta (dùng AI nào, độ tin cậy bao nhiêu)
        session.setAiServiceUsed(aiService);
        if (confidence != null) {
            session.setAverageConfidence(BigDecimal.valueOf(confidence));
        }
        
        // Tính tổng thể tích (m3) để ước lượng xe tải
        BigDecimal totalVolume = session.getItems().stream()
            .map(IntakeSessionItem::getVolumeM3)
            .filter(vol -> vol != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        session.setEstimatedVolume(totalVolume);
        
        sessionRepository.save(session);
        log.info("Đã lưu {} món đồ vào session {}", candidates.size(), sessionId);
    }
    
    public Optional<IntakeSession> getSession(String sessionId) {
        return sessionRepository.findBySessionIdAndStatus(sessionId, "active");
    }
    
    public List<IntakeSessionItem> getSessionItems(String sessionId) {
        return itemRepository.findBySessionSessionId(sessionId);
    }
    
    /**
     * Đánh dấu session là hết hạn (khách bỏ dở lâu quá).
     */
    @Transactional
    public void expireSession(String sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setStatus("expired");
            sessionRepository.save(session);
            log.info("Đã đóng session: {}", sessionId);
        });
    }
    
    @Transactional
    public void deleteSession(String sessionId) {
        sessionRepository.deleteById(sessionId);
        log.info("Đã xóa session vĩnh viễn: {}", sessionId);
    }
    
    /**
     * Job chạy ngầm định kỳ để dọn dẹp rác.
     * 1. Đánh dấu hết hạn cho các session quá giờ.
     * 2. Xóa vĩnh viễn các session đã hết hạn quá lâu (VD: 30 ngày).
     */
    @Scheduled(cron = BookingConstants.INTAKE_SESSION_CLEANUP_CRON)
    @Transactional
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        
        int expired = sessionRepository.expireOldSessions(now);
        log.info("Đã đánh dấu hết hạn {} session cũ", expired);
        
        LocalDateTime cutoff = now.minusDays(BookingConstants.INTAKE_SESSION_CLEANUP_DAYS);
        int deleted = sessionRepository.deleteExpiredSessions(cutoff);
        log.info("Đã dọn dẹp sạch sẽ {} session rác (cũ hơn {} ngày)", deleted, BookingConstants.INTAKE_SESSION_CLEANUP_DAYS);
    }
    
    // Helper: Chuyển đổi từ DTO (dữ liệu AI trả về) sang Entity (Lưu DB)
    private IntakeSessionItem mapCandidateToItem(ItemCandidateDto candidate) {
        IntakeSessionItem.IntakeSessionItemBuilder builder = IntakeSessionItem.builder()
            .itemId(candidate.getId())
            .name(candidate.getName())
            .category(candidate.getCategoryName())
            .quantity(candidate.getQuantity() != null ? candidate.getQuantity() : 1)
            .isFragile(candidate.getIsFragile() != null ? candidate.getIsFragile() : false)
            .requiresDisassembly(candidate.getRequiresDisassembly() != null ? candidate.getRequiresDisassembly() : false)
            .imageUrl(candidate.getImageUrl())
            .notes(candidate.getNotes())
            .source(candidate.getSource());
        
        if (candidate.getWeightKg() != null) {
            builder.weightKg(BigDecimal.valueOf(candidate.getWeightKg()));
        }
        
        // Tính toán kích thước và thể tích
        if (candidate.getDimensions() != null) {
            ItemCandidateDto.DimensionsDto dims = candidate.getDimensions();
            if (dims.getWidthCm() != null) {
                builder.widthCm(BigDecimal.valueOf(dims.getWidthCm()));
            }
            if (dims.getHeightCm() != null) {
                builder.heightCm(BigDecimal.valueOf(dims.getHeightCm()));
            }
            if (dims.getDepthCm() != null) {
                builder.lengthCm(BigDecimal.valueOf(dims.getDepthCm()));
            }
            
            // Công thức: Dài x Rộng x Cao / 1.000.000 (cm3 -> m3)
            if (dims.getWidthCm() != null && dims.getHeightCm() != null && dims.getDepthCm() != null) {
                double volumeM3 = (dims.getWidthCm() * dims.getHeightCm() * dims.getDepthCm()) / 1_000_000.0;
                builder.volumeM3(BigDecimal.valueOf(volumeM3));
            }
        }
        
        if (candidate.getConfidence() != null) {
            builder.confidence(BigDecimal.valueOf(candidate.getConfidence()));
            builder.aiDetected(true);
        }
        
        return builder.build();
    }
}
