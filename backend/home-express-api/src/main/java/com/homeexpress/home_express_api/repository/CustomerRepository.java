package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    // tim customer theo phone
    Optional<Customer> findByPhone(String phone);
    
    // check phone da ton tai chua
    boolean existsByPhone(String phone);
    
    // tim customer theo user ID
    Optional<Customer> findByUser_UserId(Long userId);
}
