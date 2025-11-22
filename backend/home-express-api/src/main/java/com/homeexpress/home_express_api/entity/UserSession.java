package com.homeexpress.home_express_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
public class UserSession {

    // NOTE: quan ly refresh token cho JWT auth
    // luu hash SHA-256 thay vi plaintext de bao mat
    // co revocation support de logout/security breach
    
    @Id
    @Column(name = "session_id", length = 36)
    private String sessionId; // UUID
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @JsonIgnore // sensitive - ko dc expose
    @NotBlank
    @Size(max = 64)
    @Column(name = "refresh_token_hash", nullable = false, length = 64)
    private String refreshTokenHash; // SHA-256 hash (64 hex chars)
    
    @Transient
    @JsonIgnore
    private transient String plainRefreshToken; // raw token returned to caller only
    
    // session lifecycle
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt; // cap nhat moi khi dung refresh token
    
    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // 7 days tu luc tao
    
    // revocation - de logout hoac force logout
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
    
    @Column(name = "revoked_reason", columnDefinition = "TEXT")
    private String revokedReason; // vd: "user_logout", "security_breach", "admin_action"
    
    // security tracking - biet ai dang dung token o dau
    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress; // IPv4 hoac IPv6
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent; // browser/app info
    
    @Size(max = 255)
    @Column(name = "device_id", length = 255)
    private String deviceId; // client-provided device ID
    
    @PrePersist
    protected void onCreate() {
        // tu generate UUID neu chua co
        if (sessionId == null) {
            sessionId = java.util.UUID.randomUUID().toString();
        }
    }
    
    // helper methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isRevoked() {
        return revokedAt != null;
    }
    
    public boolean isValid() {
        return !isExpired() && !isRevoked();
    }
    
    // constructor
    public UserSession() {}

    // getters setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public void setRefreshTokenHash(String refreshTokenHash) {
        this.refreshTokenHash = refreshTokenHash;
    }
    
    public String getPlainRefreshToken() {
        return plainRefreshToken;
    }

    public void setPlainRefreshToken(String plainRefreshToken) {
        this.plainRefreshToken = plainRefreshToken;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getRevokedReason() {
        return revokedReason;
    }

    public void setRevokedReason(String revokedReason) {
        this.revokedReason = revokedReason;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
