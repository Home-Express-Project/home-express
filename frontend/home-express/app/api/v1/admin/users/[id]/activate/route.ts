import { type NextRequest } from "next/server"
import { proxyBackend } from "@/app/api/_lib/backend"

type AdminUserActivateParams = Promise<{ id: string }>

export async function PUT(
  request: NextRequest,
  { params }: { params: AdminUserActivateParams }
) {
  const { id } = await params
  return proxyBackend(request, `/admin/users/${id}/activate`)
}
