# Member 3 - Quang: Vehicle, Catalog & Pricing Architecture
_Last updated: 2025-11-03_

## Scope
- Transport fleet onboarding, verification, and lifecycle management for the backend.
- Pricing engines for vehicle-distance tiers and item category surcharges that feed the quotation pipeline.
- Catalog data (categories, sizes, product models) and Vietnam location datasets that power intake and booking UX.

## Backend Modules
- Vehicle lifecycle:
  - `controller/VehicleController.java` exposes CRUD, status updates, and ownership checks. `service/VehicleService.java` validates Vietnamese license plates, enforces role ownership, and stores physical specs.
  - `repository/VehicleRepository.java` supports queries by transport, status, and license plate hash; `VehicleStatus` enum governs availability.
- Pricing engines:
  - `controller/VehiclePricingController.java` + `service/VehiclePricingService.java` manage tiered distance pricing per transport with overlap detection and automatic deactivation of superseded rules.
  - `controller/CategoryPricingController.java` + `service/CategoryPricingService.java` capture per-category + size multipliers (fragile, disassembly, heavy fees) and enforce exclusive active ranges.
  - `service/CommissionService.java` and `PriceHistoryRepository.java` record commission basis points and history for analytics.
- Catalog administration:
  - `controller/CategoryManagementController.java`, `service/CategoryService.java`, and `service/ProductModelService.java` create categories, attach sizes, and seed standard product templates that intake can reference.
  - `controller/ProductModelController.java` serves curated catalog entries to the frontend.
- Location data:
  - `controller/LocationController.java` and `service/VnLocationService.java` wrap province/district/ward lookups stored in `VnProvince`, `VnDistrict`, `VnWard`.
- Supporting analytics: `service/TransportAnalyticsService.java` and `TransportDashboardService.java` aggregate utilization metrics, average ratings, and revenue figures for transport dashboards.
- AI assist: `service/ai/*` (GPT-5 mini vision stack) provides detection services consumed by Member 2's intake pipeline; maintain mappings in `constants/AIPrompts.java`.

## Data Model & Migrations
- Entities: `Vehicle`, `VehiclePricing`, `Category`, `Size`, `CategoryPricing`, `PriceHistory`, `ProductModel`, `TransportSettings`, `AdminSettings` (shared), and `NotificationPreference` (consumed for transport alerts).
- Recent migrations:
  - `V20251107__create_vn_location_tables.sql` and `V20251108__augment_vn_location_tables.sql` seed hierarchical location data.
  - `V20251111__create_product_models_table.sql` stores canonical model specs used during intake.
  - `V20251114__add_brand_model_value_to_booking_items.sql` ensures booking items retain catalog references coming from this module.
  - Earlier vehicle/pricing schemas are part of `database/init.sql`; review before altering unique constraints (e.g., `license_plate_compact`).
- Pricing overlap detection relies on SQL functions defined in the migrations (`hasOverlappingActivePricing` queries). Any schema adjustments must preserve those checks.

## Frontend Touchpoints
- Transport workspace:
  - `app/transport/vehicles/page.tsx` with `hooks/use-vehicles.ts` drives fleet CRUD, modals in `components/vehicle/*`, and status changes via `apiClient.updateVehicleStatus`.
  - `app/transport/pricing/vehicles` and `app/transport/pricing/categories` consume `useVehiclePricing()` / `useCategoryPricing()` hooks, providing live validation before posting to `/api/v1/transport/pricing/*`.
  - `app/transport/profile` displays aggregated stats from `TransportAnalyticsService`.
- Admin views:
  - `app/admin/transports/verification/page.tsx` queries `AdminTransportController` to approve or reject providers, relying on `TransportSettingsService`.
  - `app/admin/moderation` (shared with Member 4) references transport ratings sourced from this domain.
- Shared catalog:
  - Intake and quotation flows fetch categories via `apiClient.getCategories`, `apiClient.getProductModels`, and location data through `apiClient.getProvinces/Districts/Wards`.
  - Components under `components/catalog/*` render category selectors with size-based pricing hints.

## Collaboration Points
- Pricing outputs (`VehiclePricingResponse`, `CategoryPricingResponse`) are consumed by Member 2's `QuotationService`; any schema change requires DTO alignment.
- Transport verification flows produce notifications via Member 4; ensure `Notification.ReferenceType.TRANSPORT` stays stable.
- Auth hooks and role checks rely on Member 1's security layer (`SecurityConfig`, `AuthenticationUtils`). Keep controller annotations synchronized with platform policies.

## Operational Flow
1. Transport onboarding
   - Registration (handled by Member 1) creates a `Transport` record. `TransportSettingsService` captures business info and documents.
   - `AdminTransportController` exposes verification queues so managers can approve (`VerificationStatus.APPROVED`) or reject applications.
2. Fleet setup
   - Transport owners use `VehicleController` via the `/transport/vehicles` UI to add vehicles.
   - `VehicleService#createVehicle` normalizes license plates, enforces uniqueness, and defaults status to `ACTIVE`.
   - Editing or status updates (`VehicleService#updateVehicle`, `#updateVehicleStatus`) ensure only owners can mutate their fleet.
3. Pricing configuration
   - Vehicle pricing: `VehiclePricingController` posts to `VehiclePricingService#createVehiclePricing`, which deactivates overlapping rules and stores tiered distance rates.
   - Category pricing: `CategoryPricingController` + `CategoryPricingService#createCategoryPricing` apply per-category modifiers (fragile, disassembly, heavy).
   - Both services expose `getCurrentActivePricing` for Member 2's quotation engine to fetch applicable rates at runtime.
4. Catalog support
   - Admins manage categories/sizes through `CategoryManagementController`; `CategoryService` ensures unique names and soft deletes.
   - Standard product dimensions live in `ProductModel` records, fetched by intake flows to speed up booking forms.
5. Location data usage
   - `VnLocationService` serves provinces/districts/wards for booking and transport forms. Updates propagate to all clients via `LocationController`.
6. Analytics feedback
   - `TransportAnalyticsService` aggregates booking outcomes and ratings (from Member 4) to show performance on `/transport/profile`.
   - Any pricing or vehicle changes are logged to `PriceHistory` for audit and potential rollback.

## Testing Guide
- **Automated**
  - Add unit tests for `VehicleService`, `VehiclePricingService`, `CategoryPricingService`, and `CategoryService` to assert validation (license plate format, overlap detection, uniqueness).
  - Create repository tests using an embedded database (H2 configured with MySQL mode) to validate custom queries (`hasOverlappingActivePricing`, `findActiveByTransportAndVehicleType`).
  - Include integration tests for `LocationController` to ensure full province/district/ward hierarchies load from migrations.
- **Manual Checks**
  1. Add/edit/delete vehicles via `/api/v1/transport/vehicles`; verify owner constraints and status changes.
  2. Configure vehicle and category pricing; confirm overlapping rule prevention and that active pricing matches frontend displays.
  3. Manage categories/sizes/product models; ensure defaults propagate to booking intake and that soft-deleted items hide from selectors.
  4. Hit `/api/v1/locations/provinces`, `/districts`, `/wards` to confirm dataset integrity and caching.
  5. Review analytics endpoints (`/api/v1/transport/analytics/*`) for expected aggregates after running sample bookings.
- **Frontend Smoke**
  - Transport dashboard: add fleets, tweak pricing, and ensure analytics update in UI.
  - Admin transport verification: approve/reject flows and confirm status toggles propagate to booking availability.

## Open Items & Risks
- `AdminTransportController` slices verification lists in memory; move pagination/filtering down to repository queries before traffic increases.
- Transport onboarding documents are not stored via `FileStorageService` yet; managers cannot download proof assets.
- Surge multipliers (`peakHourMultiplier`, `weekendMultiplier`) exist in the entity but are not yet surfaced in the UI; coordinate with frontend to expose configuration.
- Vehicle telemetry (capacity utilization, maintenance schedule) is stubbed; analytics currently rely on basic counts. Consider integrating actual job metrics from bookings.
- Product model catalog lacks localization beyond defaults; expanding to multi-language requires extending `ProductModel` fields and admin tooling.
- AI detection prompts live in code; move to configuration (e.g., DB or feature flag) to avoid redeploy when tuning.
- Ensure location dataset refresh procedure is documented; currently there is no automated update pipeline after the initial migration.

## Alignment & Next Steps
- Implement repository-level filtering/pagination for transport verification, persist onboarding documents, and expose utilization analytics APIs.
- Coordinate surge schedule data with Quy's quotation engine before introducing dynamic multipliers or simulation endpoints.
- Automate Vietnam location dataset refresh and seed baseline product models so intake suggestions stay current.
