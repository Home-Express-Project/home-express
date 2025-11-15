package com.homeexpress.home_express_api.util;

import com.homeexpress.home_express_api.entity.Customer;
import com.homeexpress.home_express_api.entity.Transport;
import com.homeexpress.home_express_api.entity.User;
import com.homeexpress.home_express_api.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;

/**
 * Utility helpers for extracting the currently authenticated user from the
 * Spring Security {@link Authentication} object. Several existing controllers
 * assumed that {@code authentication.getName()} returned an email address,
 * but the JWT filter stores the numeric user id as the principal. Centralising
 * the resolution logic avoids those incorrect assumptions and prevents 500s
 * caused by failed lookups.
 */
public final class AuthenticationUtils {

    private AuthenticationUtils() {
    }

    /**
     * Resolve the authenticated user's id if possible without hitting the database.
     *
     * @param authentication the authentication context
     * @return the user id, or {@code null} if it cannot be derived directly
     */
    public static Long getUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long id) {
            return id;
        }

        if (principal instanceof Customer customer) {
            if (customer.getUser() != null) {
                return customer.getUser().getUserId();
            }
            return customer.getCustomerId();
        }

        if (principal instanceof Transport transport) {
            if (transport.getUser() != null) {
                return transport.getUser().getUserId();
            }
            return transport.getTransportId();
        }

        if (principal instanceof User user) {
            return user.getUserId();
        }

        if (principal instanceof String raw) {
            try {
                return Long.parseLong(raw);
            } catch (NumberFormatException ignored) {
                // Fall through to email based resolution.
            }
        }

        return null;
    }

    /**
     * Resolve the authenticated {@link User}, using the provided repository as needed.
     *
     * @param authentication the authentication context
     * @param userRepository repository used to load user information
     * @return the authenticated user
     * @throws RuntimeException if the user cannot be resolved
     */
    public static User getUser(Authentication authentication, UserRepository userRepository) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Authenticated user not found");
        }

        Long userId = getUserId(authentication);
        if (userId != null) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        }

        String email = authentication.getName();
        if (StringUtils.hasText(email)) {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        }

        throw new RuntimeException("Authenticated user not found");
    }
}
