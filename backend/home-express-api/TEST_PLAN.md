# KẾ HOẠCH UNIT TEST - LUỒNG CHÍNH
**Ngày tạo:** 2025-12-15  

## **TỔNG QUAN**

### **Modules cần test (theo thứ tự luồng):**
1. **Authentication & OTP** - Đăng ký & Đăng nhập
2. **Booking Flow** - 8 giai đoạn booking
3. **AI Detection** - Phân tích AI
4. **Quotation** - Báo giá
5. **Payment** - Thanh toán
6. **Settlement** - Quyết toán
7. **Payout** - Chi trả
8. **Dispute** - Tranh chấp
9. **Admin Dashboard** - Thống kê

### **Chiến lược test:**
- **Unit tests only** (mock dependencies)
- **Happy case first** (luồng thành công)
- **JUnit 5 + Mockito**
- **Coverage mục tiêu: 70-80%**

---

## **CHI TIẾT UNIT TESTS**

### **1. AuthServiceTest.java** 
**Package:** `service`  
**Dependencies cần mock:** `CustomerRepository`, `TransportRepository`, `ManagerRepository`, `PasswordEncoder`, `OtpService`

#### Test cases:
```java
@Test void testRegisterCustomer_Success()
@Test void testRegisterTransport_Success()
@Test void testLoginCustomer_Success()
@Test void testLoginTransport_Success()
@Test void testLoginManager_Success()
```

**Scenario:**
- ✅ Đăng ký customer mới với phone + password
- ✅ Đăng ký transport với đầy đủ thông tin (vehicle, license)
- ✅ Login thành công với phone + password đúng
- ✅ Password được hash/verify đúng
- ✅ Trả về JWT token

---

### **2. OtpServiceTest.java**
**Package:** `service`  
**Dependencies cần mock:** `OtpRepository`

#### Test cases:
```java
@Test void testGenerateOtp_Success()
@Test void testVerifyOtp_Success()
@Test void testOtpExpiry_After5Minutes()
```

**Scenario:**
- ✅ Generate OTP 6 digits cho phone number
- ✅ Verify OTP đúng trong vòng 5 phút
- ✅ OTP expired sau 5 phút

---

### **3. BookingServiceTest.java**
**Package:** `service`  
**Dependencies cần mock:** `BookingRepository`, `CustomerRepository`, `CategoryRepository`, `AiDetectionService`

#### Test cases (8 stages):
```java
@Test void testCreateBooking_Stage1_Draft()
@Test void testUpdateBooking_Stage2_PendingIntake()
@Test void testUpdateBooking_Stage3_IntakeInProgress()
@Test void testUpdateBooking_Stage4_PendingAssignment()
@Test void testUpdateBooking_Stage5_Assigned()
@Test void testUpdateBooking_Stage6_InProgress()
@Test void testUpdateBooking_Stage7_Completed()
@Test void testUpdateBooking_Stage8_Closed()
```

**Scenario:**
- ✅ Customer tạo booking mới (DRAFT)
- ✅ Chuyển sang PENDING_INTAKE sau submit
- ✅ Manager trigger AI → INTAKE_IN_PROGRESS
- ✅ AI xong → PENDING_ASSIGNMENT
- ✅ Manager assign Transport → ASSIGNED
- ✅ Transport bắt đầu → IN_PROGRESS
- ✅ Transport hoàn thành → COMPLETED
- ✅ Customer xác nhận/thanh toán → CLOSED

---

### **4. AiDetectionServiceTest.java**
**Package:** `service.ai`  
**Dependencies cần mock:** `AiItemRepository`, `BookingRepository`, `IntakeSessionRepository`

#### Test cases:
```java
@Test void testAnalyzeBooking_Success()
@Test void testDetectItems_FromImages()
@Test void testEstimateVolume_Success()
```

**Scenario:**
- ✅ Analyze booking và tạo IntakeSession
- ✅ Detect items từ images (AI mock)
- ✅ Estimate volume/weight từ items
- ✅ Lưu kết quả vào AiDetectionResult

---

### **5. QuotationServiceTest.java**
**Package:** `service`  
**Dependencies cần mock:** `QuotationRepository`, `BookingRepository`, `RateCardRepository`, `VehicleRepository`

#### Test cases:
```java
@Test void testCreateQuotation_Success()
@Test void testCalculatePrice_BasedOnVolume()
@Test void testApplyPricingRules_Success()
@Test void testCustomerAcceptQuotation_Success()
```

**Scenario:**
- ✅ Tạo quotation từ booking + AI result
- ✅ Calculate price dựa vào volume, distance, vehicle
- ✅ Apply pricing rules (base rate, per km, per kg)
- ✅ Customer accept quotation

---

### **6. PaymentServiceTest.java**
**Package:** `service`  
**Dependencies cần mock:** `PaymentRepository`, `BookingRepository`, `WalletService`, `ContractRepository`

#### Test cases:
```java
@Test void testCreatePayment_Success()
@Test void testProcessPayment_ByCash_Success()
@Test void testProcessPayment_ByWallet_Success()
@Test void testUpdatePaymentStatus_Success()
```

**Scenario:**
- ✅ Tạo payment từ quotation
- ✅ Process payment bằng CASH
- ✅ Process payment bằng WALLET
- ✅ Update payment status → COMPLETED

---

### **7. SettlementServiceTest.java**
**Package:** `service`  
**Dependencies cần mock:** `SettlementRepository`, `PaymentRepository`, `BookingRepository`

#### Test cases:
```java
@Test void testCreateSettlement_Success()
@Test void testCalculateSettlement_WithPlatformFee()
@Test void testCalculateTransportShare_Success()
```

**Scenario:**
- ✅ Tạo settlement từ payment
- ✅ Calculate platform fee (10%)
- ✅ Calculate transport share (90%)
- ✅ Lưu settlement record

---

### **8. PayoutServiceTest.java**
**Package:** `service`  
**Dependencies cần mock:** `PayoutRepository`, `SettlementRepository`, `WalletService`

#### Test cases:
```java
@Test void testCreatePayout_Success()
@Test void testProcessPayout_ToTransportWallet_Success()
@Test void testUpdatePayoutStatus_Success()
```

**Scenario:**
- ✅ Tạo payout từ settlement
- ✅ Transfer tiền vào transport wallet
- ✅ Update payout status → COMPLETED

---

### **9. DisputeServiceTest.java**
**Package:** `service`  
**Dependencies cần mock:** `DisputeRepository`, `BookingRepository`, `IncidentReportRepository`

#### Test cases:
```java
@Test void testCreateDispute_Success()
@Test void testResolveDispute_Success()
@Test void testCreateIncidentReport_Success()
```

**Scenario:**
- ✅ Customer/Transport tạo dispute
- ✅ Manager/Admin resolve dispute
- ✅ Tạo incident report nếu cần

---

### **10. AdminDashboardServiceTest.java**
**Package:** `service.admin`  
**Dependencies cần mock:** `BookingRepository`, `PaymentRepository`, `TransportRepository`, `CustomerRepository`

#### Test cases:
```java
@Test void testGetDashboardStats_Success()
@Test void testGetRevenueReport_Success()
@Test void testGetBookingStats_Success()
```

**Scenario:**
- ✅ Get tổng booking, revenue, users
- ✅ Get revenue report theo khoảng thời gian
- ✅ Get booking statistics (by status, by category)

---

## 🛠️ **SETUP & DEPENDENCIES**

### **Maven dependencies (đã có):**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
```

### **Base test class pattern:**
```java
@ExtendWith(MockitoExtension.class)
class ServiceTest {
    @Mock
    private DependencyRepository repository;
    
    @InjectMocks
    private ServiceUnderTest service;
    
    @BeforeEach
    void setUp() {
        // Setup test data
    }
}
```

---

## 📊 **THỨ TỰ THỰC HIỆN**

### **Phase 1: Core Services (2-3 giờ)**
1. ✅ AuthServiceTest
2. ✅ OtpServiceTest
3. ✅ BookingServiceTest

### **Phase 2: Business Logic (2-3 giờ)**
4. ✅ AiDetectionServiceTest
5. ✅ QuotationServiceTest
6. ✅ PaymentServiceTest

### **Phase 3: Financial Flow (1-2 giờ)**
7. ✅ SettlementServiceTest
8. ✅ PayoutServiceTest

### **Phase 4: Additional Features (1 giờ)**
9. ✅ DisputeServiceTest
10. ✅ AdminDashboardServiceTest

---

## ✅ **CHECKLIST HOÀN THÀNH**

- [ ] AuthServiceTest (5 tests)
- [ ] OtpServiceTest (3 tests)
- [ ] BookingServiceTest (8 tests)
- [ ] AiDetectionServiceTest (3 tests)
- [ ] QuotationServiceTest (4 tests)
- [ ] PaymentServiceTest (4 tests)
- [ ] SettlementServiceTest (3 tests)
- [ ] PayoutServiceTest (3 tests)
- [ ] DisputeServiceTest (3 tests)
- [ ] AdminDashboardServiceTest (3 tests)

**Tổng:** ~39-40 test cases

---

## 🚀 **RUN TESTS**

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Run with coverage
mvn test jacoco:report
```

---

## 📌 **LƯU Ý**

1. **Mock external dependencies** (repositories, APIs)
2. **Focus on happy case** (success scenarios)
3. **Use realistic test data** (phone: 0901234567, etc.)
4. **Assert critical values** (status, amount, relationships)
5. **Keep tests simple** (1 test = 1 scenario)
6. **Run tests before commit**

---

**Prepared for thesis defense** 🎓
