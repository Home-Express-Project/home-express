package com.homeexpress.home_express_api.repository;

import com.homeexpress.home_express_api.entity.TransportWallet;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransportWalletRepository extends JpaRepository<TransportWallet, Long> {

    Optional<TransportWallet> findByTransportId(Long transportId);
}
