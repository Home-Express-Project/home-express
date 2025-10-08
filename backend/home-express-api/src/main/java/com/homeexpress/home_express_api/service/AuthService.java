package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.config.JwtTokenProvider;
import com.homeexpress.home_express_api.dto.request.LoginRequest;
import com.homeexpress.home_express_api.dto.request.RegisterRequest;
import com.homeexpress.home_express_api.dto.response.AuthResponse;
import com.homeexpress.home_express_api.dto.response.UserResponse;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.entity.UserRole;
import com.homeexpress.home_express_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // Dang ky user moi
    public AuthResponse register(RegisterRequest request) {
        // Kiem tra email da ton tai chua
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Kiem tra phone da ton tai chua
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already exists");
        }

        // Tao user moi
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash password
        user.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
        user.setIsActive(true);
        user.setIsVerified(false);

        // Luu vao database
        User savedUser = userRepository.save(user);

        // Tao JWT token
        String token = jwtTokenProvider.generateToken(
            savedUser.getUserId(),
            savedUser.getEmail(),
            savedUser.getRole().name()
        );

        // Tao response
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUser(convertToUserResponse(savedUser));
        response.setMessage("Registration successful");

        return response;
    }

    // Dang nhap
    public AuthResponse login(LoginRequest request) {
        // Tim user theo email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Kiem tra password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Kiem tra account co active khong
        if (!user.getIsActive()) {
            throw new RuntimeException("Account is disabled");
        }

        // Tao JWT token
        String token = jwtTokenProvider.generateToken(
            user.getUserId(),
            user.getEmail(),
            user.getRole().name()
        );

        // Tao response
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUser(convertToUserResponse(user));
        response.setMessage("Login successful");

        return response;
    }

    // Reset password
    public void resetPassword(String email, String newPassword) {
        // Tim user theo email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update password moi
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Helper method: chuyen User entity thanh UserResponse DTO
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole().name());
        response.setAvatar(user.getAvatar());
        response.setIsActive(user.getIsActive());
        response.setIsVerified(user.getIsVerified());
        return response;
    }
}
