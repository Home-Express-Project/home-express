# TODO List - Member 2: Quy

Last Updated: 2025-11-03  
Status: Booking & payments stabilization

## Sprint 14 (Nov 03 - Nov 15) - Intake Ops & Payment Hooks
- [ ] (H) Stand up `/api/v1/admin/sessions` endpoints (list/detail/items/rerun/publish) on top of `IntakeSessionService` to satisfy the admin Next.js proxies under `frontend/home-express/app/api/v1/admin/sessions`.
- [ ] (H) Stream AI intake logs over SSE by bridging `HybridAIDetectionOrchestrator` diagnostics to `/api/v1/admin/sessions/{id}/events` with authentication and retry handling.
- [ ] (M) Stabilize and simplify payment flows using only bank transfer + cash (no external payment gateways).
- [ ] (M) Trigger booking and quotation notifications (status changes, accepted quote, deposit paid) via `NotificationService` hooks in `BookingService`, `QuotationService`, and `ContractService`.
- [ ] (M) Publish a manager timeline endpoint (`/api/v1/admin/bookings/{id}/timeline`) that reads `BookingStatusHistoryRepository` for oversight and audits.

## Next Up - Customer & Transport Experience

### Booking lifecycle polish
- [ ] (H) Add validation and audit logging around forced quote overrides from the admin intake session UI, persisting decisions to a dedicated table.
- [ ] (M) Normalize `priceBreakdown` JSON blobs into a `booking_price_components` table for finance analytics and settlement accuracy.
- [ ] (M) Implement an SLA job that auto-expires stale quotations, reopens bookings to transports, and emits notifications.

### Payments & Settlements
- [ ] (H) Build payout batching (group eligible `BookingSettlement` rows, write `TransportPayout` records) and surface summary counters in `AdminPayoutController`.
- [ ] (M) Generate contract/payment PDFs after successful deposits and queue delivery through the outbox for Giang's email worker.
- [ ] (L) Provide sandbox gateway emulators for automated tests (mock / callbacks with deterministic payloads).

## Backlog / Research
- [ ] (M) Introduce intake rate limits and cost controls (per-user quotas, Redis/S3 caching of detection results).
- [ ] (M) Define a contract e-signature workflow (provider selection, signature storage, customer/transport UX).
- [ ] (L) Explore route optimization APIs for dispatch once Quang finalizes capacity data.

## Dependencies & Coordination
- Audit logging for admin overrides depends on TriQuan's platform pipeline.
- Pricing DTO changes require coordination with Quang to keep quotation maths in sync.
- Notification copy and email templates for booking/payment events come from Giang; confirm payload fields early.
