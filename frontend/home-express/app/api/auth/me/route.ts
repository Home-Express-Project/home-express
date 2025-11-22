import { type NextRequest } from "next/server"
import { proxyBackend } from "@/app/api/_lib/backend"

/**
 * GET /api/auth/me
 *
 * Proxy to the real backend authentication endpoint:
 *   GET /api/v1/auth/me
 *
 * The backend validates the HTTP-only access_token cookie and returns
 * the current user loaded from the database. This ensures there is
 * no mock or hardcoded auth state in the frontend layer.
 */
export async function GET(request: NextRequest) {
  return proxyBackend(request, "/auth/me")
}
