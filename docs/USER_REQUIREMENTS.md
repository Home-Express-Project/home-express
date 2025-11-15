# User Requirements
_Cap nhat lan cuoi: 2025-11-01 _

## Tong quan

## I. Overview

### 1. System Context & Objectives
Home Express so hoa toan bo quy trinh van chuyen nha tu luc khach tao booking den khi job hoan tat. Frontend su dung Next.js 15.5.4 + React 19 + Tailwind CSS 4 (`frontend/home-express`) phuc vu cac portal Guest/Customer/Transport/Manager. Backend Spring Boot 3.5.5 (`backend/home-express-api`) cung cap REST `/api/v1/**`, tich hop email/OTP, AI intake/OCR, Redis 7, Flyway/MySQL 8,  +  ( chi con trong code cu, khong dung trong luong escrow). Docker Compose khoi tao MySQL/Redis cho local dev (`docker-compose.yml`).

Muc tieu chinh
1. **Digital booking & KYC pipeline**: tu signup, OTP, intake, booking creation, quotation/contract cho den job completion.
2. **Minh bach & chu dong cho doi tac**: Transport quan ly Rate Card, job status, wallet/payout, reporting/export; Manager kiem soat audit/outbox.
3. **An toan tai chinh**: Payment escrow 30/70, settlement, double-entry wallet, dispute freeze, audit trail, backup.
4. **Kha nang mo rong**: Dynamic Pricing, Rate Card Snapshot, ExportService, SSE job updates, AI intake reuse.

### 2. Implementation pillars tu Main Workflow
- **Rate Card & Dynamic Pricing**: Transport phai tu cau hinh Rate Card + snapshot khi tao quotation (Stage 3.2-3.4). FE: `app/transport/pricing/**`; BE: `CategoryPricingController`, `VehiclePricingController`, `QuotationService`, `RateCardSnapshotService` (phai bo sung).
- **Escrow, settlement & wallet**: Flow deposit/remaining -> `BookingSettlement` -> `TransportWallet`. Yeu cau double-entry (`WalletService.credit/debit/freeze`) + `TransportFinanceController` (`/earnings`, `/transactions`) + `TransportPayoutController`.
- **Evidence & export**: Booking/quotation/payment status history, incident evidence, export CSV/XLSX (`ExportService`, `ReportController`) cho Wallet/Settlement/Payout/Job history.
- **Resilience & notifications**: Outbox + `NotificationPreferenceService`, SSE job events (`TransportEventController`), audit log cho cac thao tac quan trong.

## Actor snapshot
| #   | Actor     | Mo ta nhanh                                                                                                       |
| --- | --------- | ----------------------------------------------------------------------------------------------------------------- |
| 1   | Guest     | Chua dang nhap; tim hieu dich vu, dang ky Customer/Transport, co the tao intake session truoc khi tao booking.    |
| 2   | Customer  | Da dang ky va dang nhap; tao/quan ly booking, xem quotation/contract, thanh toan escrow, danh gia dich vu.        |
| 3   | Transport | Doi tac van chuyen da KYC/KYB; cau hinh Rate Card, gui quotation, thuc hien job, quan ly tai chinh/payout/export. |
| 4   | Manager   | Admin he thong; duyet KYC, giam sat booking/payment/payout, quan ly outbox, review, setting bao mat.              |

### Lien ket module
| Actor     | Mo ta ngan                                          | Frontend lien quan                                                                                                    | Backend lien quan                                                                                                                                                                                                                                    |
| --------- | --------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Guest     | Signup/login/forgot cho Customer & Transport        | `app/page.tsx`, `app/signup/**`, `app/login/**`, `app/forgot-password/page.tsx`                                       | `AuthController`, `OtpService`, `UserSessionService`, `LocationController`, `IntakeController`                                                                                                                                                       |
| Customer  | Dat dich vu va theo doi luong chinh                 | `app/customer/**`, `app/checkout/**`, `app/notifications/page.tsx`, `app/customer/quote/**`, `app/customer/review/**` | `BookingController`, `QuotationController`, `CustomerDashboardController`, `CustomerPaymentController`, `NotificationController`, `ReviewController`                                                                                                 |
| Transport | Quan ly doi xe, Rate Card, quotation, job, earnings | `app/transport/**` (dashboard/jobs/active/pricing/vehicles/earnings/analytics/settings)                               | `TransportJobController`, `TransportDashboardController`, `CategoryPricingController`, `VehicleController`, `TransportFinanceController`, `TransportPayoutController`, `TransportSettingsController`, `TransportEventController`                     |
| Manager   | Portal admin                                        | `app/admin/**` (dashboard/users/sessions/bookings/outbox/moderation/settings)                                         | `AdminDashboardController`, `AdminUsersController`, `AdminBookingController`, `AdminIntakeSessionController`, `AdminSettlementController`, `AdminPayoutController`, `ReviewModerationController`, `AdminOutboxController`, `AdminSettingsController` |

## Guest (Chua dang nhap)

### Mo ta
Khach tham quan va nguoi vua dang ky (Customer hoac Transport). Duoc phuc vu boi landing page + multi-step signup co OTP.

### Nhu cau chinh
- **Landing & CTA**: Trang chu hien thong tin gia tri, CTA toi `/signup` hoac `/login`.
- **Signup co OTP**: Chon vai tro (Customer/Transport), nhap thong tin ca nhan + cong ty, upload giay to (Transport) va xac minh OTP email (`AuthController.register`, `OtpService`).
- **Dang nhap/duy tri phien**: `AuthController.login/logout/refresh` tao HttpOnly cookie `access_token` + `refresh_token`; guest phai dang nhap truoc khi vao `/customer`, `/transport`, `/admin`.
- **Quen mat khau**: OTP email + token reset (`forgotPassword`, `verifyOtp`, `resetPassword`).
- **Pre-booking intake**: Guest co the tao `IntakeSession` (upload anh, OCR) truoc, he thong luu `sessionId` cho Manager QA va Customer reuse.

### Rang buoc & ghi chu
- Tat ca API auth nam duoi `/api/v1/auth/**`, bat buoc dung cookie HttpOnly + CSRF guard.
- Flow signup Transport co nhieu buoc (company -> compliance -> security); phai hoan tat truoc khi gui ho so KYC.
- Guest chi duoc tao intake tam thoi, du lieu phai duoc gan vao user sau khi dang ky thanh cong.

---

## Customer

### Mo ta
Nguoi dat dich vu tren portal `/customer`. Moi trang duoc bao ve boi `useAuth` (role CUSTOMER) va su dung cac API Booking/Quotation/Payment/Notification/Review.

### Nhu cau chinh
- **Booking + Intake**: Tao booking 4 buoc (dia diem, hang hoa, thoi gian, review) tai `app/customer/bookings/create/page.tsx`. Co the lay du lieu tu AI intake (`IntakeController.merge/parse/ocr`) va kho `CustomerSavedItemController`.
- **Status timeline & evidence**: `BookingController.getBookingById/history` phan tach cac trang thai tu `PENDING -> QUOTED -> NEGOTIATING -> CONFIRMED -> DEPOSIT_PAID -> ... -> SETTLED` nhu Main Workflow 4.1. UI hien timeline + lich su chuyen doi.
- **Quotation & dam phan**: Tu dashboard co the xem toan bo quotation, chap nhan/tu choi/counter (`QuotationController.accept/reject/counter`). Hien `suggestedPrice`, `quotedPrice`, `validityPeriod`, reason.
- **Contract & thanh toan escrow**: Sau khi chon quotation, Customer ky hop dong (digital) va thanh toan dat coc 30% thong qua `CustomerPaymentController.initiateDeposit` (chi ). Phan con lai 70% sau khi job gan hoan tat, theo doi qua `getPaymentSummary` + `PaymentStatus`.
- **Notifications**: `app/notifications/page.tsx` thong bao su kien booking/quotation/payment/dispute. Customer co the chinh thong bao qua `NotificationPreferenceService`.
- **Review & dispute**: Sau `COMPLETED`, Customer tao review (`ReviewController.createReview`), phan hoi, bao cao vi pham de Manager giai quyet (Stage 3.8). Dispute job/settlement duoc mo qua UI va goi `IncidentController`.
- **Saved items & ho so**: Quan ly danh muc hang hoa, ho so ca nhan, bao mat (doi mat khau, 2FA) tai `CustomerSettingsController`.

### Rang buoc & ghi chu
- `BookingController.createBooking` chi cho role CUSTOMER; neu user role khac phai dieu huong ve portal phu hop.
- Chi dat review khi `booking.status == COMPLETED`; `ReviewService` enforce 1 review/booking.
- Dam phan gia co thoi han (validity) -> FE phai dem nguoc va het han thi chuyen trang thai `EXPIRED`.
- Payments hien chi chap nhan  cho escrow (khong mo ). Tat ca callback phai chuyen booking sang `DEPOSIT_PAID` truoc khi thong bao transport.
- Evidence (anh, bien ban) phai upload vao timeline de ho tro Stage 7 & dispute.

---

## Transport

### Mo ta
Doi tac van chuyen da duoc duyet KYC. Portal `/transport/**` bao gom dashboard, job board, Rate Card, vehicles, analytics, earnings, settings.

### Nhu cau chinh
- **Onboarding & readiness**: Sau signup, Transport hoan tat ho so, tai lieu, cau hinh thong tin cong ty (`TransportSettingsController`). Chi khi `transport.status == READY_TO_QUOTE` moi xem booking can bao gia (Stage 2).
- **Rate Card & snapshot**: Chinh sua bang gia theo category/vehicle tai `app/transport/pricing/**` goi `CategoryPricingController` + `VehiclePricingController`. Khi tao quotation, he thong phai tao snapshot (`RateCardSnapshotService`) de dong bang thong tin gia tai thoi diem gui bao gia.
- **Dynamic pricing & negotiation**: `TransportDashboardController.submitQuotation` + `QuotationService` ap dung `suggestedPrice`, `priceBreakdown`, `confidenceScore`. UI ho tro counter/timeline cho cac trang thai `PENDING/NEGOTIATING/COUNTERED/ACCEPTED/REJECTED/WITHDRAWN`.
- **Job execution & SSE**: `app/transport/active/[id]/page.tsx` dung `useSSE` vao `/transport/jobs/{bookingId}/events` de cap nhat cac buoc `DRIVER_ON_THE_WAY -> LOADING -> IN_TRANSIT -> UNLOADING -> COMPLETED`. Transport can upload evidence (anh, bien ban) cho moi buoc.
- **Tai chinh & wallet**: `TransportFinanceController` cung cap `/earnings/stats` + `/transactions`. `TransportSettlementController` va `TransportPayoutController` cho phep xem settlement READY/ON_HOLD, yeu cau payout, xem chi tiet `netToTransportVnd`. Khi double-entry wallet duoc add, Transport thay so du + lich su `TransportWalletTransaction`.
- **Reporting & export**: Can co `ExportService` + `ReportController` de tao file CSV/XLSX (Wallet transaction, Settlement detail, Payout history, Job history) va thong bao khi san sang.
- **Reputation & notifications**: Tra loi review (`ReviewController.respondToReview`), nhan thong bao `QUOTATION_RECEIVED`, `PAYMENT_RELEASED`, `DISPUTE_OPENED`, ... thong qua module chung.

### Rang buoc & ghi chu
- Tat ca `/api/v1/transport/**` bat buoc role TRANSPORT va phai kiem tra `transport.status`. Neu chua `READY_TO_QUOTE` thi khong duoc xem booking can bao gia.
- Moi quotation phai bao gom snapshot Rate Card, thoi han, ly do dieu chinh gia. BE phai luu snapshot truoc khi luu `Quotation`.
- SSE subscription khong duoc ket thuc ngay (hien `TransportEventController` can implement event streaming). FE phai fallback polling neu mat ket noi.
- Wallet/payout thong ke phai dua tren `BookingSettlement` + ledger double-entry, khong tru tong tu view khac.
- Export job lon phai day qua background job + thong bao (khong block request).

---

## Manager

### Mo ta
Vai tro quan tri he thong (`role MANAGER`). Portal `app/admin/**` gom dashboard, user/session, booking timeline, payout, outbox, moderation, settings.

### Nhu cau chinh
- **KYC/KYB & access control**: Tham dinh ho so Transport, chuyen trang thai `REGISTERED -> PENDING_VERIFICATION -> APPROVED/REJECTED -> READY_TO_QUOTE`. Chan transport chua duyet bang middleware/interceptor.
- **Platform health dashboard**: Tong quan user, booking funnel, KPI, top transport (`AdminDashboardController`, `DashboardService`).
- **Booking/payment/settlement oversight**: Xem timeline, trang thai, quotation, payment, settlement/payout (`AdminBookingController`, `AdminSettlementController`, `AdminPayoutController`). Co nut freeze/unfreeze wallet khi dispute (`WalletService.freezeWallet`).
- **Moderation & danh tieng**: Duyet review/report (`ReviewModerationController`), theo doi incident/evidence (`IncidentController`). Moi thao tac bat buoc ghi `AuditLog`.
- **Outbox & resilience**: Theo doi `OutboxMessage` (NEW/PROCESSING/SENT/FAILED), retry, bulk action, nho he thong thong bao neu backlog tang (`AdminOutboxController`).
- **Reporting/export management**: Theo doi job `ExportService`, cap quyen tai file, ho tro doi tac khi file loi.
- **Settings & bao mat**: Cap nhat cau hinh he thong (SMTP, backup, maintenance, 2FA), thong bao `SYSTEM_ALERT`, `OUTBOX_FAILURE`.

### Rang buoc & ghi chu
- Moi controller `admin/**` duoc bao ve boi `@PreAuthorize("hasRole(''MANAGER'')")` va can them audit trail.
- Manager la nguoi duy nhat duoc thay doi review status, revoke session, trigger payout batch, freeze/unfreeze wallet -> FE phai goi `logAuditAction` truoc khi call API.
- Outbox hien chua co worker rieng, Manager phai chu dong retry va theo doi SLA gui email/OTP.
- SSE (intake session, job events) chi duoc expose sau khi Manager duyet noi dung (compliance, an toan thong tin).

---

## Phu thuoc lien ma tran
- Chuoi Customer Booking -> Transport Quotation -> Manager giam sat: `BookingService`, `QuotationService`, `ReviewService` phai dong bo trang thai voi bang `booking_progress_events`.
- Rate Card vs Quotation: moi thay doi `CategoryPricing` phai thong bao transport cap nhat (notification + audit) va snapshot phai duoc tao trong `QuotationService.createQuotation`.
- Payment & Settlement => Wallet/Payout: `CustomerPaymentController` tao `BookingSettlement`, `SettlementService` tinh `platformFee`/`netToTransport`, `WalletService` (sau khi them) tao giao dich credit/debit, `PayoutService` tra tien.
- Notification & Outbox: su dung chung cho 3 actor, moi thay doi schema (`V20251102__create_notifications_table.sql`, `NotificationPreference`) phai thong bao toan team.
- Intake pipeline: Guest/Customer tao session (`IntakeController`), Manager QA (`AdminIntakeSessionController`), Transport nhan du lieu item truoc khi bao gia.
- Reporting/Export: `ExportService` dung background job + Apache POI/OpenCSV, ket qua luu tru tam thoi (Cloud Storage hoac local) va thong bao qua `NotificationService`.
