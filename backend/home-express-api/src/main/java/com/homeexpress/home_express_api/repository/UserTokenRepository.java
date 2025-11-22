package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.TokenType;
import com.homeexpress.home_express_api.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    
    // tim token theo hash va type (verify token)
    Optional<UserToken> findByTokenHashAndTokenType(String tokenHash, TokenType tokenType);
    
    // tim valid token cua user theo type (chua expire, chua consume)
    @Query("SELECT t FROM UserToken t WHERE t.user.userId = :userId " +
           "AND t.tokenType = :type AND t.expiresAt > :now AND t.consumedAt IS NULL " +
           "ORDER BY t.createdAt DESC")
    Optional<UserToken> findValidTokenByUserAndType(Long userId, TokenType type, LocalDateTime now);
    
    // xoa expired tokens (cleanup task)
    @Modifying
    @Query("DELETE FROM UserToken t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(LocalDateTime now);
    
    // xoa tat ca tokens cua user theo type (khi tao token moi)
    @Modifying
    @Query("DELETE FROM UserToken t WHERE t.user.userId = :userId AND t.tokenType = :type")
    int deleteUserTokensByType(Long userId, TokenType type);
    
    // count active tokens cua user
    @Query("SELECT COUNT(t) FROM UserToken t WHERE t.user.userId = :userId " +
           "AND t.tokenType = :type AND t.expiresAt > :now AND t.consumedAt IS NULL")
    long countActiveTokensByUserAndType(Long userId, TokenType type, LocalDateTime now);
}
