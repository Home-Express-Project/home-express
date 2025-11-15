# Member 1 - TriQuan: Platform & Access Architecture
_Last updated: 2025-11-03_

## Scope
- Identity, session, and permission model for the Spring Boot monolith under `backend/home-express-api`.
- Manager tooling surfaced in `app/admin/*` and global auth UX in `app/login`, `app/signup`, `app/forgot-password`, `app/reset-password`.
- Shared platform services (notification preferences, device management, outbox oversight) that other members build upon.

## Backend Structure
- `controller/AuthController.java`, `config/SecurityConfig.java`, and `config/JwtAuthenticationFilter.java` expose cookie-based JWT auth with stateless Spring Security. Access tokens live for `jwt.access-token-expiration-ms`, refresh tokens ride in HTTP-only cookies and are validated by `JwtTokenProvider`.
- `service/AuthService.java` orchestrates registration across roles, encodes passwords, persists `User`, `Customer`, or `Transport`, and emits paired access/refresh tokens. `UserSessionService.java` hashes refresh tokens, stores device metadata, supports revocation, and performs nightly cleanup.
- `service/LoginAttemptService.java` tracks failed logins in `LoginAttempt` with IP/email rate limiting and temporary account lockouts. The scheduled cleanup runs at 02:00 daily.
- `controller/UserController.java` plus `service/UserService.java` deliver profile management, password changes, avatar uploads (`FileStorageService.java` writes to `/uploads/avatars`), and admin-level CRUD. `AdminUsersController.java` and `AdminSettingsController.java` layer manager-only views on top, backed by `AdminUserService.java` and `AdminSettingsService.java`.
- `controller/AdminDashboardController.java` and `service/DashboardService.java` aggregate user and transport metrics for the manager dashboard, sourcing from `UserRepository` and `TransportRepository`.
- Cross-cutting helpers: `OtpService.java` + `EmailService.java` drive password reset OTP mail; `NotificationPreferenceService.java` lets users tune delivery channels; `AdminOutboxController.java` with `OutboxMessageService.java` monitors asynchronous jobs.

## Persistence & Config
- Core tables live in `database/init.sql`; recent deltas are in Flyway scripts such as `V20251102__create_notifications_table.sql` (notification storage) and `V20251110__create_intake_sessions_tables.sql` (AI-assisted intake audit trail).
- Auth layers depend on `users`, `user_sessions`, `login_attempts`, `otp_codes`, `notifications`, `admin_settings`, and `outbox_messages`. All are wired through Spring Data repositories inside `repository/`.
- Environment is driven via `.env` merged by `application.properties`: MySQL (`spring.datasource.*`), Redis cache (`spring.data.redis.*`) for token and rate limit support, and mail credentials (`spring.mail.*`) for OTP delivery. JWT secrets must be populated in `JWT_SECRET`.

## Frontend Touchpoints
- `frontend/home-express/contexts/auth-context.tsx` bootstraps auth state, calling `/api/v1/auth/me` through the shared HTTP client and routing users by role. `frontend/home-express/lib/api-client.ts` centralizes auth/session requests and refresh handling.
- Admin surfaces under `frontend/home-express/app/admin/*`:
  - `page.tsx` renders dashboard metrics via `/api/v1/admin/dashboard/stats`.
  - `settings/page.tsx` edits manager profile, MFA toggles, and SMTP config through `/api/v1/admin/settings`.
  - `users/page.tsx` and `users/[id]/page.tsx` hit `/api/v1/admin/users` for CRUD, activation, and reset flows.
  - `outbox/page.tsx` monitors `/api/v1/admin/outbox` for queued jobs, retries, and deletions.
  - `profile/page.tsx` displays consolidated manager contact info fetched from `/api/v1/admin/profile`.
- Next.js API routes (see `frontend/home-express/app/api/_lib/backend.ts`) proxy requests to the Spring backend; audit helpers already POST to `/api/v1/admin/audit-logs`, which still needs server-side support.
- End-user auth flows reuse shared UI atoms in `frontend/home-express/components/ui/*`, invoking `apiClient.login`, `apiClient.register`, `apiClient.logout`, and password reset helpers.

## Flow Overview
1. Registration hits `AuthController#register`, which validates payload, persists `User` plus role profile (`Customer`/`Transport`), then returns access + refresh tokens (cookies are set by `buildAuthCookies`).
2. Login routes through `AuthService#login`; it records attempts (`LoginAttemptService`), creates/updates `UserSession`, and issues new JWTs in HTTP-only cookies.
3. Every API request passes `JwtAuthenticationFilter`, which checks token validity and wires the authenticated principal. Refresh cookies can be exchanged at `/api/v1/auth/refresh`.
4. Logout requests call `AuthService#logout` and `UserSessionService#revokeSession` to invalidate refresh tokens and clear cookies.
5. Admin-only paths under `/api/v1/admin/**` rely on `@PreAuthorize` checks configured in `SecurityConfig`; the frontend admin screens consume these endpoints to manage users, settings, dashboard metrics, sessions, and outbox items.
6. Ancillary flows (password reset, OTP, device management) reuse the same infrastructure: `OtpService` issues codes over email, `UserSessionService` exposes active sessions, and `NotificationPreferenceService` governs downstream alerts.

## Testing Guide
- **Automated**
  - Add unit tests around `AuthService`, `UserSessionService`, and `LoginAttemptService` to validate credential hashing, session hashing, and lockout timing (example `AuthServiceTest`, `UserSessionServiceTest`).
  - Use `@WebMvcTest` to cover `AuthController` and `AdminUsersController`, mocking dependent services to verify response structures and security annotations.
- **Manual API**
  1. Register and log in via Postman/cURL; confirm `access_token`/`refresh_token` cookies and that `user_sessions` stores hashed tokens.
  2. Attempt invalid logins repeatedly to verify `LoginAttemptService` blocks after five failures and unlocks after the configured window.
  3. Call `/api/v1/auth/refresh` with only refresh cookie; ensure new access token is issued and rate-limited sessions remain valid.
  4. Logout (`/api/v1/auth/logout`) and confirm refresh attempts fail; check DB for `revoked_at` timestamp.
  5. Exercise admin endpoints (`/api/v1/admin/users`, `/api/v1/admin/settings`, `/api/v1/admin/outbox`) with MANAGER vs CUSTOMER roles to validate RBAC.
  6. Trigger password reset (`/api/v1/auth/forgot-password`) and verify OTP email log plus expiry after five minutes.
- **Frontend Smoke**
  - Run `pnpm dev`; login as CUSTOMER, TRANSPORT, MANAGER to confirm redirects and persistence; inspect cookies for `SameSite=None` behaviour under HTTPS during staging.

## Operational Notes
- `AuthController#buildCookie` still emits `secure=false` cookies; tie the flag to `isSecureRequest` (and document env overrides) before deploying behind HTTPS.
- Admin audit logging endpoints are not yet implemented even though the UI posts to `/api/v1/admin/audit-logs`; delivering the storage + controller is part of the current sprint.
- Manager device management endpoints (`/api/v1/admin/users/{id}/sessions`) are not wired yet; they should wrap `UserSessionService` once exposed.
- `LoginAttemptService` and `UserSessionService` write to stdout; switch to structured logging once the centralized configuration lands.
- Redis remains optional in `application.properties`; enabling it allows future token blacklisting without DB hits.
- Email sending is synchronous. Consider delegating through the outbox to avoid blocking login flows if SMTP stalls.
- WebSocket notifications are not yet implemented; all notification reads route through REST in `NotificationController.java`.

## Alignment & Next Steps
- Ship the audit log pipeline (Flyway + `/api/v1/admin/audit-logs`) so Giang’s admin tooling can persist actions and so booking overrides are traceable.
- Harden cookie handling (`secure`, `SameSite`) and document the deployment knobs before staging behind reverse proxies.
- Expose session/device metadata APIs, finish OTP email templates and throttling, and plan MFA rollout in concert with booking/payment notifications.
