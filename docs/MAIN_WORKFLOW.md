# Luồng Chính Của Dự Án Home Express (v2.0)

**Ngày cập nhật:** 23/10/2025
**Phiên bản:** 1.6

> Tài liệu này mô tả luồng nghiệp vụ chuẩn đang/được triển khai trong repo `home-express-project`. Mỗi giai đoạn đều chỉ rõ: **mục tiêu**, **thực trạng code**, **khoảng trống & hành động**, **endpoint liên quan**. Nội dung dùng tiếng Việt có dấu để dễ chia sẻ cho đội sản phẩm/kỹ thuật.

---

## Mục Lục

1. [Tổng Quan Hệ Thống](#1-tổng-quan-hệ-thống)
2. [Các Vai Trò Chính](#2-các-vai-trò-chính)
3. [Luồng Nghiệp Vụ Chính](#3-luồng-nghiệp-vụ-chính)
   1. [Giai đoạn 1: Đăng ký & xác minh](#31-giai-đoạn-1-đăng-ký--xác-minh)
   2. [Giai đoạn 2: Cấu hình Transport](#32-giai-đoạn-2-cấu-hình-transport)
   3. [Giai đoạn 3: Tạo booking (Customer)](#33-giai-đoạn-3-tạo-booking-customer)
   4. [Giai đoạn 4: Báo giá (Transport)](#34-giai-đoạn-4-báo-giá-transport)
   5. [Giai đoạn 5: Đàm phán & chấp nhận](#35-giai-đoạn-5-đàm-phán--chấp-nhận)
   6. [Giai đoạn 6: Thanh toán đặt cọc (Escrow)](#36-giai-đoạn-6-thanh-toán-đặt-cọc-escrow)
   7. [Giai đoạn 7: Thực hiện công việc](#37-giai-đoạn-7-thực-hiện-công-việc)
   8. [Giai đoạn 8: Hoàn tất & quyết toán](#38-giai-đoạn-8-hoàn-tất--quyết-toán)
4. [Trình Tự Trạng Thái](#4-trình-tự-trạng-thái)
5. [Mô Hình Định Giá Động](#5-mô-hình-định-giá-động)
6. [Tích Hợp Thanh Toán & Ví Nội Bộ](#6-tích-hợp-thanh-toán--ví-nội-bộ)
7. [Phụ Lục: Mapping Module ↔ Package](#7-phụ-lục-mapping-module--package)

---

## 1. Tổng Quan Hệ Thống

### 1.1 Ngăn xếp kỹ thuật

| Tầng          | Công nghệ                                                                                  | Thư mục/liên quan                                          |
| ------------- | ------------------------------------------------------------------------------------------ | ---------------------------------------------------------- |
| Frontend      | Next.js 15.5.4, React 19, TypeScript 5, Tailwind CSS 4, Radix UI                           | `frontend/home-express/`                                   |
| Backend       | Spring Boot 3.5.5 (Java 17) với Web, Security, Data JPA, Validation, Redis, Mail, Actuator | `backend/home-express-api/`                                |
| Dữ liệu       | MySQL 8.0, Redis 7, Flyway, Docker Compose                                                 | `.env`, `docker-compose.yml`, `db/migration`               |
| AI & tích hợp | OpenAI (GPT-5 mini Vision & text), Google Maps Distance Matrix                            | service trong `backend/home-express-api/src/main/java/...` |

### 1.2 Các module chính

| Module                 | Chức năng                                                                        | Gói mã                                                                                                                                                                               |
| ---------------------- | -------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Auth & OTP             | Đăng ký, đăng nhập, OTP, session                                                 | `controller/AuthController`, `service/AuthService`, `service/OtpService`                                                                                                             |
| KYC/KYB                | Quản lý hồ sơ Transport, duyệt hồ sơ                                             | `controller/AdminTransportController`, `service/TransportService`, `entity/VerificationStatus`                                                                                       |
| Booking & Quotation    | Tạo booking, quản lý báo giá, lịch sử trạng thái                                 | `controller/BookingController`, `controller/QuotationController`, `service/BookingService`, `service/QuotationService`                                                               |
| Pricing & Estimation   | Bảng giá, auto estimation, **Rate Card Snapshot**                                | `controller/CategoryPricingController`, `controller/VehiclePricingController`, `controller/EstimationController`, `service/EstimationService`, **`service/RateCardSnapshotService`** |
| Customer Payment       | Khởi tạo giao dịch, đặt cọc (CASH/BANK_TRANSFER)                               | `controller/customer/CustomerPaymentController`, `service/PaymentService`                                                                                                            |
| Transport Portal       | Booking khả dụng, job, tài chính, settlement, payout, **báo cáo & xuất dữ liệu** | `controller/transport/*`, `service/TransportJobService`, `TransportFinanceService`, `PayoutService`, **`ExportService`**                                                             |
| Escrow & Settlement    | Theo dõi thu tiền, tính net-to-transport, xử lý payout                           | `repository/BookingSettlementRepository`, `controller/transport/TransportSettlementController`, `controller/admin/AdminSettlementController`                                         |
| **Wallet System**      | **Ví nội bộ Transport với Double-entry Bookkeeping**                             | **`entity/TransportWallet`, `entity/TransportWalletTransaction`, `service/WalletService`**                                                                                           |
| **Reporting & Export** | **Tạo báo cáo, xuất dữ liệu tài chính/vận hành cho TP**                          | **`service/ExportService`, `controller/transport/ReportController`**                                                                                                                 |
| Admin Portal           | Dashboard, người dùng, payout, moderation, intake session                        | `controller/admin/*`                                                                                                                                                                 |

### 1.3 Kiến trúc hệ thống

#### 1.3.1 Sơ đồ tổng quan

```
            +----------------------------------------------+
            |                 Ứng dụng khách               |
            |  - Customer Portal (Next.js)                 |
            |  - Transport Portal (Next.js)                |
            |  - Admin Portal (Next.js)                    |
            +------------------------+---------------------+
                                     |
                               HTTPS (JWT, SSE)
                                     |
+-----------------------------------------------------------------------+
|              Spring Boot Monolith (home-express-api)                  |
|  Security Filter → Controller → Service → Repository → Entity         |
|  Cross-cutting: logging, auditing, validation, notification           |
+----------------------+------------------------------+----------------+
                       |                              |
             Primary Data Stores             External Integrations
             --------------------            ----------------------
             - MySQL 8 (RDBMS)               - Ngân hàng (tài khoản chuyển khoản do backend cấu hình)
             - Redis 7 (cache/OTP/AI)        - OpenAI Vision (GPT-5 mini) - OCR & nhận diện vật dụng
             - Flyway migrations             - Google Maps Distance
```

#### 1.3.2 Các lớp backend

- **Security & middleware**: cấu hình Spring Security, JWT filter, rate limit Redis, logging IP/thiết bị (`config/*`).
- **Controller layer**: REST endpoint chia theo miền (common/customer/transport/admin), chỉ xử lý validate và gọi service.
- **Service layer**: chứa toàn bộ business logic (BookingService, EstimationService, PaymentService, TransportFinanceService, PayoutService, CommissionService, NotificationService, ...).
- **Repository layer**: Entity JPA + Repository interface, quản lý transaction.
- **Client tích hợp**: các service gọi ngân hàng (thông tin tài khoản chuyển khoản), OpenAI (GPT Vision), Distance API thông qua config `.env`.

#### 1.3.3 Dữ liệu & đồng bộ

- **MySQL**: lưu toàn bộ booking, quotation, thanh toán, settlement, payout, audit. Flyway đảm bảo version schema.
- **Redis**: OTP, session, cache estimation, budget AI, throttle API.
- **Migration**: file `V20YYMMDD__*.sql`. Ví dụ `V20251102__create_notifications_table.sql`.
- **Sao lưu**: ⚠️ **KHẨN CẤP - Ưu tiên 1** - Thiết lập Automated Backups
  - **Lý do**: Hệ thống quản lý dòng tiền thực tế - Mất mát dữ liệu tài chính là rủi ro không thể chấp nhận
  - **Yêu cầu MySQL**:
    - Automated daily snapshots với retention 30 ngày
    - Point-in-time recovery capability
    - Backup encryption và off-site storage
  - **Yêu cầu Redis**:
    - RDB snapshots mỗi 6 giờ
    - AOF (Append-Only File) cho critical data
- **Log/giám sát**: Spring Actuator (health/metrics), log file `./logs/application.log`, kế hoạch tích hợp Prometheus.

#### 1.3.4 Tích hợp ngoài

| Đối tác                  | Luồng                                             | Lớp mã                                                                                           |
| ------------------------ | ------------------------------------------------- | ------------------------------------------------------------------------------------------------ |
| Ngân hàng (chuyển khoản) | Cung cấp thông tin tài khoản escrow              | `service/PaymentService#getBankInfo`                                                             |
| OpenAI (GPT Vision)      | OCR giấy tờ, nhận diện vật dụng, hỗ trợ ước lượng | `service/ai/GptService`, `service/ai/AIDetectionOrchestrator`, `service/ai/BudgetLimitService`   |
| Google Maps Distance     | Ước lượng quãng đường                             | `service/DistanceService`                                                                        |

#### 1.3.5 Triển khai & vận hành

- Dev/local: Docker Compose chạy MySQL + Redis + phpMyAdmin; backend chạy `mvn spring-boot:run`, frontend `npm run dev`.
- Cấu hình: `.env` → Spring Dotenv → `Environment`.
- Build: Maven package jar, Next.js build.
- Bảo mật: HTTPS tại reverse proxy, JWT + refresh token, OTP, audit log, cấu hình CORS (`APP_CORS_ALLOWED_ORIGINS`).
- Mở rộng: Kiến trúc monolith có thể tách Payment/Settlement thành microservice nếu tải lớn; Redis cache hỗ trợ scaling.

---

## 2. Các Vai Trò Chính

| Vai trò                                   | Mô tả                      | Quyền hạn                                                                   |
| ----------------------------------------- | -------------------------- | --------------------------------------------------------------------------- |
| Guest                                     | Người dùng chưa đăng nhập  | Đăng ký, yêu cầu OTP, xem landing                                           |
| Customer (`UserRole.CUSTOMER`)            | Người đặt dịch vụ          | Tạo booking, xem/bình chọn báo giá, thanh toán, đánh giá                    |
| Transport Provider (`UserRole.TRANSPORT`) | Đối tác vận tải đã KYC/KYB | Nộp hồ sơ, cấu hình Rate Card, gửi báo giá, cập nhật job, nhận tiền         |
| Manager/Admin (`UserRole.MANAGER`)        | Vận hành nền tảng          | Duyệt hồ sơ, quản lý người dùng/booking/settlement/payout, xử lý tranh chấp |

---

## 3. Luồng Nghiệp Vụ Chính

### 3.1 Giai đoạn 1: Đăng ký & xác minh

**Mục tiêu**
- 100% transport phải hoàn thành KYC/KYB và được duyệt trước khi truy cập dashboard.
- Lưu vết duyệt và hỗ trợ mở lại hồ sơ khi tài liệu hết hạn.

**Thực trạng code**
- `AuthController` xử lý `register/login/verify-otp/resend-otp`.
- OTP lưu Redis qua `OtpService`.
- Admin duyệt hồ sơ tại `AdminTransportController` với enum `VerificationStatus = PENDING/APPROVED/REJECTED`.
- Hiện chưa chặn transport chưa duyệt truy cập `/api/v1/transport/**`.

**Khoảng trống & hành động**
1. Thêm bảng/field audit (người duyệt, thời điểm, lý do).
2. Interceptor hoặc `@PreAuthorize` chặn transport chưa APPROVED.
3. Endpoint `GET /api/v1/transport/profile/status` để FE hiển thị checklist.
4. Bổ sung trạng thái `NEEDS_UPDATE`, `SUSPENDED`.

**Endpoint liên quan**

| Endpoint                                     | Controller               | Ghi chú         |
| -------------------------------------------- | ------------------------ | --------------- |
| `POST /api/v1/auth/register`                 | AuthController           | Đăng ký + OTP   |
| `POST /api/v1/auth/verify-otp`               | AuthController           | Xác thực OTP    |
| `GET /api/v1/admin/transports`               | AdminTransportController | Danh sách hồ sơ |
| `PATCH /api/v1/admin/transports/{id}/verify` | AdminTransportController | Duyệt/Từ chối   |

---

### 3.2 Giai đoạn 2: Cấu hình Transport

**Mục tiêu**
- Transport tự quản lý Rate Card (giá cơ bản, bậc quãng đường, giá vật phẩm, phụ phí).
- Chỉ transport trạng thái `READY_TO_QUOTE` mới được xem booking cần báo giá.

**Thực trạng code**
- Pricing hiện chỉ do Admin nhập (`CategoryPricingController`, `VehiclePricingController`).
- `EstimationService` đọc pricing này để đưa gợi ý.
- Transport dashboard chưa có module Rate Card riêng.

**Khoảng trống & hành động**
1. Tạo bảng/endpoint `POST|PUT /api/v1/transport/rate-card` + entity riêng.
2. Đồng bộ `TransportJobService#getAvailableBookings` để lọc theo `readyToQuote`.
3. API `GET /api/v1/transport/rate-card/snapshot` phục vụ Dynamic Pricing.
4. Nhắc nhở (notification/email) khi Rate Card sắp hết hiệu lực.

---

### 3.3 Giai đoạn 3: Tạo booking (Customer)

**Mục tiêu**
- Flow 4 bước có hiển thị rõ **khoảng giá ước tính** và nguồn dữ liệu (AI/OCR hay nhập tay).
- Lưu lại toàn bộ metadata phục vụ pricing engine và báo cáo.

**Thực trạng code**
- `BookingController#POST /api/v1/bookings` trả `BookingResponse` chứa `estimatedPrice` (một giá trị).
- `EstimationController#POST /api/v1/estimation/auto` đã trả `priceRange`, `breakdown`.
- Enum `BookingStatus` hiện có: `PENDING/QUOTED/CONFIRMED/IN_PROGRESS/COMPLETED/CANCELLED`.

**Khoảng trống & hành động**
1. Gọi `EstimationService` trong bước review → lưu `estimated_price_min/max`.
2. Cập nhật DTO + DB: thêm `estimatedPriceRange`, `estimationSource`.
3. Ghi log nguồn dữ liệu (AI, thủ công) lẫn file đính kèm.
4. Đồng bộ FE hiển thị min-max thay vì single price.

**Endpoint chính**

| Endpoint                       | Vai trò              |
| ------------------------------ | -------------------- |
| `POST /api/v1/bookings`        | Customer tạo booking |
| `POST /api/v1/estimation/auto` | Tính khoảng giá      |

---

### 3.4 Giai đoạn 4: Báo giá (Transport)

**Mục tiêu**
- Khi Transport xem booking chi tiết, hệ thống áp dụng Rate Card để sinh `suggestedPrice`.
- Transport có thể chỉnh sửa và gửi `quotedPrice`, kèm breakdown, thời hạn hiệu lực.

**Thực trạng code**
- `TransportJobController` trả danh sách booking khả dụng.
- `QuotationController#POST /api/v1/quotations` mới chỉ nhận `price`, `notes`.
- `QuotationStatus` chỉ có `PENDING/ACCEPTED/REJECTED/EXPIRED`.

**Khoảng trống & hành động**
1. Tạo `DynamicPricingService` áp dụng Rate Card từng transport.
2. Mở rộng DTO request/response để chứa `suggestedPrice`, `quotedPrice`, `priceBreakdown`, `validityPeriod`.
3. Bổ sung trạng thái `NEGOTIATING`, `COUNTERED`, `WITHDRAWN`.
4. Lưu lịch sử giá để phân tích chênh lệch.
5. ⚠️ **QUAN TRỌNG - Rate Card Snapshot**: Khi một Báo giá được tạo, hệ thống phải lưu lại một bản sao (Snapshot) của Rate Card được sử dụng tại thời điểm đó.
   - **Lý do**: Transport có thể thay đổi giá trong tương lai, nhưng báo giá đã gửi phải tôn trọng các quy tắc tại thời điểm tạo
   - **Bằng chứng**: Snapshot này là bằng chứng không thể thiếu khi kiểm toán hoặc giải quyết tranh chấp
   - **Triển khai**:
     - Tạo bảng `quotation_rate_card_snapshot` với các cột: `quotation_id`, `rate_card_json`, `created_at`
     - Tạo `RateCardSnapshotService` để serialize và lưu Rate Card
     - Cập nhật `QuotationService.createQuotation()` để gọi snapshot service

---

### 3.5 Giai đoạn 5: Đàm phán & chấp nhận

**Mục tiêu**
- Customer có thể so sánh đa tiêu chí (giá, rating, dịch vụ kèm, SLA) và gửi counter-offer.
- Chấp nhận quotation sẽ tạo hợp đồng điện tử và khóa giá.

**Thực trạng code**
- `BookingController#getBookingQuotations` trả danh sách báo giá.
- `QuotationController#POST /{id}/accept` set `finalPrice` và `BookingStatus.CONFIRMED`.
- Chưa có API đàm phán.

**Khoảng trống & hành động**
1. Endpoint `POST /api/v1/quotations/{id}/negotiate` (giá mong muốn + ghi chú).
2. Bổ sung bảng log đàm phán (bên gửi, giá đề xuất, thời gian).
3. Sinh hợp đồng (`ContractController`) khi accept, lưu URL/file.
4. UI hiển thị filter/sort theo các tiêu chí.

---

### 3.6 Giai đoạn 6: Thanh toán đặt cọc (Escrow)

**Mục tiêu**
- Thu 30% giá trị booking qua **CASH** hoặc **BANK_TRANSFER**, tạo bản ghi `Payment` trong DB.
- Chuẩn bị cho việc triển khai dịch vụ (Transport thấy booking đã đặt cọc).

**Thực trạng code**
- `CustomerPaymentController` hiện có các endpoint PRODUCTION:
  - `POST /api/v1/customer/payments/deposit/initiate` – khởi tạo thanh toán đặt cọc 30%.
  - `POST /api/v1/customer/payments/remaining/initiate` – (dùng cho Giai đoạn 8, phần 70% + tip).
  - `GET  /api/v1/customer/payments/status` – FE poll trạng thái thanh toán cho từng booking.
  - `GET  /api/v1/customer/payments/bank-info` – lấy thông tin tài khoản ngân hàng từ cấu hình backend.
- `PaymentService` chỉ hỗ trợ **CASH** và **BANK_TRANSFER** (không còn tích hợp VNPay/MoMo/VietQR, không có callback controller).

**Luồng giao diện & điều hướng thực tế**
1. Customer tạo booking tại `/customer/bookings/create`.
2. Customer xem/chọn báo giá:
   - `/customer/bids?bookingId=...` (màn hình chọn bid realtime), **hoặc**
   - `/customer/bookings/[id]/quotations` (danh sách báo giá cho booking).
3. Khi chấp nhận một báo giá, hệ thống tạo hợp đồng tự động.
4. Thanh toán đặt cọc (30%) → `/customer/checkout?bookingId=...`.
5. Tại `/customer/checkout`, customer chọn phương thức (CASH/BANK_TRANSFER) và gọi API khởi tạo thanh toán đặt cọc.
6. Backend tạo bản ghi `Payment` (loại DEPOSIT) trong MySQL và trả kết quả về cho FE.
7. Sau khi dịch vụ hoàn tất, booking được cập nhật trạng thái `COMPLETED` và customer thanh toán phần còn lại (70% + tip) tại `/customer/bookings/[id]/complete`.

---

### 3.7 Giai đoạn 7: Thực hiện công việc

**Mục tiêu**
- Theo dõi trạng thái thi công chi tiết, minh bạch cho khách và admin.
- Ghi nhận bằng chứng (ảnh, chữ ký, biên bản) và phát broadcasting realtime.

**Thực trạng code**
- `TransportJobController` mới liệt kê job, chưa có API cập nhật trạng thái.
- `TransportEventController` SSE chưa đẩy dữ liệu.
- `BookingStatus` không có các bước trung gian (đang chỉ `IN_PROGRESS`).
- Chưa có logging hành trình.

**Khoảng trống & hành động**
1. Endpoint `PUT /api/v1/transport/jobs/{bookingId}/status` với payload `status`, `note`, `evidenceUrls`.
2. Mở rộng enum `BookingStatus`: `DRIVER_ON_THE_WAY`, `LOADING`, `IN_TRANSIT`, `UNLOADING`, `COMPLETED`.
3. Push SSE/WebSocket cho customer/admin mỗi khi trạng thái đổi.
4. Lưu lịch sử vào `booking_status_history`, gắn userId/role.
5. Báo cáo cho Admin Dashboard (ETA, SLA).

---

### 3.8 Giai đoạn 8: Hoàn tất & quyết toán

**Mục tiêu**
- Customer xác nhận hoàn tất, thanh toán 70% + tips, hệ thống tự động tính commission, ghi có ví transport, hỗ trợ payout tự động.
- Có cơ chế dispute đóng băng tiền nếu phát sinh khiếu nại.

**Thực trạng code**
- `TransportSettlementController` + `BookingSettlementRepository` theo dõi settlement.
- `TransportPayoutController` và `AdminPayoutController` tạo payout batch (thủ công).
- Đã có endpoint riêng cho phần 70% còn lại (`POST /api/v1/customer/payments/remaining/initiate`), chưa có ví nội bộ, tips chưa track chi tiết.

**Khoảng trống & hành động**
1. Hoàn thiện xử lý `POST /api/v1/customer/payments/remaining/initiate` (bookingId, amount, optional tip) trong Settlement & báo cáo.
2. Cập nhật `BookingSettlement.totalCollectedVnd`, `commissionRateBps`, `netToTransportVnd`.
3. Khi customer confirm → `BookingStatus.CONFIRMED_BY_CUSTOMER` và settlement `READY`.
4. ⚠️ **QUAN TRỌNG - Thiết kế Ví với Double-entry Bookkeeping**: Thiết kế `transport_wallet` + `transport_wallet_transaction` tuân thủ nguyên tắc kế toán
   - **Nguyên tắc Bất biến (Immutable)**: Mọi giao dịch phải Bất biến. Không bao giờ cập nhật một giao dịch đã xảy ra
   - **Điều chỉnh**: Nếu cần điều chỉnh, tạo một giao dịch đảo ngược mới (reversal transaction)
   - **Cấu trúc bảng `transport_wallet_transaction`**:
   - `transaction_id` (PK, UUID)
   - `wallet_id` (FK to transport_wallet)
   - `transaction_type` (ENUM: SETTLEMENT_CREDIT, PAYOUT_DEBIT, ADJUSTMENT_CREDIT, ADJUSTMENT_DEBIT, REVERSAL)
   - `amount` (DECIMAL, always positive)
   - `running_balance` (DECIMAL, calculated balance after this transaction)
     - `reference_type` (ENUM: BOOKING, PAYOUT, DISPUTE, MANUAL)
     - `reference_id` (ID của booking/payout/dispute)
     - `description` (TEXT)
     - `created_at` (TIMESTAMP, immutable)
     - `created_by` (User ID who initiated)
   - **Cấu trúc bảng `transport_wallet`**:
   - `wallet_id` (PK, UUID)
   - `transport_id` (FK, UNIQUE)
    - `current_balance` (DECIMAL, denormalized for quick access)

> **LƯU Ý VỀ IMPLEMENTATION (đã được audit – không phải gap nghiệp vụ):**  
> - Schema thực tế dùng bảng **`transport_wallets`** và **`transport_wallet_transactions`** (plural) để thống nhất convention đặt tên.  
> - Tất cả các cột tiền tệ lưu bằng **`BIGINT ..._vnd` (integer VND)** thay vì DECIMAL. Đây là convention chung của toàn hệ thống (payment, settlement, payout).  
> - Khóa chính đang dùng **`BIGINT AUTO_INCREMENT`** giống bookings/payments; docs mô tả UUID mang tính high-level, không ảnh hưởng nghiệp vụ.  
> - `WalletTransactionReferenceType` trong code còn mở rộng thêm `SETTLEMENT`, `ADJUSTMENT` (so với danh sách tối thiểu trong docs) để theo dõi ledger chi tiết hơn.  
> → Những điểm trên chỉ là **khác biệt tài liệu** cần ghi chú, không phải thiếu sót hay sai lệch về logic double-entry.
     - `total_earned` (DECIMAL, lifetime earnings)
     - `total_withdrawn` (DECIMAL, lifetime withdrawals)
     - `status` (ENUM: ACTIVE, FROZEN, SUSPENDED)
     - `last_transaction_at` (TIMESTAMP)
   - **WalletService methods**:
     - `creditWallet(walletId, amount, referenceType, referenceId, description)` - Add money
     - `debitWallet(walletId, amount, referenceType, referenceId, description)` - Remove money
     - `freezeWallet(walletId, reason)` - Freeze for disputes
     - `unfreezeWallet(walletId)` - Unfreeze after resolution
     - `getTransactionHistory(walletId, dateRange, transactionType)` - Query history
     - `calculateBalance(walletId)` - Verify balance integrity
5. `PayoutService` gọi API chi hộ ngân hàng, cập nhật `PayoutStatus` (`CREATED → PROCESSING → PAID/FAILED`).
6. Tranh chấp: đặt settlement `ON_HOLD/DISPUTED`, đóng băng ví cho tới khi `RESOLVED`.
7. ⚠️ **MỚI - Báo cáo & Xuất dữ liệu**: Triển khai `ExportService` cho phép Transport xuất:
   - **Sao kê Giao dịch Ví** (Wallet Transaction History): Transaction ID, Ngày giờ, Loại, Số tiền, Số dư cuối kỳ, Mô tả
   - **Chi tiết Quyết toán** (Settlement Details): Booking ID, Ngày hoàn thành, Tổng giá trị, Tips, Phí nền tảng, Thu nhập ròng
   - **Lịch sử Rút tiền** (Payout History): Payout ID, Ngày yêu cầu, Số tiền, Tài khoản ngân hàng, Trạng thái
   - **Lịch sử Công việc** (Job History): Booking ID, Ngày giờ, Điểm đón/Giao, Khoảng cách, Loại xe, Đánh giá
   - **Định dạng**: CSV (bắt buộc cho kế toán) và XLSX (Excel)
   - **Xử lý Bất đồng bộ**: Sử dụng Background Job/Queue cho báo cáo lớn, gửi thông báo khi file sẵn sàng
   - **Triển khai**:
     - Tạo `ExportService` với methods: `exportWalletTransactions()`, `exportSettlements()`, `exportPayouts()`, `exportJobHistory()`
     - Tạo `ReportController` với endpoints: `POST /api/v1/transport/reports/wallet`, `POST /api/v1/transport/reports/settlements`, etc.
     - Sử dụng Apache POI (Excel) hoặc OpenCSV (CSV) để generate files
     - Lưu trữ tạm thời trên Cloud Storage với Signed URL
     - Gửi email/notification khi file sẵn sàng

---

## 4. Trình Tự Trạng Thái

### 4.1 Booking lifecycle đề xuất

```
PENDING
  → QUOTED
  → NEGOTIATING
  → CONFIRMED
  → DEPOSIT_PAID
  → DRIVER_ON_THE_WAY
  → LOADING
  → IN_TRANSIT
  → UNLOADING
  → COMPLETED
  → CONFIRMED_BY_CUSTOMER
  → SETTLED
```

Trạng thái đặc biệt: `CANCELLED`, `DISPUTED`, `RESOLVED`, `EXPIRED`.

### 4.2 Quotation lifecycle

```
PENDING → NEGOTIATING ↔ COUNTERED → ACCEPTED
PENDING → REJECTED
PENDING → EXPIRED
ACCEPTED → WITHDRAWN (nếu hợp đồng bị hủy)
```

### 4.3 Transport account

`REGISTERED → PENDING_VERIFICATION → APPROVED/REJECTED → READY_TO_QUOTE → (có thể SUSPENDED/NEEDS_UPDATE)`

### 4.4 Settlement & payout

```
PENDING → READY → ON_HOLD/DISPUTED → READY → PAID
```

---

## 5. Mô Hình Định Giá Động

1. **Rate Card tự quản lý**
   - Gồm giá cơ bản, bậc quãng đường, giá vật phẩm (chuẩn hóa theo Master Item List), phụ phí (leo tầng, tháo lắp, giờ cao điểm).
   - Snapshot khi tạo báo giá để tránh biến động.

2. **Dynamic Pricing Engine**
   - Input: chi tiết booking + Rate Card + thời điểm (peak), KPI transport.
   - Output: `suggestedPrice`, `priceBreakdown`, `confidenceScore`.
   - Triển khai tại service riêng, được gọi khi transport mở booking.

3. **Automated RFQ**
   - Suggested price hiển thị cho transport, họ điều chỉnh thành quoted price.
   - Lưu chênh lệch (%), lý do tăng/giảm để tối ưu engine.

4. **Feedback thị trường**
   - Customer nhận nhiều báo giá và chọn dựa trên cân bằng giá – chất lượng – dịch vụ.
   - Thu thập dữ liệu `won/lost` để cải thiện recommendation/score.

---

## 6. Tích Hợp Thanh Toán & Ví Nội Bộ

| Bước                                | Công việc                                                                              | Thành phần                                                                                               |
| ----------------------------------- | -------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------- |
| Đặt cọc 30%                         | `POST /api/v1/customer/payments/deposit/initiate`                                       | `CustomerPaymentController`, `PaymentService`                                                            |
| Giữ tiền Escrow                     | Tạo `BookingSettlement` sau khi nhận cọc (Payment loại DEPOSIT)                         | `BookingSettlementRepository`, `BookingService`                                                          |
| Thu 70% + tips                      | `POST /api/v1/customer/payments/remaining/initiate`                                    | `CustomerPaymentController`, `PaymentService`                                                            |
| Tính phí nền tảng                   | `CommissionService` (tính `platformFee`, `netToTransport`)                             | `CommissionService`, `SettlementDTO`                                                                     |
| **Ghi ví transport (Double-entry)** | Tạo bút toán `transport_wallet_transaction` (SETTLEMENT_CREDIT) với `running_balance`  | **`WalletService.creditWallet()`**                                                                       |
| Payout                              | Admin tạo batch hoặc TP yêu cầu, hệ thống gọi API ngân hàng, tạo bút toán PAYOUT_DEBIT | `AdminPayoutController`, `TransportPayoutController`, `PayoutService`, **`WalletService.debitWallet()`** |
| Dispute                             | Đặt settlement `ON_HOLD/DISPUTED`, khóa ví với `WalletService.freezeWallet()`          | `TransportSettlementController`, `AdminSettlementController`, **`WalletService`**                        |
| **Đối soát & Báo cáo (MỚI)**        | Cho phép TP xuất dữ liệu tài chính để kiểm toán và đối soát                            | **`TransportFinanceController`, `ExportService`**                                                        |
> **TransportFinanceController endpoints (ledger-backed)**  
> - `GET /api/v1/transport/earnings/stats`: Tổng hợp booking + ví dựa trên dữ liệu thực tế trong MySQL.  
> - `GET /api/v1/transport/earnings/wallet-report`: Báo cáo cashflow in/out, số dư theo ngày, đối chiếu settlement/payout với `transport_wallet_transactions`.  
> - `GET /api/v1/transport/transactions`: Sao kê ví (wallet ledger) cho dashboard Transport.
**Gateway fee (bank transfer)**
- Escrow chi ho tro chuyen khoan thu cong nen `gateway_fee_vnd = 0`.
- Neu co phi ngan hang, cap nhat constant `CommissionService.BANK_TRANSFER_FIXED_FEE_VND`.

**Mapping `collection_mode`**

| Deposit (PaymentType.DEPOSIT) | Remaining (PaymentType.REMAINING_PAYMENT) | `collection_mode`      | Ghi chu |
| ----------------------------- | ----------------------------------------- | ---------------------- | ------- |
| BANK_TRANSFER                 | BANK_TRANSFER                             | `ALL_ONLINE`           | Thu 30/70 bang chuyen khoan |
| BANK_TRANSFER                 | CASH                                      | `CASH_ON_DELIVERY`     | Dat coc online, thu phan 70% tien mat khi giao |
| CASH                          | BANK_TRANSFER                             | `PARTIAL_ONLINE`       | Dat coc tien mat, phan con lai online |
| CASH                          | CASH                                      | `ALL_CASH`             | Toan bo thu offline |
| Mix CASH + BANK_TRANSFER trong cung stage |                                         | `MIXED`                 | Khi deposit hoac remaining dung ca 2 phuong thuc |

- `PaymentService` tu dong tao/cap nhat `BookingSettlement` khi deposit hoac remaining payment duoc confirm => escrow tien den `READY` tu dong.

---

## 7. Phụ Lục: Mapping Module ↔ Package

| Nghiệp vụ                   | Package/Controller/Service                                                                                                                                                                                           |
| --------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Đăng ký, OTP, session       | `controller/AuthController`, `service/AuthService`, `service/OtpService`, `service/UserSessionService`                                                                                                               |
| Quản lý người dùng/customer | `controller/UserController`, `controller/customer/*`, `service/UserService`                                                                                                                                          |
| Booking                     | `controller/BookingController`, `service/BookingService`, `entity/Booking`, `repository/BookingRepository`                                                                                                           |
| Quotation & đàm phán        | `controller/QuotationController`, `service/QuotationService`, `entity/Quotation`, `entity/QuotationStatus`                                                                                                           |
| Pricing & estimation        | `controller/CategoryPricingController`, `controller/VehiclePricingController`, `controller/EstimationController`, `service/EstimationService`                                                                        |
| Thanh toán & escrow         | `controller/customer/CustomerPaymentController`, `service/PaymentService`, `repository/BookingSettlementRepository`                                                                  |
| Transport portal            | `controller/transport/TransportJobController`, `TransportFinanceController`, `TransportSettlementController`, `TransportPayoutController`, `service/TransportJobService`, `TransportFinanceService`, `PayoutService` |
| Admin portal                | `controller/admin/*`, `service/Admin*Service`                                                                                                                                                                        |
| Notification & monitoring   | `controller/NotificationController`, `controller/AdminDashboardController`, `service/NotificationService`                                                                                                            |


## 8. Thứ Tự Ưu Tiên Triển Khai

Để đạt hiệu quả cao nhất và giảm thiểu rủi ro sớm nhất, nên ưu tiên triển khai theo các nhóm sau:

### **Ưu tiên 1: Kiểm soát An ninh và Nền tảng Tài chính** ⚠️ **KHẨN CẤP**

**Mục tiêu:** Đảm bảo nền tảng an toàn và kiểm soát được dòng tiền

1. **Kiểm soát Truy cập (3.1 - Hành động #2)**: Triển khai ngay Interceptor để chặn Transport chưa được duyệt
2. **Bảo mật Nền tảng**: Đảm bảo các lỗ hổng nghiêm trọng từ báo cáo kiểm toán (Chính sách mật khẩu, Mã hóa dữ liệu thanh toán) được xử lý song song
3. **Cơ chế Escrow (3.6 - Hành động #2)**: Đảm bảo việc tạo và cập nhật BookingSettlement hoạt động chính xác khi nhận tiền cọc
4. **Hệ thống Ví nội bộ (3.8 - Hành động #4)**: Thiết kế và triển khai `transport_wallet` với các nguyên tắc toàn vẹn tài chính (Double-entry Bookkeeping)
5. **Sao lưu Dữ liệu (1.3.3)**: Triển khai Automated Backups cho MySQL và Redis

### **Ưu tiên 2: Triển khai Mô hình Định giá Động**

**Mục tiêu:** Cho phép thị trường hoạt động theo đúng thiết kế (TP tự định giá)

1. **Quản lý Rate Card (3.2)**: Xây dựng API và giao diện để Transport cấu hình bảng giá
2. **Dynamic Pricing Engine & Automated RFQ (3.4)**:
   - Phát triển `DynamicPricingService`
   - Cập nhật luồng báo giá
   - Triển khai **Rate Card Snapshot** (bảo toàn bằng chứng định giá)

### **Ưu tiên 3: Hiệu quả Vận hành và Minh bạch**

**Mục tiêu:** Tăng hiệu quả vận hành và xây dựng niềm tin với đối tác

1. **Theo dõi Chi tiết (3.7)**: Triển khai các trạng thái công việc chi tiết và thu thập bằng chứng
2. **Tự động hóa Chi trả (3.8 - Hành động #5)**: Tích hợp API chi hộ ngân hàng để tự động hóa Payout
3. **Xử lý Tranh chấp (3.8 - Hành động #6)**: Định nghĩa và triển khai quy trình Dispute và đóng băng ví
4. **Báo cáo & Xuất dữ liệu (3.8 - Hành động #7)**: Triển khai `ExportService` cho phép Transport xuất:
   - Sao kê Giao dịch Ví (Wallet Transaction History)
   - Chi tiết Quyết toán (Settlement Details Report)
   - Lịch sử Rút tiền (Payout History)
   - Lịch sử Công việc (Job History)

### **Ưu tiên 4: Cải thiện Trải nghiệm Người dùng (UX)**

**Mục tiêu:** Nâng cao trải nghiệm và tăng tỷ lệ chuyển đổi

1. **Đàm phán (3.5)**: Triển khai tính năng đàm phán giá
2. **Tiền Tips (3.8)**: Tích hợp theo dõi và xử lý tiền Tips

---

## 9. Các Nguyên Tắc Quan Trọng

### **9.1 Toàn vẹn Tài chính (Financial Integrity)**

1. **Double-entry Bookkeeping**: Mọi giao dịch tài chính phải tuân thủ nguyên tắc kế toán
2. **Immutability**: Giao dịch đã xảy ra không được sửa đổi, chỉ được đảo ngược bằng giao dịch mới
3. **Audit Trail**: Mọi thay đổi phải được ghi lại với timestamp và user ID
4. **Balance Verification**: Định kỳ kiểm tra tính toàn vẹn của số dư ví

### **9.2 Bảo toàn Bằng chứng (Evidence Preservation)**

1. **Rate Card Snapshot**: Lưu lại bản sao Rate Card tại thời điểm tạo báo giá
2. **Transaction History**: Lưu trữ vĩnh viễn lịch sử giao dịch
3. **Status History**: Ghi lại mọi thay đổi trạng thái booking/quotation/payment
4. **Document Retention**: Lưu trữ hợp đồng, hóa đơn, chứng từ thanh toán

### **9.3 Minh bạch và Đối soát (Transparency & Reconciliation)**

1. **Export Capability**: Cho phép đối tác xuất dữ liệu tài chính định dạng CSV/XLSX
2. **Real-time Balance**: Hiển thị số dư ví và lịch sử giao dịch real-time
3. **Detailed Breakdown**: Cung cấp chi tiết phí, hoa hồng, thuế cho mọi giao dịch
4. **Reconciliation Reports**: Báo cáo đối soát định kỳ (ngày/tuần/tháng)

### **9.4 Bảo vệ Dữ liệu (Data Protection)**
1. **Automated Backups**: Sao lưu tự động hàng ngày với retention policy
2. **Point-in-time Recovery**: Khả năng khôi phục dữ liệu tại bất kỳ thời điểm nào
3. **Encryption**: Mã hóa dữ liệu nhạy cảm (thanh toán, tài khoản ngân hàng)
4. **Off-site Storage**: Lưu trữ backup tại vị trí địa lý khác

### 9.5 Không sử dụng Mock Data / Demo Mode

1. **Luồng dữ liệu thật end-to-end**: Mọi dữ liệu hiển thị trên UI phải đi qua chuỗi:
    - **Frontend (Next.js)** → **Next.js API (proxy)** → **Spring Boot Backend** → **MySQL**.
2. **Không hard-code / mock**: Không dùng JSON tĩnh, response giả, hay mảng dữ liệu in-memory thay cho truy vấn DB.
3. **Không "demo mode" cho thanh toán**: Khi gọi API thanh toán, luôn tạo/sử dụng bản ghi `Payment` thật trong DB (kể cả khi chỉ mô phỏng chuyển khoản ngân hàng).
4. **Checklist trước buổi bảo vệ**:
    - Tất cả form/flow đều lưu dữ liệu xuống MySQL.
    - Refresh trang vẫn thấy dữ liệu đã tạo.
    - Không còn route Next.js nào trả dữ liệu hard-code.

---

## 10. Reviews & Ratings (Đánh Giá & Xếp Hạng)

**Mục tiêu**
- Cho phép Customer và Transport đánh giá lẫn nhau sau khi booking hoàn tất.
- Hỗ trợ Transport phản hồi lại các đánh giá từ Customer.
- Quản lý báo cáo và kiểm duyệt đánh giá không phù hợp.

**Thực trạng code**
- **Frontend**: `/transport/reviews/page.tsx` (lines 64-492) hiển thị danh sách đánh giá với bộ lọc, thống kê, và tính năng phản hồi.
- **Backend Controller**: `ReviewController` xử lý:
  - `POST /api/v1/reviews` – Tạo đánh giá (Customer hoặc Transport)
  - `GET /api/v1/reviews` – Lấy danh sách đánh giá với filter (revieweeId, minRating, status)
  - `GET /api/v1/reviews/{id}` – Chi tiết một đánh giá
  - `POST /api/v1/reviews/{id}/respond` – Transport phản hồi đánh giá (lines 68-80)
  - `POST /api/v1/reviews/{id}/report` – Báo cáo đánh giá không phù hợp
  - `PATCH /api/v1/reviews/{id}/status` – Admin cập nhật trạng thái (kiểm duyệt)
- **Backend Service**: `ReviewService` chứa toàn bộ logic:
  - `createReview()` – Kiểm tra booking COMPLETED, phòng tránh đánh giá trùng, lưu vào DB
  - `getReviews()` – Truy vấn theo revieweeId, rating min, hoặc trạng thái
  - `respondToReview()` – Cho phép người được đánh giá (reviewee) phản hồi
  - `reportReview()` – Ghi nhận báo cáo, tự động flag review khi ≥ 3 báo cáo
  - `updateReviewStatus()` – Admin kiểm duyệt (PENDING → PUBLISHED/FLAGGED/REMOVED)
- **Entities**:
  - `Review` – Đánh giá chính (bookingId, reviewerId, revieweeId, ratings, comments)
  - `ReviewResponse` – Phản hồi từ reviewee
  - `ReviewReport` – Báo cáo từ người dùng
  - `ReviewStatus` (enum) – PENDING, PUBLISHED, FLAGGED, REMOVED
  - `ReviewerType` (enum) – CUSTOMER, TRANSPORT
  - `ReportStatus` (enum) – PENDING, RESOLVED, REJECTED

**Mapping API tới FE**
- Frontend hook `use-reviews.ts` cung cấp các function:
  - `fetchReviews(page, filters)` – Gọi `apiClient.getMyReviews()`
  - `submitReview(data)` – Gọi `apiClient.submitReview()`
  - `respondToReview(reviewId, response)` – Gọi `apiClient.respondToReview()`
  - `reportReview(reviewId, reason)` – Gọi `apiClient.reportReview()`
  - `canReviewBooking(bookingId)` – Kiểm tra điều kiện review
- FE page `/transport/reviews` hiển thị:
  - Thống kê: Tổng đánh giá, rating TB, % đã phản hồi, % xác minh
  - Phân bố sao (5/4/3/2/1)
  - Bộ lọc: tìm kiếm, rating, trạng thái phản hồi
  - Danh sách review với avatar, rating, nội dung, ảnh, phản hồi
  - Nút "Phản hồi" và form textarea để gửi phản hồi
  - Export CSV cho kế toán/báo cáo

**Khoảng trống & hành động**
1. **Kiểm tra Rating Column** – Hiện ReviewRequest/ReviewDto dùng `rating` nhưng ReviewService dùng `overallRating`, `punctualityRating`, `professionalismRating`, `communicationRating`, `careRating`. Cần thống nhất DTO và FE gửi đúng cấu trúc.
2. **Review Page Component** – Chưa có endpoint `getMyReviews` trong API controller; FE page gọi `apiClient.getMyReviews()` nhưng controller không có GET `/{revieweeId}` hoặc query param `my-reviews`.
   - **Hành động**: Thêm endpoint `GET /api/v1/reviews/my` hoặc `GET /api/v1/reviews?revieweeId=me` để trả về đánh giá về Transport hiện tại (authenticated).
3. **Review Stats** – FE yêu cầu `stats` object (total_reviews, average_rating, rating_distribution, verified_reviews, with_response) nhưng Service không có method `getReviewStats()`.
   - **Hành động**: Thêm endpoint `GET /api/v1/reviews/stats?revieweeId=...` hoặc tính stats bên FE từ danh sách.
4. **Photo URLs** – Review model chưa có field `photo_urls`; FE hiển thị ảnh nhưng BE không lưu.
   - **Hành động**: Thêm field `photoUrls` vào Review entity, handle upload file trong request.
5. **Verified Badge** – FE hiển thị `is_verified` nhưng ReviewService set `isVerified` từ đâu?
   - **Hành động**: Thêm logic verify review (ví dụ: kiểm tra booking đúng hoặc admin approve).
6. **Pagination & Sorting** – Page state `currentPage` nhưng API GET `/reviews` không rõ có support phân trang trong response hay không.
   - **Hành động**: Đảm bảo `getMyReviews()` trả về object chứa `data.reviews[]`, `data.stats`, `data.pagination { totalPages, currentPage, totalItems }`.

**Endpoint Mapping (Hiện tại)**

| Endpoint                         | Method | Controller          | Service Method        | Status      |
| -------------------------------- | ------ | ------------------- | --------------------- | ----------- |
| `/api/v1/reviews`                | POST   | ReviewController    | createReview()        | ✓ Implemented |
| `/api/v1/reviews`                | GET    | ReviewController    | getReviews()          | ⚠ Partial   |
| `/api/v1/reviews/{id}`           | GET    | ReviewController    | getReviewById()       | ✓ Implemented |
| `/api/v1/reviews/{id}/respond`   | POST   | ReviewController    | respondToReview()     | ✓ Implemented |
| `/api/v1/reviews/{id}/report`    | POST   | ReviewController    | reportReview()        | ✓ Implemented |
| `/api/v1/reviews/{id}/status`    | PATCH  | ReviewController    | updateReviewStatus()  | ✓ Implemented |
| `/api/v1/reviews/my` (MỚI)       | GET    | ReviewController    | getMyReviews()        | ❌ Missing   |
| `/api/v1/reviews/stats` (MỚI)    | GET    | ReviewController    | getReviewStats()      | ❌ Missing   |

**Hành động ưu tiên**
1. Thống nhất Review DTO (single `rating` vs. multi-criteria ratings)
2. Thêm endpoint `GET /api/v1/reviews/my` để FE fetch đánh giá về user hiện tại
3. Thêm endpoint `GET /api/v1/reviews/stats` để cung cấp thống kê (total, average, distribution)
4. Thêm hỗ trợ photo upload khi tạo review
5. Xác định logic verify review (tự động hay manual)

---

**Ghi chú triển khai:**
- Khi bổ sung enum hoặc endpoint mới, cần cập nhật tài liệu này để đảm bảo Product/Engineering cùng hiểu.
- Mỗi “khoảng trống & hành động” nên được tách thành ticket với mô tả API, migration, FE tương ứng.
- Không còn tích hợp MoMo/VNPay/VietQR trong luồng thanh toán; hiện tại nền tảng chỉ hỗ trợ **CASH** và **BANK_TRANSFER** với thông tin ngân hàng được cấu hình ở backend.
- ⚠️ **Ưu tiên 1 (An ninh & Tài chính) phải được triển khai trước khi deploy production** - Đây là yêu cầu bắt buộc, không thể thương lượng.

---

**Phiên bản:** 1.6 (Cập nhật với Rate Card Snapshot, Double-entry Bookkeeping, Reporting & Export)
**Ngày cập nhật:** 23/10/2025
**Người đóng góp:** TriQuan

### Phase 2 settlement & wallet updates
- Deposit confirm t?o settlement PENDING; remaining + tip confirm (khi booking d� COMPLETED) c?p nh?t `totalCollectedVnd`, `gatewayFeeVnd`, `platformFeeVnd`, `collection_mode` (ALL_ONLINE, MIXED, CASH) v� chuy?n settlement sang READY khi d? 100%.
- `WalletService` d?m b?o double-entry: credit idempotent, ch?y `recalculateBalanceFromLedger(walletId)` d? so s�nh running balance v?i `transport_wallet.current_balance_vnd`, cung c?p API `applyAdjustmentCredit/ Debit`, reversal payout t? d?ng.
- `PayoutService` duy tr� READY ? IN_PAYOUT ? PAID, debit v� d�ng m?t l?n; FAILED rollback v? READY/ON_HOLD v� sinh `REVERSAL`.

### Cross-cutting tests
- `backend/home-express-api/src/test/java/com/homeexpress/home_express_api/integration/PaymentSettlementPayoutFlowTest.java` ch?y b?ng MySQL Testcontainers (kh�ng mock) d? ch?ng minh:
  1. Deposit confirm c?p nh?t booking + settlement d�ng.
  2. Remaining + tip confirm ? settlement READY, v� credit `net_to_transport_vnd`.
  3. Payout COMPLETED ? settlement PAID, v� debit (`PAYOUT_DEBIT`).
  4. Payout FAILED ? settlement revert READY, v� c� `REVERSAL`, running balance kh?p l?i.
- C�c test ki?m tra tr?c ti?p b?ng `bookings`, `payments`, `booking_settlements`, `transport_wallets`, `transport_wallet_transactions`, `transport_payouts` d? s�t quy tr�nh MAIN_WORKFLOW.
