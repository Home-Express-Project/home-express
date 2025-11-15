package com.homeexpress.home_express_api.service;

import org.springframework.stereotype.Service;

/**
 * Service for calculating commission and platform fees Based on
 * commission_rules table in the database
 */
@Service
public class CommissionService {

    private static final int DEFAULT_COMMISSION_BPS = 500;
    /**
     * Manual bank transfers (no third-party gateway) cost 0 VND by default.
     * Update this constant if the bank applies a per-transaction fee.
     */
    private static final long BANK_TRANSFER_FIXED_FEE_VND = 0L;

    /**
     * Calculate platform fee based on commission rate Formula: (agreedPrice ×
     * commissionRateBps) / 10000
     *
     * @param agreedPriceVnd agreed price in VND
     * @param transportId transport ID (for future transport-specific rates)
     * @return platform fee in VND
     */
    public long calculatePlatformFee(long agreedPriceVnd, Long transportId) {
        int commissionRateBps = getCommissionRateBps(transportId);
        return (agreedPriceVnd * commissionRateBps) / 10000;
    }

    /**
     * Get commission rate in basis points for a transport Currently returns
     * default rate (500 bps = 5%) Future: Query commission_rules table for
     * transport-specific rates
     *
     * @param transportId transport ID
     * @return commission rate in basis points
     */
    public int getCommissionRateBps(Long transportId) {
        return DEFAULT_COMMISSION_BPS;
    }

    /**
     * Calculate payment gateway fee.
     * In this version all customer payments are cash or manual bank transfer
     * without any third-party gateway markup, so this method always returns 0 VND.
     * It is kept as a hook in case banks introduce per-transfer fees later.
     *
     * @param amountVnd payment amount in VND (currently unused)
     * @return gateway fee in VND (0 in this version)
     */
    public long calculateGatewayFee(long amountVnd) {
        return BANK_TRANSFER_FIXED_FEE_VND;
    }

    /**
     * Calculate gateway fee for multiple payments
     *
     * @param totalAmountVnd total payment amount in VND
     * @param paymentCount number of payments
     * @return total gateway fee in VND
     */
    public long calculateGatewayFeeForMultiplePayments(long totalAmountVnd, int paymentCount) {
        int effectiveCount = Math.max(paymentCount, 1);
        return BANK_TRANSFER_FIXED_FEE_VND * effectiveCount;
    }

    /**
     * Calculate net amount to transport after all deductions.
     * Formula: totalCollected - gatewayFee - platformFee + adjustment.
     * In this version, gatewayFeeVnd is expected to be 0 because there is no
     * gateway markup for cash or manual bank transfers.
     *
     * @param totalCollectedVnd total collected from customer
     * @param gatewayFeeVnd payment gateway fees (0 in this version)
     * @param platformFeeVnd platform commission
     * @param adjustmentVnd manual adjustments (can be negative)
     * @return net amount to transport in VND
     */
    public long calculateNetToTransport(long totalCollectedVnd, long gatewayFeeVnd,
            long platformFeeVnd, long adjustmentVnd) {
        return totalCollectedVnd - gatewayFeeVnd - platformFeeVnd + adjustmentVnd;
    }
}
