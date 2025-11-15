# TODO List - Member 1: TriQuan

Last Updated: 2025-11-03  
Status: Platform hardening (beta readiness)

## Sprint 14 (Nov 03 - Nov 15) - Security & Audit Foundations
- [ ] (H) Ship the audit log pipeline for admin consoles: add persistence plus a Flyway migration, expose `/api/v1/admin/audit-logs` and wire helpers inside `backend/home-express-api/src/main/java/com/homeexpress/home_express_api/service/AdminUserService.java` and other platform services.
- [ ] (H) Harden authentication cookies for production proxies by updating `backend/home-express-api/src/main/java/com/homeexpress/home_express_api/controller/AuthController.java` (`buildCookie`) to respect `isSecureRequest` and environment-driven SameSite settings; document the toggles in `.env` and `application.properties`.
- [ ] (M) Publish manager-facing session management endpoints wrapping `UserSessionService` (list, revoke single/all) under `/api/v1/admin/users/{id}/sessions` so the admin UI can manage devices.
- [ ] (M) Replace `System.out` instrumentation with structured logging in `UserSessionService` and `LoginAttemptService`, forwarding through the centralized logging configuration.

## Next Up - Q4 Readiness

### Identity UX & MFA
- [ ] (H) Finalize OTP email templates and add throttling guards in `OtpService` plus a per-user attempt counter to close brute-force gaps.
- [ ] (M) Introduce optional secondary factors (email/SMS) by extending `User` contact fields, persisting verification flags, and exposing enrollment APIs.
- [ ] (M) Return device metadata (IP, user agent, `lastSeenAt`) in manager/user profile responses so device management tables render correctly.

### Access Policies & Roles
- [ ] (H) Consolidate `AuthService` and the legacy `AuthServiceNew` to avoid divergent validation paths while keeping the new session features.
- [ ] (M) Add feature-scope permissions to `AdminSettings` and guard manager endpoints with granular `@PreAuthorize` rules instead of broad role gates.
- [ ] (L) Document the RBAC matrix in `docs/security/rbac.md` and generate fixtures for end-to-end tests.

## Backlog / Research
- [ ] (M) Evaluate Redis-backed session revocation and token blacklisting (toggled via `REDIS_URL`) for instant logout.
- [ ] (M) Define audit log retention/export policy (partitioned tables vs. offloading to S3) and automation for purging.
- [ ] (L) Investigate WebAuthn support for manager accounts and required frontend libraries.

## Dependencies & Coordination
- Flyway migration for audit logs must be sequenced with Quy's booking migrations to avoid clashes.
- Notification emails (OTP, security alerts) require Giang's templating work; sync on shared partials.
- The admin frontend already hits `/api/v1/admin/audit-logs`; align on payload contract with Giang before implementation.
