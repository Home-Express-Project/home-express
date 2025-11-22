package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.RateCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RateCardRepository extends JpaRepository<RateCard, Long> {

    List<RateCard> findByTransportId(Long transportId);

    List<RateCard> findByTransportIdAndIsActiveTrue(Long transportId);

    List<RateCard> findByTransportIdAndCategoryIdAndIsActiveTrue(Long transportId, Long categoryId);
}

