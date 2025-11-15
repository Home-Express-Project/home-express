import { type NextRequest } from "next/server"
import { proxyBackend } from "@/app/api/_lib/backend"

type AdminUserDeactivateParams = Promise<{ id: string }>

export async function PUT(
  request: NextRequest,
  { params }: { params: AdminUserDeactivateParams }
) {
  const { id } = await params
  return proxyBackend(request, `/admin/users/${id}/deactivate`)
}
