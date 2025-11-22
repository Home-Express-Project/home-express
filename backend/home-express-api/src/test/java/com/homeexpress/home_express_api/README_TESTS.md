# Unit Tests - Member 2 (Quy)

## Overview

Unit tests cho các services và controllers chính của Member 2, bao gồm:
- BookingService
- QuotationService
- PaymentService
- AdminBookingController

## Test Coverage

### BookingServiceTest
- `getBookingTimeline()` - Timeline với status history
- `getBookingTimeline()` - Timeline với quotations
- `getBookingTimeline()` - Timeline với payments
- `getBookingTimeline()` - Booking not found
- `getBookingTimeline()` - Events sorted by timestamp

### QuotationServiceTest
- `getQuotationById()` - Success case
- `getQuotationById()` - Not found
- `rejectQuotation()` - Success
- `rejectQuotation()` - Invalid status
- `rejectQuotation()` - Not found
- `createQuotation()` - Success

### PaymentServiceTest
- `getPaymentSummary()` - Success
- `getPaymentSummary()` - Booking not found
- `getPaymentSummary()` - Unauthorized
- `initializePayment()` - Success
- `confirmPayment()` - Success
- `confirmPayment()` - Invalid status
- `confirmPayment()` - Not found

### AdminBookingControllerTest
- `getBookingTimeline()` - Success
- `getBookingTimeline()` - Not found
- `getBookingTimeline()` - Unauthorized

### CustomExceptionTest
- Exception constructors and messages

## Running Tests

### Maven
```bash
cd backend/home-express-api
mvn test
```

### Run specific test class
```bash
mvn test -Dtest=BookingServiceTest
```

### Run with coverage
```bash
mvn test jacoco:report
```

## Test Dependencies

Tests sử dụng:
- JUnit 5 (`@Test`, `@BeforeEach`, `@ExtendWith`)
- Mockito (`@Mock`, `@InjectMocks`, `when()`, `verify()`)
- Spring Boot Test (`@SpringBootTest` cho integration tests)

## Notes

- Các test files có thể hiển thị lỗi trong IDE nếu dependencies chưa được resolve
- Chạy `mvn clean compile test-compile` để resolve dependencies
- Integration tests có thể được thêm vào với `@SpringBootTest` và `@AutoConfigureMockMvc`

