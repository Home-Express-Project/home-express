import { type NextRequest } from "next/server"
import { proxyBackend } from "@/app/api/_lib/backend"

type AdminUserDetailParams = Promise<{ id: string }>

export async function GET(
  request: NextRequest,
  { params }: { params: AdminUserDetailParams }
) {
  const { id } = await params
  return proxyBackend(request, `/admin/users/${id}`)
}
