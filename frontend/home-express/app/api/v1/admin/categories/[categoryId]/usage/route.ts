import { type NextRequest } from "next/server"
import { proxyBackend } from "@/app/api/_lib/backend"

type CategoryUsageParams = Promise<{ categoryId: string }>

export async function GET(
  request: NextRequest,
  { params }: { params: CategoryUsageParams }
) {
  const { categoryId } = await params
  return proxyBackend(request, `/admin/categories/${categoryId}/usage`)
}
