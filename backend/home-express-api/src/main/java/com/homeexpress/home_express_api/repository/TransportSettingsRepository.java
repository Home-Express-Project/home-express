package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.TransportSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransportSettingsRepository extends JpaRepository<TransportSettings, Long> {
}

