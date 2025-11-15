# TODO List - Member 3: Quang

Last Updated: 2025-11-03  
Status: Fleet & catalog stabilization

## Sprint 14 (Nov 03 - Nov 15) - Catalog & Verification Hardening
- [ ] (H) Replace in-memory pagination in `AdminTransportController` with repository-level queries (filters plus sort) so verification queues scale.
- [ ] (H) Persist transport onboarding documents via `FileStorageService`, linking uploaded credentials to `Transport` records and exposing download URLs.
- [ ] (M) Deliver vehicle utilization analytics (`/api/v1/transport/vehicles/analytics`) backed by `TransportAnalyticsService`.
- [ ] (M) Fill out category/pricing admin APIs (restore, archive, audit hooks) to complement the React admin screens.
- [ ] (M) Seed baseline `ProductModel` fixtures through Flyway and provide an admin sync script for intake suggestions.

## Next Up - Pricing Intelligence

### Dynamic pricing
- [ ] (H) Introduce surge multiplier schedules stored in a dedicated table and expose resolution helpers used by Quy's quotation engine.
- [ ] (M) Build a pricing simulation endpoint that previews cost impact when vehicle/category rules change.
- [ ] (L) Prototype demand-forecast inputs (daily aggregates, vehicle capacity) for future machine learning hooks.

### Data infrastructure
- [ ] (M) Automate Vietnam location dataset refresh (external script plus migration) instead of manual SQL updates.
- [ ] (L) Add ETag/caching headers to `LocationController` responses and document CDN expectations.

## Backlog / Research
- [ ] (M) Plan multi-language support for `ProductModel` records (translation table plus admin editing).
- [ ] (M) Capture vehicle maintenance telemetry and surface in dashboards.
- [ ] (L) Evaluate third-party telematics integrations to enrich availability signals.

## Dependencies & Coordination
- Surge schedule data must align with Quy's pricing DTOs in `QuotationService`.
- Document uploads depend on TriQuan's auth/cookie hardening to safely accept multipart forms.
- Verification notifications flow through Giang; keep `Notification.ReferenceType` and status enums stable.
