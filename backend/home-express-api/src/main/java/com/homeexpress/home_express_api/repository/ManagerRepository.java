package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Long> {
    
    // tim theo employee ID
    Optional<Manager> findByEmployeeId(String employeeId);
    
    // check employee ID da ton tai
    boolean existsByEmployeeId(String employeeId);
    
    // tim theo department
    // List<Manager> findByDepartment(String department);
    
    // tim manager theo user ID
    Optional<Manager> findByUser_UserId(Long userId);
}
