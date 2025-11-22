package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.config.JwtTokenProvider;
import com.homeexpress.home_express_api.config.SecurityConfigProperties;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.entity.UserSession;
import com.homeexpress.home_express_api.repository.UserRepository;
import com.homeexpress.home_express_api.repository.UserSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserSessionService {

    private static final Logger log = LoggerFactory.getLogger(UserSessionService.class);

    // NOTE: service quan ly refresh token sessions
    // luu SHA-256 hash trong DB thay vi plaintext
    // support revocation (logout, security breach, change password)
    
    @Autowired
    private UserSessionRepository sessionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private SecurityConfigProperties securityConfig;
    
    // tao session moi khi login
    public UserSession createSession(User user, String ipAddress, String userAgent, String deviceId) {
        // 1. generate refresh token
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());
        
        // 2. hash token truoc khi luu DB
        String tokenHash = hashToken(refreshToken);
        
        // 3. tao session entity
        UserSession session = new UserSession();
        session.setUser(user);
        session.setRefreshTokenHash(tokenHash);
        session.setExpiresAt(LocalDateTime.now().plusSeconds(
            jwtTokenProvider.getRefreshTokenExpiration() / 1000));
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setDeviceId(deviceId);
        
        // 4. luu vao DB
        UserSession savedSession = sessionRepository.save(session);
        
        // 5. attach plaintext token transiently for caller without persisting it
        savedSession.setPlainRefreshToken(refreshToken);
        
        return savedSession;
    }
    
    // verify refresh token va return session neu valid
    public Optional<UserSession> verifyRefreshToken(String refreshToken) {
        try {
            // 1. validate JWT format
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                return Optional.empty();
            }
            
            // 2. check token type = "refresh"
            String tokenType = jwtTokenProvider.getTokenType(refreshToken);
            if (!"refresh".equals(tokenType)) {
                return Optional.empty();
            }
            
            // 3. hash token de tim trong DB
            String tokenHash = hashToken(refreshToken);
            
            // 4. tim session theo hash
            Optional<UserSession> sessionOpt = sessionRepository.findByRefreshTokenHash(tokenHash);
            if (sessionOpt.isEmpty()) {
                return Optional.empty();
            }
            
            UserSession session = sessionOpt.get();
            
            // 5. check session co valid ko (chua revoke, chua expire)
            if (!session.isValid()) {
                return Optional.empty();
            }
            
            // 6. update last_seen_at
            session.setLastSeenAt(LocalDateTime.now());
            sessionRepository.save(session);
            
            return Optional.of(session);
            
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    // revoke single session (logout)
    public void revokeSession(String sessionId, String reason) {
        Optional<UserSession> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            session.setRevokedAt(LocalDateTime.now());
            session.setRevokedReason(reason);
            sessionRepository.save(session);
        }
    }
    
    // revoke tat ca sessions cua user (change password, security breach)
    public int revokeAllUserSessions(Long userId, String reason) {
        return sessionRepository.revokeAllUserSessions(userId, LocalDateTime.now(), reason);
    }
    
    // lay active sessions cua user (de hien thi "Manage devices")
    public List<UserSession> getActiveSessions(Long userId) {
        return sessionRepository.findActiveSessionsByUserId(userId, LocalDateTime.now());
    }
    
    // cleanup expired sessions - chay scheduled task moi ngay
    @Scheduled(cron = "${security.session-cleanup.cleanup-cron:0 0 3 * * ?}")
    public void cleanupExpiredSessions() {
        int deleted = sessionRepository.deleteExpiredSessions(LocalDateTime.now());
        log.info("Cleaned up {} expired sessions", deleted);
    }
    
    // helper - hash token bang SHA-256
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            
            // convert byte[] to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
