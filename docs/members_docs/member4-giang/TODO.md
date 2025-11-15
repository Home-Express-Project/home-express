# TODO List - Member 4: Giang

Last Updated: 2025-11-03  
Status: Notifications & moderation polish

## Sprint 14 (Nov 03 - Nov 15) - Delivery & Moderation Polish
- [ ] (H) Implement an outbox worker that dequeues pending messages (`OutboxMessageService#getPendingMessages`) and sends templated emails via `EmailService`.
- [ ] (H) Add maintainable templates for review/payment notifications under `backend/home-express-api/src/main/resources/templates/notifications` and wire them through `EmailService`.
- [ ] (M) Provide `/api/v1/admin/notifications/stats` summarizing unread counts, delivery latency, and failure buckets for the admin dashboard.
- [ ] (M) Capture moderation rationale (notes, actor) when approving/rejecting reviews in `ReviewModerationService` and surface it in responses.
- [ ] (M) Support soft-delete and restore of notifications (add audit columns, expose endpoints) to satisfy retention policy.

## Next Up - Realtime & Insights

### Realtime delivery
- [ ] (H) Design a WebSocket/SSE channel for in-app notifications that reuses TriQuan's JWT auth and gracefully falls back to polling.
- [ ] (M) Build push payload builders for future mobile clients (topic mapping, collapse keys, action URLs).
- [ ] (L) Extend `NotificationPreferenceService` with quiet hours/snooze windows and UI toggles.

### Review intelligence
- [ ] (M) Create `/api/v1/admin/reviews/metrics` aggregating rating trends, NPS, and response SLAs.
- [ ] (M) Run sentiment analysis on reviews (store tags/score) to highlight risky interactions for moderators.
- [ ] (L) Generate a weekly digest email of flagged reviews routed through the outbox.

## Backlog / Research
- [ ] (M) Pick a templating engine (Thymeleaf vs Mustache) for notifications and document partial conventions.
- [ ] (M) Define escalation rules for repeat offenders (e.g., auto-suspend after N approved reports).
- [ ] (L) Explore SMS provider integration for high priority alerts.

## Dependencies & Coordination
- Moderation/audit logging depends on TriQuan delivering the audit log service.
- Booking/payment events from Quy must include URLs and metadata to enrich notifications.
- Transport verification messaging requires Quang's enum stability for reference types and statuses.
