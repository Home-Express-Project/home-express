/**
 * API Contract Testing Script
 * 
 * Kiểm tra các điểm nối giữa frontend types và backend API responses
 * 
 * Usage:
 *   npx ts-node scripts/test-api-contracts.ts
 * 
 * Hoặc thêm vào package.json:
 *   "scripts": {
 *     "test:contracts": "ts-node scripts/test-api-contracts.ts"
 *   }
 */

import type {
  ReviewWithDetails,
  BookingStatus,
  Transport,
  Vehicle,
  Category,
  CategoryWithSizes,
  BookingListItem,
  CreateBookingResponse,
} from "@/types"
import { API_BASE_URL, buildApiUrl } from "@/lib/api-url"

// ============================================================================
// Configuration
// ============================================================================

const TEST_TOKEN = process.env.TEST_ACCESS_TOKEN || ""

// ============================================================================
// Test Utilities
// ============================================================================

interface TestResult {
  name: string
  passed: boolean
  message: string
  details?: any
}

const results: TestResult[] = []

function logTest(name: string, passed: boolean, message: string, details?: any) {
  results.push({ name, passed, message, details })
  const icon = passed ? "✅" : "❌"
  console.log(`${icon} ${name}: ${message}`)
  if (details && !passed) {
    console.log("   Details:", JSON.stringify(details, null, 2))
  }
}

async function fetchAPI(endpoint: string, options: RequestInit = {}) {
  const response = await fetch(buildApiUrl(endpoint), {
    ...options,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${TEST_TOKEN}`,
      ...options.headers,
    },
  })

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}: ${response.statusText}`)
  }

  return response.json()
}

// ============================================================================
// Test Cases
// ============================================================================

async function testReviewAPIContract() {
  console.log("\n📋 Testing Review API Contract...")

  try {
    const response = await fetchAPI("/reviews?page=0&size=5")

    // Check if response has content
    if (!response.content || !Array.isArray(response.content)) {
      logTest("Review API - Response Structure", false, "Response should have 'content' array", response)
      return
    }

    if (response.content.length === 0) {
      logTest("Review API - Data Available", false, "No reviews found in database", null)
      return
    }

    const review = response.content[0]

    // Check required fields for ReviewWithDetails
    const requiredFields: (keyof ReviewWithDetails)[] = [
      "review_id",
      "booking_id",
      "reviewer_name",
      "reviewee_name",
      "rating",
      "booking_pickup_location", // ⚠️ MISSING in backend
      "booking_delivery_location", // ⚠️ MISSING in backend
      "booking_completed_date", // ⚠️ MISSING in backend
    ]

    const missingFields = requiredFields.filter((field) => !(field in review))

    if (missingFields.length > 0) {
      logTest(
        "Review API - Required Fields",
        false,
        `Missing fields: ${missingFields.join(", ")}`,
        { review, missingFields },
      )
    } else {
      logTest("Review API - Required Fields", true, "All required fields present")
    }

    // Check optional fields
    const optionalFields = ["reviewer_avatar", "photo_urls", "response", "responded_at"]
    const presentOptionalFields = optionalFields.filter((field) => field in review)

    logTest(
      "Review API - Optional Fields",
      true,
      `Present: ${presentOptionalFields.join(", ") || "none"}`,
    )
  } catch (error: any) {
    logTest("Review API - Connection", false, error.message)
  }
}

async function testNotificationAPIContract() {
  console.log("\n🔔 Testing Notification API Contract...")

  try {
    const response = await fetchAPI("/notifications?page=0&size=5")

    if (!response.content || !Array.isArray(response.content)) {
      logTest("Notification API - Response Structure", false, "Response should have 'content' array", response)
      return
    }

    if (response.content.length === 0) {
      logTest("Notification API - Data Available", false, "No notifications found", null)
      return
    }

    const notification = response.content[0]

    // Check required fields
    const requiredFields = [
      "notification_id",
      "type",
      "title",
      "message",
      "is_read",
      "created_at",
    ]

    const missingFields = requiredFields.filter((field) => !(field in notification))

    if (missingFields.length > 0) {
      logTest(
        "Notification API - Required Fields",
        false,
        `Missing fields: ${missingFields.join(", ")}`,
        { notification, missingFields },
      )
    } else {
      logTest("Notification API - Required Fields", true, "All required fields present")
    }

    // Check for action_url field (used in frontend)
    if (!("action_url" in notification)) {
      logTest(
        "Notification API - action_url Field",
        false,
        "Missing 'action_url' field (used in frontend toast)",
        { notification },
      )
    } else {
      logTest("Notification API - action_url Field", true, "action_url field present")
    }
  } catch (error: any) {
    logTest("Notification API - Connection", false, error.message)
  }
}

async function testBookingStatusContract() {
  console.log("\n📦 Testing Booking Status Contract...")

  try {
    const response = await fetchAPI("/customer/bookings?page=0&size=5")

    if (!response.data || !Array.isArray(response.data)) {
      logTest("Booking API - Response Structure", false, "Response should have 'data' array", response)
      return
    }

    if (response.data.length === 0) {
      logTest("Booking API - Data Available", false, "No bookings found", null)
      return
    }

    const booking: BookingListItem = response.data[0]

    // Check BookingStatus type
    const validStatuses: BookingStatus[] = [
      "PENDING",
      "QUOTED",
      "CONFIRMED",
      "IN_PROGRESS",
      "COMPLETED",
      "REVIEWED",
      "CANCELLED",
    ]

    if (!validStatuses.includes(booking.status)) {
      logTest(
        "Booking API - Status Type",
        false,
        `Invalid status: ${booking.status}. Expected one of: ${validStatuses.join(", ")}`,
        { booking },
      )
    } else {
      logTest("Booking API - Status Type", true, `Status '${booking.status}' is valid`)
    }

    // Check required fields
    const requiredFields: (keyof BookingListItem)[] = [
      "bookingId",
      "pickupLocation",
      "deliveryLocation",
      "status",
      "estimatedPrice",
      "createdAt",
    ]

    const missingFields = requiredFields.filter((field) => !(field in booking))

    if (missingFields.length > 0) {
      logTest(
        "Booking API - Required Fields",
        false,
        `Missing fields: ${missingFields.join(", ")}`,
        { booking, missingFields },
      )
    } else {
      logTest("Booking API - Required Fields", true, "All required fields present")
    }
  } catch (error: any) {
    logTest("Booking API - Connection", false, error.message)
  }
}

async function testTransportAPIContract() {
  console.log("\n🚚 Testing Transport API Contract...")

  try {
    const response = await fetchAPI("/transport/profile")

    if (!response || typeof response !== "object") {
      logTest("Transport API - Response Structure", false, "Response should be an object", response)
      return
    }

    const transport: Transport = response

    // Check bank_name field (added in recent fix)
    if (!("bank_name" in transport)) {
      logTest(
        "Transport API - bank_name Field",
        false,
        "Missing 'bank_name' field (added in frontend types)",
        { transport },
      )
    } else {
      logTest("Transport API - bank_name Field", true, "bank_name field present")
    }

    // Check other required fields
    const requiredFields: (keyof Transport)[] = [
      "transport_id",
      "company_name",
      "phone",
      "verification_status",
    ]

    const missingFields = requiredFields.filter((field) => !(field in transport))

    if (missingFields.length > 0) {
      logTest(
        "Transport API - Required Fields",
        false,
        `Missing fields: ${missingFields.join(", ")}`,
        { transport, missingFields },
      )
    } else {
      logTest("Transport API - Required Fields", true, "All required fields present")
    }
  } catch (error: any) {
    logTest("Transport API - Connection", false, error.message)
  }
}

async function testWebSocketEndpoint() {
  console.log("\n🔌 Testing WebSocket Endpoint...")

  const WS_URL = process.env.NEXT_PUBLIC_WS_URL

  if (!WS_URL || WS_URL === "ws://localhost:8084/ws") {
    logTest("WebSocket - Configuration", false, "WebSocket URL not configured or using default", { WS_URL })
    return
  }

  // Note: Cannot actually test WebSocket connection in Node.js without ws library
  // This is just a configuration check
  logTest("WebSocket - Configuration", true, `WebSocket URL configured: ${WS_URL}`)
  logTest(
    "WebSocket - Implementation",
    false,
    "⚠️ Backend WebSocket implementation not found in codebase",
    {
      recommendation: "Implement WebSocket handler in backend or use SSE for real-time updates",
    },
  )
}

// ============================================================================
// Main Test Runner
// ============================================================================

async function runAllTests() {
  console.log("🚀 Starting API Contract Tests...\n")
  console.log(`API Base URL: ${API_BASE_URL}`)
  console.log(`Token Configured: ${TEST_TOKEN ? "Yes" : "No (tests may fail)"}\n`)

  await testReviewAPIContract()
  await testNotificationAPIContract()
  await testBookingStatusContract()
  await testTransportAPIContract()
  await testWebSocketEndpoint()

  // Summary
  console.log("\n" + "=".repeat(80))
  console.log("📊 TEST SUMMARY")
  console.log("=".repeat(80))

  const passed = results.filter((r) => r.passed).length
  const failed = results.filter((r) => !r.passed).length
  const total = results.length

  console.log(`Total Tests: ${total}`)
  console.log(`✅ Passed: ${passed}`)
  console.log(`❌ Failed: ${failed}`)
  console.log(`Success Rate: ${((passed / total) * 100).toFixed(1)}%`)

  if (failed > 0) {
    console.log("\n⚠️  Failed Tests:")
    results
      .filter((r) => !r.passed)
      .forEach((r) => {
        console.log(`   - ${r.name}: ${r.message}`)
      })
  }

  console.log("\n" + "=".repeat(80))

  // Exit with error code if any tests failed
  process.exit(failed > 0 ? 1 : 0)
}

// Run tests
runAllTests().catch((error) => {
  console.error("❌ Test runner failed:", error)
  process.exit(1)
})
