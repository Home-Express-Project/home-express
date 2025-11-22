package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.config.JwtTokenProvider;
import com.homeexpress.home_express_api.dto.request.LoginRequest;
import com.homeexpress.home_express_api.dto.request.RegisterRequest;
import com.homeexpress.home_express_api.dto.response.AuthResponse;
import com.homeexpress.home_express_api.dto.response.UserResponse;
import com.homeexpress.home_express_api.entity.*;
import com.homeexpress.home_express_api.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthService {

    // NOTE: new version - integrate tat ca features moi:
    // - Customer/Transport/Manager entities
    // - UserSession (refresh token)
    // - LoginAttemptService (rate limiting)
    // - Email normalization
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private TransportRepository transportRepository;
    
    @Autowired
    private ManagerRepository managerRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private UserSessionService sessionService;
    
    @Autowired
    private HttpServletRequest httpRequest; // de lay IP, user agent
    
    // register user moi
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        
        // 1. validate email chua ton tai
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        // 2. validate role
        UserRole role;
        try {
            role = UserRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + request.getRole());
        }
        
        // 3. tao User entity
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setIsActive(true);
        user.setIsVerified(false); // can verify email
        user.setLastPasswordChange(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        
        // 4. tao role-specific entity
        switch (role) {
            case CUSTOMER:
                Customer customer = new Customer();
                // NOTE: @MapsId will automatically set customerId from user.userId
                customer.setUser(savedUser);
                customer.setFullName(request.getFullName());
                customer.setPhone(request.getPhone());
                customer.setAddress(request.getAddress());
                customerRepository.save(customer);
                break;
                
            case TRANSPORT:
                Transport transport = new Transport();
                // NOTE: @MapsId will automatically set transportId from user.userId
                transport.setUser(savedUser);
                transport.setCompanyName(request.getCompanyName());
                transport.setPhone(request.getPhone());
                transport.setAddress(request.getAddress());
                transport.setCity(request.getCity());
                transport.setBusinessLicenseNumber(request.getBusinessLicenseNumber());
                transport.setVerificationStatus(VerificationStatus.PENDING);
                transportRepository.save(transport);
                break;
                
            case MANAGER:
                // manager chi tao boi super admin, ko public register
                throw new RuntimeException("Cannot register as MANAGER");
        }
        
        // 5. generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
            savedUser.getUserId(), savedUser.getEmail(), savedUser.getRole().name());
        
        // 6. tao session (refresh token)
        String ipAddress = getClientIp();
        String userAgent = getUserAgent();
        UserSession session = sessionService.createSession(savedUser, ipAddress, userAgent, null);
        String refreshToken = session.getPlainRefreshToken();
        if (!StringUtils.hasText(refreshToken)) {
            throw new IllegalStateException("Failed to generate refresh token");
        }
        
        // 7. tao response
        AuthResponse response = new AuthResponse();
        response.setToken(accessToken); // legacy field
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUser(convertToUserResponse(savedUser));
        response.setMessage("Registration successful. Please verify your email.");
        
        return response;
    }
    
    // login
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        String ipAddress = getClientIp();
        String userAgent = getUserAgent();
        
        // 1. tim user
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
        // 2. check account active
        if (!user.getIsActive()) {
            throw new RuntimeException("Account is disabled. Contact admin.");
        }
        
        // 3. verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        // 7. update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        // 8. generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
            user.getUserId(), user.getEmail(), user.getRole().name());
        
        // 9. tao session moi
        UserSession session = sessionService.createSession(user, ipAddress, userAgent, null);
        String refreshToken = session.getPlainRefreshToken();
        if (!StringUtils.hasText(refreshToken)) {
            throw new IllegalStateException("Failed to generate refresh token");
        }
        
        // 10. response
        AuthResponse response = new AuthResponse();
        response.setToken(accessToken);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUser(convertToUserResponse(user));
        response.setMessage("Login successful");
        
        return response;
    }
    
    // refresh access token
    public AuthResponse refreshToken(String refreshToken) {
        // 1. verify refresh token
        UserSession session = sessionService.verifyRefreshToken(refreshToken)
            .orElseThrow(() -> new RuntimeException("Invalid or expired refresh token"));
        
        // 2. lay user tu session
        User user = session.getUser();
        
        // 3. check account van active
        if (!user.getIsActive()) {
            throw new RuntimeException("Account is disabled");
        }
        
        // 4. generate access token moi
        String newAccessToken = jwtTokenProvider.generateAccessToken(
            user.getUserId(), user.getEmail(), user.getRole().name());
        
        // 5. response - ko generate refresh token moi, giu refresh token cu
        AuthResponse response = new AuthResponse();
        response.setToken(newAccessToken);
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(refreshToken); // same refresh token
        response.setUser(convertToUserResponse(user));
        response.setMessage("Token refreshed successfully");
        
        return response;
    }
    
    // reset password
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setLastPasswordChange(LocalDateTime.now());
        userRepository.save(user);
        
        // revoke tat ca sessions cu (force re-login)
        sessionService.revokeAllUserSessions(user.getUserId(), "password_changed");
    }
    
    // helper - convert User to UserResponse
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setIsActive(user.getIsActive());
        response.setIsVerified(user.getIsVerified());
        return response;
    }
    
    // helper - lay IP cua client
    private String getClientIp() {
        String xForwardedFor = httpRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return httpRequest.getRemoteAddr();
    }
    
    // helper - lay user agent
    private String getUserAgent() {
        return httpRequest.getHeader("User-Agent");
    }
}
