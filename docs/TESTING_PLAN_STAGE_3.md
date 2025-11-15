# Testing Plan - Stage 3.7 & 3.8

## Stage 3.7 - Job Flow Management

### Backend API Testing

#### 1. Start Job API (`PUT /api/v1/transport/bookings/{id}/start`)

**Test Cases:**
- [ ] ✅ Success: Start a CONFIRMED booking → returns IN_PROGRESS
- [ ] ❌ Error: Start booking with wrong transport (ownership)
- [ ] ❌ Error: Start booking that's not CONFIRMED
- [ ] ❌ Error: Start non-existent booking
- [ ] ✅ Verify: `actual_start_time` is set
- [ ] ✅ Verify: History logged to `booking_status_history`

**Test Data:**
```bash
# Valid request
PUT /api/v1/transport/bookings/1/start
Authorization: Bearer {transport_token}

# Expected Response:
{
  "message": "Job started successfully",
  "booking": {
    "bookingId": 1,
    "status": "IN_PROGRESS",
    "scheduledDatetime": "2025-11-15"
  }
}
```

#### 2. Complete Job API (`PUT /api/v1/transport/bookings/{id}/complete`)

**Test Cases:**
- [ ] ✅ Success: Complete an IN_PROGRESS booking → returns COMPLETED
- [ ] ❌ Error: Complete booking with wrong transport
- [ ] ❌ Error: Complete booking that's not IN_PROGRESS
- [ ] ❌ Error: Complete non-existent booking
- [ ] ✅ Verify: `actual_end_time` is set
- [ ] ✅ Verify: Completion notes saved
- [ ] ✅ Verify: History logged

**Test Data:**
```bash
# Valid request
PUT /api/v1/transport/bookings/1/complete
Content-Type: application/json
Authorization: Bearer {transport_token}

{
  "completionNotes": "Delivered successfully",
  "completionPhotos": ["url1", "url2"]
}

# Expected Response:
{
  "message": "Job completed successfully",
  "booking": {
    "bookingId": 1,
    "status": "COMPLETED",
    "completedDatetime": "2025-11-15T10:30:00"
  }
}
```

#### 3. Get Active Jobs API (`GET /api/v1/transport/active-jobs`)

**Test Cases:**
- [ ] ✅ Returns only CONFIRMED and IN_PROGRESS bookings
- [ ] ✅ Returns only transport's own jobs
- [ ] ✅ Filters by transport_id correctly
- [ ] ❌ Returns 403 for non-transport users

#### 4. Get Active Job Detail API (`GET /api/v1/transport/active-jobs/{id}`)

**Test Cases:**
- [ ] ✅ Returns full job details
- [ ] ✅ Includes status history
- [ ] ✅ Includes items list
- [ ] ✅ Includes contact info
- [ ] ❌ Returns 404 for job not assigned to transport
- [ ] ❌ Returns 403 for non-transport users

---

### Frontend Testing

#### 1. Active Jobs List Page (`/transport/active`)

**Test Cases:**
- [ ] ✅ Page loads without errors
- [ ] ✅ Shows loading skeleton while fetching
- [ ] ✅ Displays list of jobs correctly
- [ ] ✅ Status badges display correctly (CONFIRMED, IN_PROGRESS)
- [ ] ✅ Search works (by booking ID, address)
- [ ] ✅ Filter tabs work (All, CONFIRMED, IN_PROGRESS, COMPLETED)
- [ ] ✅ Empty state shows when no jobs
- [ ] ✅ Link to job detail works
- [ ] ✅ Responsive design (mobile/tablet/desktop)

**Manual Test Steps:**
1. Login as transport user
2. Navigate to `/transport/active`
3. Verify jobs list displays
4. Try search functionality
5. Try each filter tab
6. Click on a job card
7. Test on mobile viewport

#### 2. Active Job Detail Page (`/transport/active/[id]`)

**Test Cases:**
- [ ] ✅ Page loads without errors
- [ ] ✅ Shows loading state while fetching
- [ ] ✅ Displays job info correctly
- [ ] ✅ Timeline/stepper shows current status
- [ ] ✅ "Bắt đầu công việc" button shows for CONFIRMED
- [ ] ✅ "Hoàn thành công việc" button shows for IN_PROGRESS
- [ ] ✅ No action button shows for COMPLETED
- [ ] ✅ Clicking "Bắt đầu" calls startJob API
- [ ] ✅ Success toast appears after start
- [ ] ✅ Status updates automatically after start
- [ ] ✅ Clicking "Hoàn thành" calls completeJob API
- [ ] ✅ Success toast appears after complete
- [ ] ✅ Redirects to list after complete
- [ ] ❌ Error toast shows on API failure
- [ ] ✅ Contact links work (tel:)
- [ ] ✅ Items list displays correctly
- [ ] ✅ Back button works

**Manual Test Steps:**
1. Go to job with status CONFIRMED
2. Click "Bắt đầu công việc"
3. Verify status changes to IN_PROGRESS
4. Verify toast notification
5. Click "Hoàn thành công việc"
6. Verify status changes to COMPLETED
7. Verify redirect to list page

---

## Stage 3.8 - Settlements & Payouts

### Backend API Testing

#### 1. Get Settlements API (`GET /api/v1/transport/settlements`)

**Test Cases:**
- [ ] ✅ Returns settlements list
- [ ] ✅ Pagination works (page, size params)
- [ ] ✅ Filter by status works (PENDING, READY, etc.)
- [ ] ✅ Returns only transport's own settlements
- [ ] ❌ Returns 403 for non-transport users

#### 2. Get Settlement Summary API (`GET /api/v1/transport/settlements/summary`)

**Test Cases:**
- [ ] ✅ Returns correct counts for each status
- [ ] ✅ Returns correct totals for each status
- [ ] ✅ Calculates amounts correctly

#### 3. Get Payouts API (`GET /api/v1/transport/payouts`)

**Test Cases:**
- [ ] ✅ Returns payouts list
- [ ] ✅ Pagination works
- [ ] ✅ Returns only transport's own payouts
- [ ] ❌ Returns 403 for non-transport users

---

### Frontend Testing

#### 1. Settlements List Page (`/transport/settlements`)

**Test Cases:**
- [ ] ✅ Page loads without errors
- [ ] ✅ Summary cards load and display correctly
- [ ] ✅ Shows correct values for each status
- [ ] ✅ Settlements list displays
- [ ] ✅ Search works (by settlement ID, booking ID)
- [ ] ✅ Filter tabs work (All, PENDING, READY, IN_PAYOUT, PAID, ON_HOLD)
- [ ] ✅ Status badges with icons display correctly
- [ ] ✅ Amount formatting correct (VND)
- [ ] ✅ Date formatting correct
- [ ] ✅ Collection mode displays correctly
- [ ] ✅ Pagination works
- [ ] ✅ Empty state shows when no settlements
- [ ] ✅ Link to detail works
- [ ] ✅ Responsive design

**Manual Test Steps:**
1. Login as transport user
2. Navigate to `/transport/settlements`
3. Verify summary cards show data
4. Try search functionality
5. Try each filter tab
6. Test pagination
7. Click on settlement card

#### 2. Payouts List Page (`/transport/payouts`)

**Test Cases:**
- [ ] ✅ Page loads without errors
- [ ] ✅ Payouts list displays
- [ ] ✅ Search works (by payout number)
- [ ] ✅ Status badges display correctly
- [ ] ✅ Timeline shows correctly (Created, Processed, Completed)
- [ ] ✅ Amount formatting correct
- [ ] ✅ Item count displays
- [ ] ✅ Pagination works
- [ ] ✅ Empty state shows when no payouts
- [ ] ✅ Link to settlements works
- [ ] ✅ Link to detail works
- [ ] ✅ Responsive design

---

## Integration Testing

### 1. Complete Job Flow

**Test Scenario:**
1. Create a booking (as customer)
2. Submit quotation (as transport)
3. Accept quotation (as customer) → CONFIRMED
4. Start job (as transport) → IN_PROGRESS
5. Complete job (as transport) → COMPLETED
6. Verify settlement created automatically
7. Check settlement appears in settlements list

**Expected Results:**
- [ ] Job status transitions correctly
- [ ] Status history logged at each step
- [ ] Settlement created after completion
- [ ] Settlement amount matches job final_price
- [ ] All timestamps recorded correctly

### 2. Ownership & Authorization

**Test Scenario:**
1. Transport A creates job
2. Transport B tries to start/complete Transport A's job

**Expected Results:**
- [ ] Transport B gets 403/400 error
- [ ] Error message: "You are not assigned to this booking"
- [ ] Job status remains unchanged

---

## Edge Cases & Error Handling

### 1. Network Errors
- [ ] Handle timeout gracefully
- [ ] Show error toast
- [ ] Don't corrupt UI state

### 2. Invalid State Transitions
- [ ] Can't start COMPLETED job
- [ ] Can't complete CONFIRMED job (must start first)
- [ ] Clear error messages

### 3. Concurrent Updates
- [ ] Two requests to start same job
- [ ] Handle race conditions

### 4. Empty States
- [ ] No active jobs
- [ ] No settlements
- [ ] No payouts
- [ ] Show helpful empty states

---

## Performance Testing

### 1. Load Time
- [ ] Active jobs list loads < 2s
- [ ] Job detail loads < 1s
- [ ] Settlements page loads < 2s
- [ ] Payouts page loads < 2s

### 2. Pagination
- [ ] Large datasets (100+ items) paginate smoothly
- [ ] No lag when changing pages

---

## Browser Compatibility

Test on:
- [ ] Chrome (latest)
- [ ] Firefox (latest)
- [ ] Safari (if Mac available)
- [ ] Edge (latest)
- [ ] Mobile browsers (Chrome/Safari)

---

## Accessibility

- [ ] Keyboard navigation works
- [ ] Focus indicators visible
- [ ] Screen reader compatible (basic)
- [ ] Color contrast sufficient

---

## Known Issues / TODOs

1. ⏸️ **SSE Real-time Updates**: Disabled - backend endpoint not implemented
2. ⚠️ **Settlement Detail Pages**: Link exists but pages not created yet
3. ⚠️ **Payout Detail Pages**: Link exists but pages not created yet
4. 💡 **Export Functionality**: Placeholder - needs implementation

---

## Test Results

### Backend Tests
- **Job Start API**: 
- **Job Complete API**: 
- **Active Jobs API**: 
- **Settlements API**: 
- **Payouts API**: 

### Frontend Tests
- **Active Jobs List**: 
- **Job Detail**: 
- **Settlements List**: 
- **Payouts List**: 

### Integration Tests
- **Complete Flow**: 
- **Authorization**: 

---

## Bug Fixes Applied

### Bug #1: 
**Description**: 
**Fix**: 
**Status**: 

### Bug #2:
**Description**: 
**Fix**: 
**Status**: 

---

**Test Date**: 2025-11-15
**Tester**: 
**Environment**: Development
