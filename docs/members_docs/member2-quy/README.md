# Member 2 - Quy: Booking, Quotation & Contract Architecture
_Last updated: 2025-11-03_

## Scope
- Customer booking lifecycle, transport quotations, contract execution, and payment orchestration across the Spring Boot backend.
- Intake tools (AI scan, OCR, manual cataloguing) that build booking payloads before quotation.
- Customer and transport-focused dashboard surfaces under `frontend/home-express/app/customer/*` and `app/transport/*`, plus supporting admin insights.

## Backend Modules
- Booking orchestration:
  - `controller/BookingController.java` exposes CRUD + history endpoints guarded by role-aware checks.
  - `service/BookingService.java` persists `Booking`, `BookingItem`, and `BookingStatusHistory`, validates addresses via `VnProvince/District/Ward` repositories, computes distance metadata, and emits status transitions.
  - `service/BookingItemService.java` (supporting class) handles item-level updates for quotation prep.
- Quotation & contract stack:
  - `controller/QuotationController.java` and `service/QuotationService.java` accept transport bids (`SubmitQuotationRequest`), calculate composite pricing, enforce validity windows, and allow customers to accept/reject.
  - `service/ContractService.java` generates deposit-bearing contracts once a quotation is accepted, issues contract numbers, and tracks dual signatures.
  - `service/PaymentService.java`, `PayoutService.java`, and `SettlementService.java` manage  initialization, idempotent payment records, gateway fee calculations, and payout ledgers for transports.
- Intake & saved items:
  - `controller/IntakeController.java` coordinates `HybridAIDetectionOrchestrator`, `IntakeOcrService`, `IntakeTextParsingService`, and `IntakeSessionService` for AI-driven item detection from images, documents, or pasted text.
  - `controller/CustomerSavedItemController.java` with `service/customer/CustomerSavedItemService.java` persists curated item templates customers can reuse during booking.
- Customer & transport surfaces: `controller/customer/CustomerDashboardController.java`, `controller/customer/CustomerPaymentController.java`, `controller/transport/TransportJobController.java`, etc., reuse the services above for role-specific listings and analytics.

## Domain Data & Migrations
- Core entities: `Booking`, `BookingItem`, `BookingStatusHistory`, `Quotation`, `Contract`, `Payment`, `BookingSettlement`, `TransportPayout`, `IntakeSession`, `IntakeSessionItem`, `CustomerSavedItem`, and supporting enums (`BookingStatus`, `QuotationStatus`, `ContractStatus`...).
- Latest Flyway migrations in `src/main/resources/db/migration`:
  - `V20251103__create_quotations_table.sql`, `V20251104__quotation_procedure_and_history.sql`, `V20251105__reinstate_contracts_fk.sql` for quoting/contract lineage.
  - `V20251110__create_intake_sessions_tables.sql`, `V20251111__create_product_models_table.sql`, `V20251112__create_customer_saved_items_table.sql`, and `V20251113__add_declared_value_to_saved_items.sql` for intake tooling.
  - Payment summaries rely on database views (see `database/init.sql` definitions such as `booking_payment_summary`) consumed by `PaymentService`.
- All repositories live under `repository/`, providing pagination and status-aware queries leveraged by SWR hooks on the frontend.

## Frontend Touchpoints
- Customer flows:
  - `frontend/home-express/app/customer/bookings/page.tsx` + `frontend/home-express/hooks/use-bookings.ts` list bookings with live status filtering against `/api/v1/bookings`.
  - `frontend/home-express/app/customer/quote/*` and `frontend/home-express/components/quotation/*` consume `apiClient.getBookingQuotations` and `apiClient.acceptQuotation`.
  - Intake screens (`frontend/home-express/app/customer/scan`, `app/customer/saved-items`) call `apiClient.parseText`, `apiClient.ocrImages`, and saved-item endpoints for AI-assisted payload creation.
  - Payments: `frontend/home-express/app/customer/checkout/page.tsx` and `frontend/home-express/components/payment/*` leverage `apiClient.initializeDeposit`, `apiClient.getPaymentSummary`, and show  metadata.
- Transport views:
  - `frontend/home-express/app/transport/jobs/page.tsx` uses `useAvailableBookings` for open jobs, while `app/transport/quotations/page.tsx` surfaces active bids via `useMyQuotations`.
- Admin oversight surfaces:
  - `frontend/home-express/app/admin/page.tsx` presents booking/settlement summaries from `/api/v1/admin/dashboard/stats`.
  - `frontend/home-express/app/admin/outbox/page.tsx` manages `/api/v1/admin/outbox` retries and deletions.
  - `frontend/home-express/app/admin/sessions/[id]/page.tsx` (intake QA/override) proxies to `/api/v1/admin/sessions/*` via Next.js API routes—backend handlers still need to be implemented.
- Next.js API routes under `frontend/home-express/app/api/v1/` proxy booking/intake calls (`frontend/home-express/app/api/_lib/backend.ts` handles cookie forwarding); keep their paths aligned with Spring controllers.

## Process Snapshot
1. Customer drafts a booking (`BookingController#createBooking`), optionally assembling items through intake endpoints.
2. Status history records `PENDING`; notifications are handed off to Member 4's service via `NotificationService`.
3. Transports fetch available jobs, submit quotations (`QuotationService#submitQuotation`), and include itemized components in `priceBreakdown`.
4. Customer accepts a quotation -> `QuotationService#acceptQuotation` transitions other bids to `REJECTED`, creates a `Contract` draft, and generates deposit requirements.
5. Deposit payment kicks off through `PaymentService#initializePayment`, referencing `PaymentConfig` + . Booking/Payout summaries update via DB views.
6. Contract signatures finalize booking; subsequent state transitions (e.g., `IN_PROGRESS`, `COMPLETED`) trigger settlement, payout, and review flows.

### Detailed Booking Lifecycle
1. Intake
   - `IntakeController#parseText`/`#ocr`/`#merge` populate `IntakeSession` + `IntakeSessionItem`.
   - Customers save frequently moved items via `CustomerSavedItemController`, so the booking form can auto-fill later.
2. Booking creation
   - `BookingService#createBooking` validates addresses (`VnLocationService`), stores `Booking` + `BookingItem`, and emits a `BookingStatusHistory` entry.
   - Optional extras (notes, preferred slots) are persisted for transports to review.
3. Quotation phase
   - `TransportJobController` lists available bookings filtered by capacity/pricing readiness.
   - `QuotationService#submitQuotation` checks vehicle ownership, calculates total (vehicle + distance + category + extras), and marks the quotation `PENDING`.
   - Expiry windows are enforced (`expiresAt`), and `NotificationService` can inform customers.
4. Customer decision
   - `QuotationService#getDetailedQuotations` returns comparisons; frontend shows breakdown charts.
   - Accepting a quotation invokes `QuotationService#acceptQuotation`, which:
     - Marks the chosen quotation as `ACCEPTED`, others as `REJECTED`.
     - Calls `ContractService#createContract` if no contract exists.
     - Updates booking status to `QUOTED`/`CONFIRMED` as appropriate and records history.
5. Contracting & payment
   - `ContractService#signContract` captures signatures and IP stamps.
   - `PaymentService#initializePayment` generates  payloads; callbacks (future) or manual confirmations move payments to `SUCCESS`.
   - `SettlementService` and `PayoutService` aggregate payment data for finance dashboards.
6. Completion
   - `BookingService#updateStatus` drives transitions to `IN_PROGRESS`/`COMPLETED`, ensuring valid state changes.
   - Completion triggers review invites (Member 4) and eligibility for payouts.

## Testing Guide
- **Automated**
  - Implement unit tests for `BookingService`, `QuotationService`, `ContractService`, and `PaymentService` with mocked repositories to verify state transitions, pricing math, and idempotent payment creation.
  - Add integration tests using `@SpringBootTest` + Testcontainers MySQL to cover booking -> quotation -> contract flow end-to-end, asserting status history, related entities, and cleanup jobs.
  - Utilize Flyway validation in CI (`mvn -Dflyway.cleanDisabled=true flyway:validate`) to ensure migrations stay consistent.
- **Manual API**
  1. Create bookings via `/api/v1/bookings` (customer auth); inspect DB tables (`booking`, `booking_item`, `booking_status_history`).
  2. Submit multiple quotations as different transports; verify rejection/acceptance statuses and expiry.
  3. Accept a quotation and check contract creation, payment summary view (`booking_payment_summary`), and notifications.
  4. Execute deposit payment (`/api/v1/payments/init`) and confirm idempotency by repeating with same key.
  5. Update booking statuses through manager endpoints; ensure invalid transitions return `400/409`.
  6. Run intake endpoints with sample images/text to check `IntakeSession` persistence and saved items operations.
- **Frontend Smoke**
  - On `pnpm dev`, walk through:
    - Customer: create booking (with AI scan), review quotations, accept, pay deposit, track status.
    - Transport: add vehicle, configure pricing, bid on booking, monitor analytics.
    - Manager: inspect admin bookings, settlements, and cheque payment dashboards.

## Integration Points
- Depends on Member 3 for vehicle capacity/pricing validation (`VehicleRepository`, `VehiclePricingRepository`) when matching transports.
- Relies on Member 4 for notifications and review prompts; make sure `Notification.ReferenceType` values remain in sync.
- Authentication, session checks, and rate limiting come from Member 1's platform services (`AuthenticationUtils`, `SecurityConfig`).

## Open Items & Risks
- Admin intake review UI already calls `/api/v1/admin/sessions/**` for session detail, rerun, and publish workflows; backend endpoints and SSE streams still need to be delivered.
- Price breakdown JSON is stored as a string; consider normalizing into a structured table for analytics.
- Contract PDFs and digital signatures are not yet produced; current implementation only stores metadata.
- Payment webhooks are stubbed; settlement logic assumes synchronous confirmation. Plan integration or a polling job through the outbox.
- Intake AI orchestrator currently caches detection results in memory; evaluate moving session artifacts into Redis or S3 for resilience.
- Coordinate future migrations with Member 3 before altering category or vehicle references embedded in booking items.

## Alignment & Next Steps
- Ship `/api/v1/admin/sessions` (including SSE logs and forced quote actions) so the admin QA screens stop depending on stubs.
- Complete / integrations and hook booking/quotation notifications in partnership with Giang’s delivery stack.
- Normalize pricing data (breakdowns, payouts) alongside Quang’s vehicle/category pricing updates to keep downstream analytics in sync.
