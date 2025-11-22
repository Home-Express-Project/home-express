package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.RateCardSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RateCardSnapshotRepository extends JpaRepository<RateCardSnapshot, Long> {

    Optional<RateCardSnapshot> findByQuotationId(Long quotationId);
}

