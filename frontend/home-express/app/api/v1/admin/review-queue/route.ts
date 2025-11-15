import { type NextRequest, NextResponse } from "next/server"
import { requestBackend } from "@/app/api/_lib/backend"

// Backend DTOs (shape inferred from AdminIntakeSessionDetailResponse)
type BackendCustomerInfo = {
  customerId: number
  customerName: string
  customerEmail: string
  customerAvatar: string | null
}

type BackendSession = {
  sessionId: string
  customerId: number | null
  status: string
  imageUrls?: string[]
  imageCount?: number
  detectionResults?: string | null
  averageConfidence?: number | null
  items?: any[]
  estimatedPrice?: number | null
  estimatedWeightKg?: number | null
  estimatedVolumeM3?: number | null
  forcedQuotePrice?: number | null
  aiServiceUsed?: string | null
  createdAt: string
  updatedAt?: string
  expiresAt?: string | null
  customer?: BackendCustomerInfo | null
}

type BackendListResponse = {
  sessions: BackendSession[]
  total: number
  page: number
  size: number
}

function mapStatus(status: string | null | undefined): string {
  if (!status) return "PENDING"
  const normalized = status.toLowerCase()
  switch (normalized) {
    case "active":
      // Sessions waiting for admin review
      return "NEEDS_REVIEW"
    case "published":
      return "PUBLISHED"
    case "expired":
      return "FAILED"
    default:
      return status.toUpperCase()
  }
}

function parseDetectionResults(raw: string | null | undefined): any | null {
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return raw
  }
}

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = request.nextUrl
    const statusParam = searchParams.get("status")
    const searchQuery = (searchParams.get("search") || "").toLowerCase()

    const backendParams = new URLSearchParams()

    // Map frontend status filter -> backend string status
    if (statusParam) {
      let backendStatus: string | undefined
      switch (statusParam) {
        case "NEEDS_REVIEW":
          backendStatus = "active"
          break
        case "PUBLISHED":
          backendStatus = "published"
          break
        case "FAILED":
          backendStatus = "expired"
          break
        default:
          backendStatus = statusParam.toLowerCase()
      }

      if (backendStatus) {
        backendParams.set("status", backendStatus)
      }
    }

    // Reasonable defaults for admin list pagination
    backendParams.set("page", "0")
    backendParams.set("size", "50")

    const backendPath = `/admin/sessions?${backendParams.toString()}`
    const backendResponse = await requestBackend(request, backendPath)

    if (!backendResponse.ok) {
      return new NextResponse(backendResponse.body, {
        status: backendResponse.status,
        headers: backendResponse.headers,
      })
    }

    const data = (await backendResponse.json()) as BackendListResponse
    const rawSessions = data.sessions || []

    const sessions = rawSessions.map((session) => {
      const mappedStatus = mapStatus(session.status)
      const customer = session.customer
      const createdAt = session.createdAt

      return {
        // Core ScanSession fields
        session_id: session.sessionId,
        customer_id: session.customerId,
        status: mappedStatus,
        image_urls: session.imageUrls || [],
        image_count: session.imageCount ?? (session.imageUrls?.length ?? 0),
        detection_results: parseDetectionResults(session.detectionResults),
        average_confidence: session.averageConfidence ?? null,
        items: session.items || [],
        created_at: createdAt,
        expires_at: session.expiresAt ?? null,

        // Extra estimation metadata (not all are used in the list view, but
        // they are useful when drilling into a specific session)
        estimated_price: session.estimatedPrice ?? null,
        estimated_weight_kg: session.estimatedWeightKg ?? null,
        estimated_volume_m3: session.estimatedVolumeM3 ?? null,
        forced_quote_price: session.forcedQuotePrice ?? null,
        ai_service_used: session.aiServiceUsed ?? null,

        // Flattened customer info for ScanSessionWithCustomer
        customer_name: customer?.customerName || "Unknown",
        customer_email: customer?.customerEmail || "",
        customer_avatar: customer?.customerAvatar || null,
      }
    })

    const filteredSessions =
      searchQuery.length === 0
        ? sessions
        : sessions.filter((s) => {
            const name = s.customer_name.toLowerCase()
            const email = s.customer_email.toLowerCase()
            const idStr = String(s.session_id)
            return (
              name.includes(searchQuery) ||
              email.includes(searchQuery) ||
              idStr.includes(searchQuery)
            )
          })

    const total = filteredSessions.length
    const avgConfidence =
      total === 0
        ? 0
        :
          filteredSessions.reduce(
            (sum, s) => sum + (s.average_confidence ?? 0),
            0,
          ) / total

    const now = Date.now()
    const oldestWaitTime =
      total === 0
        ? 0
        : filteredSessions.reduce((max, s) => {
            const created = s.created_at ? new Date(s.created_at).getTime() : now
            const diffMinutes = Math.max(0, (now - created) / (1000 * 60))
            return Math.max(max, diffMinutes)
          }, 0)

    return NextResponse.json({
      sessions: filteredSessions,
      stats: {
        total,
        avgConfidence,
        oldestWaitTime,
      },
    })
  } catch (error) {
    console.error("Error fetching review queue:", error)
    return NextResponse.json(
      { error: "Unable to fetch review queue" },
      { status: 500 },
    )
  }
}
