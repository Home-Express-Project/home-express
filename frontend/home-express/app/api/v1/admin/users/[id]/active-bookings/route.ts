import { type NextRequest } from "next/server"
import { proxyBackend } from "@/app/api/_lib/backend"

type AdminUserActiveBookingsParams = Promise<{ id: string }>

export async function GET(
  request: NextRequest,
  { params }: { params: AdminUserActiveBookingsParams }
) {
  const { id } = await params
  return proxyBackend(request, `/admin/users/${id}/active-bookings`)
}
