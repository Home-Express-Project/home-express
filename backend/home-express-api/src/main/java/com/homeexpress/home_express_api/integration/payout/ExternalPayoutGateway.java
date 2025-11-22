package com.homeexpress.home_express_api.integration.payout;

import com.homeexpress.home_express_api.entity.TransportPayout;
import com.homeexpress.home_express_api.entity.TransportPayoutItem;
import java.util.List;

/**
 * Abstraction for sending payout batches to an external banking/gateway system.
 * Implementations can call a bank API, queue a message, or stub a response locally.
 */
public interface ExternalPayoutGateway {

    /**
     * Dispatch a payout batch to the external system.
     *
     * @param payout payout metadata (bank snapshot, totals, identifiers)
     * @param items  settlement items batched inside the payout
     * @return result describing whether the dispatch succeeded and any reference/failure details
     */
    ExternalPayoutResult dispatchPayoutBatch(TransportPayout payout, List<TransportPayoutItem> items);
}
