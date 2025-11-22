package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.CustomerSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerSettingsRepository extends JpaRepository<CustomerSettings, Long> {
}

