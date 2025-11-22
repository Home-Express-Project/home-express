"use client"

import { useState } from "react"
import Link from "next/link"
import useSWR from "swr"
import { DashboardLayout } from "@/components/dashboard/dashboard-layout"
import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import {
  Truck,
  Package,
  FileBarChart,
  FileText,
  Settings,
  DollarSign,
  BarChart3,
  Star,
  Search,
  ArrowRight,
  CheckCircle2,
  Clock,
  XCircle,
  Loader2,
} from "lucide-react"
import { apiClient } from "@/lib/api-client"
import { formatVND, formatDate } from "@/lib/format"
import { EmptyState } from "@/components/empty-state"

const navItems = [
  { href: "/transport", label: "Tổng quan", icon: "Package" },
  { href: "/transport/jobs", label: "Công việc", icon: "Truck" },
  { href: "/transport/quotations", label: "Báo giá", icon: "FileBarChart" },
  { href: "/transport/active", label: "Đang thực hiện", icon: "Truck" },
  { href: "/transport/contracts", label: "Hợp đồng", icon: "FileText" },
  { href: "/transport/vehicles", label: "Xe", icon: "Truck" },
  { href: "/transport/earnings", label: "Thu nhập", icon: "DollarSign" },
  { href: "/transport/analytics", label: "Phân tích", icon: "BarChart3" },
  { href: "/transport/reviews", label: "Đánh giá", icon: "Star" },
  { href: "/transport/settings", label: "Cài đặt", icon: "Settings" },
]

export default function PayoutsPage() {
  const [searchQuery, setSearchQuery] = useState("")
  const [currentPage, setCurrentPage] = useState(0)

  const { data: payoutsData, isLoading } = useSWR(
    `/transport/payouts?page=${currentPage}`,
    () =>
      apiClient.getPayouts({
        page: currentPage,
        size: 20,
      })
  )

  const payouts = payoutsData?.payouts || []
  const totalPages = payoutsData?.totalPages || 1

  const filteredPayouts = payouts.filter((payout: any) => {
    if (!searchQuery) return true
    return (
      payout.payoutId.toString().includes(searchQuery) ||
      payout.payoutNumber?.toLowerCase().includes(searchQuery.toLowerCase())
    )
  })

  const getStatusBadge = (status: string) => {
    const variants: Record<string, { variant: any; label: string; icon: any }> = {
      PENDING: { variant: "secondary", label: "Chờ xử lý", icon: Clock },
      PROCESSING: { variant: "default", label: "Đang xử lý", icon: Loader2 },
      COMPLETED: { variant: "success", label: "Hoàn thành", icon: CheckCircle2 },
      FAILED: { variant: "destructive", label: "Thất bại", icon: XCircle },
    }
    const config = variants[status] || variants.PENDING
    const Icon = config.icon
    return (
      <Badge variant={config.variant} className="flex items-center gap-1">
        <Icon className="h-3 w-3" />
        {config.label}
      </Badge>
    )
  }

  return (
    <DashboardLayout navItems={navItems} title="Chi trả">
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Lịch sử chi trả</h1>
            <p className="text-muted-foreground mt-2">
              Theo dõi các lần chi trả từ hệ thống
            </p>
          </div>
          <Button asChild>
            <Link href="/transport/settlements">
              <DollarSign className="mr-2 h-4 w-4" />
              Xem quyết toán
            </Link>
          </Button>
        </div>

        {/* Search */}
        <Card>
          <CardContent className="pt-6">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Tìm theo mã chi trả..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9"
              />
            </div>
          </CardContent>
        </Card>

        {/* Payouts List */}
        {isLoading ? (
          <div className="grid gap-4">
            {[1, 2, 3].map((i) => (
              <Card key={i} className="animate-pulse">
                <CardContent className="p-6">
                  <div className="h-24 bg-muted rounded" />
                </CardContent>
              </Card>
            ))}
          </div>
        ) : filteredPayouts && filteredPayouts.length > 0 ? (
          <>
            <div className="grid gap-4">
              {filteredPayouts.map((payout: any) => (
                <Card key={payout.payoutId} className="hover:shadow-md transition-shadow">
                  <CardContent className="p-6">
                    <div className="flex items-start justify-between mb-4">
                      <div>
                        <div className="flex items-center gap-2 mb-1">
                          <h3 className="font-semibold text-lg">
                            {payout.payoutNumber || `Payout #${payout.payoutId}`}
                          </h3>
                          {getStatusBadge(payout.status)}
                        </div>
                        <p className="text-sm text-muted-foreground">
                          {formatDate(payout.createdAt)}
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="text-sm text-muted-foreground mb-1">Tổng số tiền</p>
                        <p className="text-2xl font-bold text-accent-green">
                          {formatVND(payout.totalAmountVnd)}
                        </p>
                        <p className="text-xs text-muted-foreground mt-1">
                          {payout.itemCount} quyết toán
                        </p>
                      </div>
                    </div>

                    {/* Status Timeline */}
                    <div className="flex flex-wrap gap-4 text-sm text-muted-foreground mb-4">
                      <div className="flex items-center gap-1">
                        <Clock className="h-3 w-3" />
                        <span>Tạo: {formatDate(payout.createdAt)}</span>
                      </div>
                      {payout.processedAt && (
                        <div className="flex items-center gap-1">
                          <Loader2 className="h-3 w-3" />
                          <span>Xử lý: {formatDate(payout.processedAt)}</span>
                        </div>
                      )}
                      {payout.completedAt && (
                        <div className="flex items-center gap-1">
                          <CheckCircle2 className="h-3 w-3 text-success" />
                          <span>Hoàn thành: {formatDate(payout.completedAt)}</span>
                        </div>
                      )}
                    </div>

                    {/* Actions */}
                    <div className="flex gap-2">
                      <Button asChild variant="outline" className="flex-1">
                        <Link href={`/transport/payouts/${payout.payoutId}`}>
                          Xem chi tiết
                          <ArrowRight className="ml-2 h-4 w-4" />
                        </Link>
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex items-center justify-center gap-2 mt-6">
                <Button
                  variant="outline"
                  onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                  disabled={currentPage === 0}
                >
                  Trước
                </Button>
                <span className="text-sm text-muted-foreground">
                  Trang {currentPage + 1} / {totalPages}
                </span>
                <Button
                  variant="outline"
                  onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
                  disabled={currentPage >= totalPages - 1}
                >
                  Sau
                </Button>
              </div>
            )}
          </>
        ) : (
          <EmptyState
            title="Chưa có chi trả nào"
            description="Lịch sử chi trả sẽ hiển thị ở đây khi có quyết toán được xử lý"
            action={
              <Button asChild>
                <Link href="/transport/settlements">Xem quyết toán</Link>
              </Button>
            }
          />
        )}
      </div>
    </DashboardLayout>
  )
}
