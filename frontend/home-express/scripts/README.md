# 🧪 Testing Scripts

## test-api-contracts.ts

Script kiểm tra tự động các điểm nối giữa frontend types và backend API responses.

### Prerequisites

1. Backend API đang chạy (default: `http://localhost:8084`)
2. Có JWT access token hợp lệ
3. Database có dữ liệu test

### Setup

```bash
# Install dependencies (if not already installed)
npm install

# Set environment variables
export NEXT_PUBLIC_API_URL=http://localhost:8084/api/v1
export TEST_ACCESS_TOKEN=your_jwt_token_here
```

### Getting Test Token

**Option 1: Login via API**
```bash
curl -X POST http://localhost:8084/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'

# Copy the access_token from response
```

**Option 2: Extract from Browser**
```javascript
// Open browser console on your app
localStorage.getItem('access_token')
```

### Running Tests

```bash
# Run all tests
npx ts-node scripts/test-api-contracts.ts

# Or add to package.json and run
npm run test:contracts
```

### Add to package.json

```json
{
  "scripts": {
    "test:contracts": "ts-node scripts/test-api-contracts.ts"
  }
}
```

### Test Coverage

The script tests the following integration points:

1. **Review API Contract**
   - ✅ Response structure (content array)
   - ✅ Required fields (review_id, booking_id, reviewer_name, etc.)
   - ⚠️ Missing fields (booking_pickup_location, booking_delivery_location, booking_completed_date)
   - ✅ Optional fields (reviewer_avatar, photo_urls, response)

2. **Notification API Contract**
   - ✅ Response structure
   - ✅ Required fields (notification_id, type, title, message)
   - ⚠️ Missing action_url field

3. **Booking Status Contract**
   - ✅ BookingStatus type validation
   - ✅ Required fields (bookingId, pickupLocation, status, etc.)

4. **Transport API Contract**
   - ✅ bank_name field (recently added)
   - ✅ Required fields (transport_id, company_name, phone)

5. **WebSocket Configuration**
   - ✅ Configuration check
   - ⚠️ Backend implementation check

### Expected Output

```
🚀 Starting API Contract Tests...

API Base URL: http://localhost:8084/api/v1
Token Configured: Yes

📋 Testing Review API Contract...
✅ Review API - Response Structure: Response has 'content' array
❌ Review API - Required Fields: Missing fields: booking_pickup_location, booking_delivery_location, booking_completed_date
✅ Review API - Optional Fields: Present: reviewer_avatar, photo_urls

🔔 Testing Notification API Contract...
✅ Notification API - Response Structure: Response has 'content' array
✅ Notification API - Required Fields: All required fields present
❌ Notification API - action_url Field: Missing 'action_url' field (used in frontend toast)

📦 Testing Booking Status Contract...
✅ Booking API - Status Type: Status 'PENDING' is valid
✅ Booking API - Required Fields: All required fields present

🚚 Testing Transport API Contract...
✅ Transport API - bank_name Field: bank_name field present
✅ Transport API - Required Fields: All required fields present

🔌 Testing WebSocket Endpoint...
✅ WebSocket - Configuration: WebSocket URL configured: ws://localhost:8084/ws
❌ WebSocket - Implementation: ⚠️ Backend WebSocket implementation not found in codebase

================================================================================
📊 TEST SUMMARY
================================================================================
Total Tests: 12
✅ Passed: 9
❌ Failed: 3
Success Rate: 75.0%

⚠️  Failed Tests:
   - Review API - Required Fields: Missing fields: booking_pickup_location, booking_delivery_location, booking_completed_date
   - Notification API - action_url Field: Missing 'action_url' field (used in frontend toast)
   - WebSocket - Implementation: ⚠️ Backend WebSocket implementation not found in codebase

================================================================================
```

### Interpreting Results

- **✅ Passed:** Integration point is working correctly
- **❌ Failed:** Mismatch between frontend types and backend responses
- **⚠️ Warning:** Configuration issue or missing implementation

### Fixing Failed Tests

See `INTEGRATION_REVIEW_REPORT.md` in project root for detailed recommendations on fixing each failed test.

### CI/CD Integration

Add to your CI pipeline:

```yaml
# .github/workflows/test.yml
- name: Run API Contract Tests
  env:
    NEXT_PUBLIC_API_URL: ${{ secrets.API_URL }}
    TEST_ACCESS_TOKEN: ${{ secrets.TEST_TOKEN }}
  run: npm run test:contracts
```

### Troubleshooting

**Error: "Unauthorized" or 401**
- Check if TEST_ACCESS_TOKEN is valid
- Token may have expired, get a new one

**Error: "Connection refused"**
- Check if backend API is running
- Verify NEXT_PUBLIC_API_URL is correct

**Error: "No data found"**
- Database may be empty
- Seed test data first

**TypeScript errors**
- Make sure all dependencies are installed: `npm install`
- Check tsconfig.json paths are correct

### Future Enhancements

- [ ] Add more test cases (Vehicle, Category, Payment APIs)
- [ ] Add performance benchmarks
- [ ] Generate HTML report
- [ ] Add snapshot testing
- [ ] Integrate with Jest/Vitest

