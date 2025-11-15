package com.homeexpress.home_express_api.integration.payout;

import com.homeexpress.home_express_api.entity.TransportPayout;
import com.homeexpress.home_express_api.entity.TransportPayoutItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Default stub implementation that simulates an external bank transfer.
 * Can be configured via {@code PAYOUT_STUB_MODE} environment variable:
 * <ul>
 *   <li>{@code SUCCESS} (default) - always succeed</li>
 *   <li>{@code FAIL_RETRYABLE} - simulate temporary failure (settlements return to READY)</li>
 *   <li>{@code FAIL_FINAL} - simulate non-retryable failure (settlements go ON_HOLD)</li>
 * </ul>
 */
@Service
public class StubExternalPayoutGateway implements ExternalPayoutGateway {

    private static final Logger log = LoggerFactory.getLogger(StubExternalPayoutGateway.class);
    private static final String MODE_ENV = "PAYOUT_STUB_MODE";

    @Override
    public ExternalPayoutResult dispatchPayoutBatch(TransportPayout payout, List<TransportPayoutItem> items) {
        String mode = System.getenv(MODE_ENV);
        if (mode != null) {
            mode = mode.trim().toUpperCase(Locale.ROOT);
        }

        if ("FAIL_RETRYABLE".equals(mode)) {
            String reason = String.format("Stub gateway temporary failure for payout %s at %s",
                    payout.getPayoutNumber(), LocalDateTime.now());
            log.warn("[StubGateway] Simulating retryable failure for payout {} - {}", payout.getPayoutId(), reason);
            return ExternalPayoutResult.failure(reason, true);
        }

        if ("FAIL_FINAL".equals(mode)) {
            String reason = String.format("Stub gateway compliance reject for payout %s at %s",
                    payout.getPayoutNumber(), LocalDateTime.now());
            log.error("[StubGateway] Simulating non-retryable failure for payout {} - {}", payout.getPayoutId(), reason);
            return ExternalPayoutResult.failure(reason, false);
        }

        String reference = String.format("STUB-%d-%s",
                payout.getPayoutId(),
                UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        log.info("[StubGateway] Dispatched payout {} with reference {}", payout.getPayoutId(), reference);
        return ExternalPayoutResult.success(reference);
    }
}
