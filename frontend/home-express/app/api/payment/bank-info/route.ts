import { type NextRequest } from "next/server"
import { proxyBackend } from "@/app/api/_lib/backend"

/**
 * GET /api/payment/bank-info
 *
 * This route now proxies to the real backend endpoint
 *   GET /api/v1/customer/payments/bank-info
 * so that bank transfer data always comes from the database-backed service.
 */
export async function GET(request: NextRequest) {
  const search = request.nextUrl.search || ""
  const path = `/customer/payments/bank-info${search}`
  return proxyBackend(request, path)
}
