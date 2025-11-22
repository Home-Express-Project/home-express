package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    
    // tim session theo refresh token hash
    Optional<UserSession> findByRefreshTokenHash(String tokenHash);
    
    // tim tat ca sessions cua user (active hoac expired)
    List<UserSession> findByUserUserId(Long userId);
    
    // tim active sessions cua user (chua revoke, chua expire)
    @Query("SELECT s FROM UserSession s WHERE s.user.userId = :userId " +
           "AND s.revokedAt IS NULL AND s.expiresAt > :now")
    List<UserSession> findActiveSessionsByUserId(Long userId, LocalDateTime now);
    
    // xoa sessions expired
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :now")
    int deleteExpiredSessions(LocalDateTime now);
    
    // revoke tat ca sessions cua user (dung khi change password)
    @Modifying
    @Query("UPDATE UserSession s SET s.revokedAt = :now, s.revokedReason = :reason " +
           "WHERE s.user.userId = :userId AND s.revokedAt IS NULL")
    int revokeAllUserSessions(Long userId, LocalDateTime now, String reason);
    
    // count active sessions cua user
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.user.userId = :userId " +
           "AND s.revokedAt IS NULL AND s.expiresAt > :now")
    long countActiveSessionsByUserId(Long userId, LocalDateTime now);
}
