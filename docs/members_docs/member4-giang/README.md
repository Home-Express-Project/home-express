# Member 4 - Giang: Reviews, Notifications & Outbox Architecture
_Last updated: 2025-11-03_

## Scope
- Customer <-> transport review ecosystem, including moderation workflow and reporting.
- In-app notification center, delivery preferences, and admin outbox monitoring.
- Email + future real-time delivery channels that broadcast booking, quotation, and review events.

## Backend Modules
- Review lifecycle:
  - `controller/ReviewController.java` allows customers and transports to submit reviews once bookings reach `COMPLETED`, attaches ratings across multiple dimensions, and supports response threads.
  - `service/ReviewService.java` enforces participation rules, prevents duplicates, and persists `Review`, `ReviewResponse`, and `ReviewReport`.
  - `controller/ReviewModerationController.java` with `service/ReviewModerationService.java` gives managers filters, approval/rejection actions, and report resolution.
- Notification system:
  - `controller/NotificationController.java` serves paginated notifications, unread counts, preference toggles, and bulk mark-as-read. It depends on `service/NotificationService.java` and `NotificationPreferenceService.java`.
  - `NotificationService#createNotification` is the shared entry point other domains call to push booking, quotation, payment, or system updates.
  - Email delivery runs through `EmailService.java`; OTP mail is already wired, while review/notification templates remain TODO.
- Outbox & async jobs:
  - `controller/AdminOutboxController.java` exposes `/api/v1/admin/outbox` for inspecting queued `OutboxMessage` records, retrying failures, and basic stats.
  - `service/OutboxMessageService.java` implements exponential backoff, retry limits, and housekeeping for failed/pending events.
- Supporting entities & repositories: `Notification`, `NotificationPreference`, `Review`, `ReviewReport`, `ReviewResponse`, `OutboxMessage`, each with dedicated Spring Data repositories under `repository/`.

## Data & Migrations
- Flyway scripts:
  - `V20251102__create_notifications_table.sql` introduces notification storage with preference toggles.
  - `V20251106__create_reviews_module.sql` seeds review/reports tables plus moderation metadata.
- `database/init.sql` contains earlier base tables (`outbox_messages`, `email_templates` stubs, etc.). When adjusting schemas, reflect changes in both the init script and incremental migrations.

## Frontend Touchpoints
- Notification center:
  - `app/notifications/page.tsx` renders tabs, filters, and bulk actions using `hooks/use-notifications.ts` and UI components in `components/notifications/*`.
  - `apiClient` methods: `getNotifications`, `markNotificationAsRead`, `markNotificationsAsRead`, `deleteNotification`, `getUnreadNotificationCount`, `updateNotificationPreferences`.
- Admin moderation:
  - `app/admin/moderation/page.tsx` consumes `/api/v1/admin/reviews` and `/api/v1/admin/reviews/reports`, allowing managers to approve/reject and resolve reports.
  - `app/admin/outbox/page.tsx` lists queued events, triggers retries, deletes poison messages, and supports auto-refresh.
- Review submissions:
  - Customer and transport portals (`app/customer/review/*`, `app/transport/reviews/*`) leverage `apiClient.submitReview`, `apiClient.respondToReview`, and `apiClient.reportReview`.
  - Rating displays reuse components in `components/reviews/*`, visualizing aggregated metrics fed by `ReviewService`.

## Collaboration Points
- Review eligibility depends on Member 2's booking status transitions; any new status must be whitelisted in `ReviewService`.
- Notification routing provides URLs back into Member 2 and Member 3 screens (booking, quotation, vehicle actions). Keep `Notification.ReferenceType` enums synchronized across teams.
- Email/SMS settings integrate with Member 1's admin settings if two-factor or login alerts expand; coordinate on shared preference schema.

## Flow Overview
1. Review pipeline
   - Post-booking completion (from Member 2), customers or transports call `ReviewController#createReview`.
   - `ReviewService` validates booking ownership, prevents duplicates, and stores the review with status `PENDING`.
   - Managers monitor new submissions via `/api/v1/admin/reviews`; `ReviewModerationController#approveReview` or `#rejectReview` finalizes visibility.
   - Parties can respond (`ReviewService#createResponse`) and file reports (`ReviewService#createReport`), escalating to moderation if flagged.
2. Notification delivery
   - Domain services (booking, quotation, payouts) call `NotificationService#createNotification`, which checks preferences and writes to `Notification`.
   - Users fetch notifications via `NotificationController#getNotifications`; summary counts feed the bell icon through `getUnreadCount`.
   - Mark-as-read mutations (`#markAsRead`, `#markMultipleAsRead`) update state, while deletions remove records for that user.
   - Preferences (`NotificationPreferenceService`) determine channel eligibility (in-app, email, SMS future) and quiet hours.
3. Email/OTP
   - `EmailService` currently handles OTP mails (`OtpService#createAndSendOtp`). Future templates for reviews/notifications should leverage the same service, ideally queued through the outbox.
4. Outbox monitoring
   - Background jobs enqueue `OutboxMessage` records for retryable tasks (email/webhook). Managers review them via `/api/v1/admin/outbox`.
   - `OutboxMessageService#retryMessage` requeues eligible events with exponential backoff; `#markAsFailed` captures errors and schedules the next attempt.
   - The admin UI supports deletion of poison messages and displays aggregate stats through `AdminOutboxController#getStats`.

## Testing Guide
- **Automated**
  - Build unit tests for `ReviewService`, `ReviewModerationService`, and `NotificationService` to validate eligibility rules, moderation status transitions, and preference gating.
  - Add integration tests (`@SpringBootTest`) that simulate booking completion -> review creation -> moderation approval, confirming database state and notification emission.
  - Create repository tests for `OutboxMessageRepository` bulk operations (`markAsReadByIds`, retry queries) using a real database profile.
- **Manual API**
  1. Submit reviews as both customer and transport; attempt duplicate/invalid cases to ensure proper errors.
  2. Approve/reject reviews and resolve reports via admin endpoints; inspect audit fields (`moderatedBy`, notes).
  3. Trigger notifications from other modules (e.g., booking status change) and verify they appear for the intended role with correct action URLs.
  4. Toggle notification preferences and confirm suppressed notifications no longer insert records.
  5. Interact with `/api/v1/admin/outbox` to retry and delete messages; observe exponential backoff timestamps.
- **Frontend Smoke**
  - Test notification center filters, bulk mark-as-read, deletion, and preference forms.
  - Walk through admin moderation UI to approve/reject reviews and resolve reports.
  - Validate outbox dashboard auto-refresh and detail modal rendering.

## Open Items & Risks
- Outbox processing is manual; no worker drains `OutboxMessage` records for delivery yet.
- Real-time delivery: WebSocket infrastructure is not yet wired; current implementation is REST polling + manual refresh. Evaluate STOMP or SSE when ready.
- Email templates are hard-coded; migrate to file-based or database-backed templates and use the outbox for reliable delivery.
- Moderation actions do not capture reviewer notes; add rationale fields for auditability.
- Notification pagination returns generic `Page<NotificationResponse>`; the admin UI expects total counts; consider wrapping in a DTO similar to the frontend hook summary shape.
- Soft-delete/restore is absent for notifications, complicating retention policies.
- Outbox retry endpoints do not audit actions; hook into `logAuditAction` on the frontend or emit server-side audit events.
- Reporting dashboard for reviews (rating trends, response SLAs) is still pending; analytics currently live only in transport summaries.

## Alignment & Next Steps
- Build the outbox worker plus shared notification templates so booking/payment hooks from Quy can fan out reliably.
- Add moderation audit fields and `/api/v1/admin/notifications/stats`, coordinating with TriQuan’s audit log service for end-to-end traceability.
- Prototype realtime delivery (SSE/WebSocket) with platform security before rolling out push/mobile payload builders.
