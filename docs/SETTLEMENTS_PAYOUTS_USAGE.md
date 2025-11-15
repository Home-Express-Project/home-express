# Settlements & Payouts Usage Guide

## Quick Start

Transport companies can now track settlements and payouts from the earnings dashboard.

### Accessing the Features

1. **Main Earnings Page**: `/transport/earnings`
   - Two new navigation cards link to Settlements and Payouts
   - Shows transaction history and earnings stats

2. **Settlements**: `/transport/earnings/settlements`
   - View all settlement records
   - Filter by status or search by ID
   - See summary cards with counts and amounts

3. **Settlement Details**: `/transport/earnings/settlements/[id]`
   - Full financial breakdown
   - Status timeline
   - Fee details (gateway, commission, platform, adjustments)

4. **Payouts**: `/transport/earnings/payouts`
   - View all payout requests
   - See pending settlements available for payout
   - Filter and search payout records

5. **Payout Details**: `/transport/earnings/payouts/[id]`
   - Complete payout information
   - List of all settlements included
   - Download receipt
   - View status timeline

## Settlement Workflow

### Settlement Statuses

| Status | Meaning | Action |
|--------|---------|--------|
| **PENDING** | Waiting for processing | Wait for review |
| **READY** | Can be included in payout request | Review settlement details |
| **IN_PAYOUT** | Currently being paid out | Check payout details |
| **PAID** | Payment completed | View receipt/details |
| **ON_HOLD** | Payment held due to issue | Review hold reason |
| **DISPUTED** | Under investigation | Review notes and messages |

### Understanding Settlement Details

**Financial Breakdown**:
- **Agreed Price**: The price transport and customer agreed on
- **Total Collected**: Amount customer paid (may differ from agreed price)
- **Gateway Fee**: Payment processor charges
- **Commission**: Platform commission (in basis points, 0.01%)
- **Platform Fee**: Fixed platform service fee
- **Adjustment**: Manual adjustments (positive or negative)
- **Net Amount**: Final amount transport receives

**Collection Mode** (Payment Method):
- `ALL_ONLINE` - Customer paid 100% online
- `CASH_ON_DELIVERY` - Partial online + cash on delivery
- `ALL_CASH` - 100% cash payment
- `PARTIAL_ONLINE` - Partial payment received
- `MIXED` - Multiple payment methods

## Payout Workflow

### Payout Statuses

| Status | Meaning |
|--------|---------|
| **PENDING** | Payout request created |
| **PROCESSING** | Being processed by payment system |
| **COMPLETED** | Successfully paid to transport account |
| **FAILED** | Payment failed (check failure reason) |

### Creating a Payout

1. Go to Payouts page
2. See "Pending Settlements" alert (if any available)
3. Click "View Details" to review settlements
4. Settlements must be in READY status to be included
5. Create payout request (via backend API or future UI button)
6. Monitor payout status on Payouts page

### Download Receipt

On payout detail page:
- Click "Download Receipt" button
- Saves as `.txt` file with:
  - Payout information
  - All included settlements
  - Total amount
  - Timestamps

## Filtering & Search

### Settlement Page
- **Status Filter**: PENDING, READY, IN_PAYOUT, PAID, ON_HOLD, DISPUTED
- **Search**: By booking ID or settlement ID
- **Summary Cards**: Show totals for each status

### Payout Page
- **Status Filter**: PENDING, PROCESSING, COMPLETED, FAILED
- **Search**: By payout number
- **Stats**: Monthly totals and pending amount

## Important Notes

### ON_HOLD Settlements
- Check the **hold reason** displayed in red
- Cannot be included in payouts until released
- Contact support if hold was unexpected

### DISPUTED Settlements
- Shows dispute notes
- May include links to dispute threads
- Payment withheld during investigation
- Check messages for resolution updates

### Payment Timeline
1. Booking completed → Settlement created (PENDING)
2. After processing period → Settlement ready (READY)
3. Payout request created → Settlements included (IN_PAYOUT)
4. Payout processed → Payment completed (PAID)

## Troubleshooting

**"Không tìm thấy quyết toán nào" (No settlements found)**
- Check filter status - may be filtering incorrectly
- Search term might not match any settlements
- May need to wait for settlements to be created after booking completion

**"Không tìm thấy rút tiền nào" (No payouts found)**
- Might not have made any payout requests yet
- Check pending settlements alert to create a payout

**Missing Settlement**
- Settlement may still be in processing (PENDING status)
- Check settlement summary counts match expected
- May be in ON_HOLD or DISPUTED status

## Statistics & Reports

### Settlement Summary
Shows counts and totals for:
- Pending settlements
- Ready settlements (available for payout)
- In-payout settlements
- Paid settlements
- On-hold settlements

### Monthly Stats (Payouts Page)
- Count of payouts processed this month
- Total amount paid this month

## Next Steps

From settlement/payout screens:
- Click settlement → See full details → May link to booking/dispute
- Click payout → See included settlements → Click settlement for details
- Navigate back to earnings page for transaction history

## Support

If encountering issues:
1. Check settlement/payout status carefully
2. Review reasons for ON_HOLD or DISPUTED status
3. Contact support with settlement ID or payout number
4. Provide screenshots of issue if needed
