package com.homeexpress.home_express_api.service;

import com.homeexpress.home_express_api.dto.response.UserListResponse;
import com.homeexpress.home_express_api.entity.*;
import com.homeexpress.home_express_api.exception.ResourceNotFoundException;
import com.homeexpress.home_express_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransportRepository transportRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * Get all users with their profiles
     */
    public UserListResponse getAllUsersWithProfiles(
            UserRole role,
            Boolean status,
            String search,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        // Build specification for filtering
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("isActive"), status));
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("email")), searchPattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> usersPage = userRepository.findAll(spec, pageable);

        // Fetch profiles for each user
        List<Map<String, Object>> usersWithProfiles = usersPage.getContent().stream()
                .map(this::getUserWithProfileMap)
                .collect(Collectors.toList());

        // Build response
        UserListResponse response = new UserListResponse();
        response.setUsers(usersWithProfiles);
        response.setTotalUsers((int) usersPage.getTotalElements());
        response.setTotalPages(usersPage.getTotalPages());
        response.setCurrentPage(page + 1); // Convert back to 1-based for frontend
        
        // Add pagination metadata
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current_page", page + 1);
        pagination.put("total_pages", usersPage.getTotalPages());
        pagination.put("total_items", usersPage.getTotalElements());
        pagination.put("items_per_page", size);
        response.setPagination(pagination);

        return response;
    }

    /**
     * Get user with profile by ID
     */
    public Map<String, Object> getUserWithProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return getUserWithProfileMap(user);
    }

    /**
     * Helper method to map user with profile
     */
    private Map<String, Object> getUserWithProfileMap(User user) {
        Map<String, Object> result = new HashMap<>();

        // User data
        Map<String, Object> userData = new HashMap<>();
        userData.put("user_id", user.getUserId());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole());
        userData.put("is_active", user.getIsActive());
        userData.put("is_verified", user.getIsVerified());
        userData.put("created_at", user.getCreatedAt());
        userData.put("updated_at", user.getUpdatedAt());

        result.put("user", userData);

        // Fetch profile based on role and map to simple Map to avoid circular references
        Map<String, Object> profileData = new HashMap<>();

        switch (user.getRole()) {
            case CUSTOMER:
                Customer customer = customerRepository.findByUser_UserId(user.getUserId()).orElse(null);
                if (customer != null) {
                    profileData.put("customer_id", customer.getCustomerId());
                    profileData.put("full_name", customer.getFullName());
                    profileData.put("phone", customer.getPhone());
                    profileData.put("address", customer.getAddress());
                    profileData.put("date_of_birth", customer.getDateOfBirth());
                    profileData.put("avatar_url", customer.getAvatarUrl());
                    profileData.put("preferred_language", customer.getPreferredLanguage());
                    profileData.put("created_at", customer.getCreatedAt());
                    profileData.put("updated_at", customer.getUpdatedAt());
                }
                break;

            case TRANSPORT:
                Transport transport = transportRepository.findByUser_UserId(user.getUserId()).orElse(null);
                if (transport != null) {
                    profileData.put("transport_id", transport.getTransportId());
                    profileData.put("company_name", transport.getCompanyName());
                    profileData.put("business_license_number", transport.getBusinessLicenseNumber());
                    profileData.put("tax_code", transport.getTaxCode());
                    profileData.put("phone", transport.getPhone());
                    profileData.put("address", transport.getAddress());
                    profileData.put("city", transport.getCity());
                    profileData.put("district", transport.getDistrict());
                    profileData.put("ward", transport.getWard());
                    profileData.put("verification_status", transport.getVerificationStatus());
                    profileData.put("verified_at", transport.getVerifiedAt());
                    profileData.put("total_bookings", transport.getTotalBookings());
                    profileData.put("completed_bookings", transport.getCompletedBookings());
                    profileData.put("cancelled_bookings", transport.getCancelledBookings());
                    profileData.put("average_rating", transport.getAverageRating());
                    profileData.put("created_at", transport.getCreatedAt());
                    profileData.put("updated_at", transport.getUpdatedAt());
                }
                break;

            case MANAGER:
                Manager manager = managerRepository.findByUser_UserId(user.getUserId()).orElse(null);
                if (manager != null) {
                    profileData.put("manager_id", manager.getManagerId());
                    profileData.put("full_name", manager.getFullName());
                    profileData.put("phone", manager.getPhone());
                    profileData.put("employee_id", manager.getEmployeeId());
                    profileData.put("department", manager.getDepartment());
                    profileData.put("permissions", manager.getPermissions());
                    profileData.put("created_at", manager.getCreatedAt());
                    profileData.put("updated_at", manager.getUpdatedAt());
                }
                break;
        }

        result.put("profile", profileData);

        return result;
    }

    /**
     * Activate user
     */
    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        user.setIsActive(true);
        userRepository.save(user);
    }

    /**
     * Deactivate user
     */
    @Transactional
    public void deactivateUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        user.setIsActive(false);
        userRepository.save(user);
        
        // TODO: Log deactivation reason in audit log
    }

    /**
     * Check if user has active bookings
     */
    public Map<String, Object> checkUserActiveBookings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        long activeBookingsCount = 0;

        if (user.getRole() == UserRole.CUSTOMER) {
            // Count customer's active bookings
            Customer customer = customerRepository.findByUser_UserId(userId).orElse(null);
            if (customer != null) {
                activeBookingsCount = bookingRepository.countByCustomerIdAndStatusIn(
                    customer.getCustomerId(),
                    Arrays.asList(
                        BookingStatus.PENDING,
                        BookingStatus.QUOTED,
                        BookingStatus.CONFIRMED,
                        BookingStatus.IN_PROGRESS
                    )
                );
            }
        } else if (user.getRole() == UserRole.TRANSPORT) {
            // Count transport's active bookings
            Transport transport = transportRepository.findByUser_UserId(userId)
                    .orElse(null);
            if (transport != null) {
                activeBookingsCount = bookingRepository.countByTransportIdAndStatusIn(
                    transport.getTransportId(),
                    Arrays.asList(
                        BookingStatus.PENDING,
                        BookingStatus.QUOTED,
                        BookingStatus.CONFIRMED,
                        BookingStatus.IN_PROGRESS
                    )
                );
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hasActiveBookings", activeBookingsCount > 0);
        result.put("activeBookingsCount", activeBookingsCount);

        return result;
    }
}
