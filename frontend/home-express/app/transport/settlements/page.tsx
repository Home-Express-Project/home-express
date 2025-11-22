"use client"

import { useState } from "react"
import Link from "next/link"
import useSWR from "swr"
import { DashboardLayout } from "@/components/dashboard/dashboard-layout"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs"
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
  Clock,
  CheckCircle2,
  AlertCircle,
  PauseCircle,
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

export default function SettlementsPage() {
  const [searchQuery, setSearchQuery] = useState("")
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [currentPage, setCurrentPage] = useState(0)

  const { data: summary, isLoading: summaryLoading } = useSWR(
    "/transport/settlements/summary",
    () => apiClient.getSettlementSummary()
  )

  const { data: settlementsData, isLoading } = useSWR(
    `/transport/settlements?status=${statusFilter !== "all" ? statusFilter : ""}&page=${currentPage}`,
    () =>
      apiClient.getSettlements({
        status: statusFilter !== "all" ? statusFilter : undefined,
        page: currentPage,
        size: 20,
      })
  )

  const settlements = settlementsData?.settlements || []
  const totalPages = settlementsData?.totalPages || 1

  const filteredSettlements = settlements.filter((settlement: any) => {
    if (!searchQuery) return true
    return (
      settlement.settlementId.toString().includes(searchQuery) ||
      settlement.bookingId.toString().includes(searchQuery)
    )
  })

  const getStatusBadge = (status: string) => {
    const variants: Record<string, { variant: any; label: string; icon: any }> = {
      PENDING: { variant: "secondary", label: "Chờ xử lý", icon: Clock },
      READY: { variant: "default", label: "Sẵn sàng", icon: CheckCircle2 },
      IN_PAYOUT: { variant: "secondary", label: "Đang chi trả", icon: Loader2 },
      PAID: { variant: "success", label: "Đã thanh toán", icon: CheckCircle2 },
      ON_HOLD: { variant: "destructive", label: "Tạm giữ", icon: PauseCircle },
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
    <DashboardLayout navItems={navItems} title="Quyết toán">
      <div className="space-y-6">
        {/* Header */}
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Quyết toán</h1>
          <p className="text-muted-foreground mt-2">
            Quản lý các khoản quyết toán từ công việc đã hoàn thành
          </p>
        </div>

        {/* Summary Cards */}
        {summaryLoading ? (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            {[1, 2, 3, 4].map((i) => (
              <Card key={i} className="animate-pulse">
                <CardContent className="p-6">
                  <div className="h-20 bg-muted rounded" />
                </CardContent>
              </Card>
            ))}
          </div>
        ) : summary ? (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  Chờ xử lý
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{formatVND(summary.totalPendingAmountVnd)}</div>
                <p className="text-xs text-muted-foreground mt-1">
                  {summary.pendingCount} quyết toán
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  Sẵn sàng
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-accent-green">
                  {formatVND(summary.totalReadyAmountVnd)}
                </div>
                <p className="text-xs text-muted-foreground mt-1">
                  {summary.readyCount} quyết toán
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  Đang chi trả
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{formatVND(summary.totalInPayoutAmountVnd)}</div>
                <p className="text-xs text-muted-foreground mt-1">
                  {summary.inPayoutCount} quyết toán
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  Đã thanh toán
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-success">
                  {formatVND(summary.totalPaidAmountVnd)}
                </div>
                <p className="text-xs text-muted-foreground mt-1">
                  {summary.paidCount} quyết toán
                </p>
              </CardContent>
            </Card>
          </div>
        ) : null}

        {/* Search & Filter */}
        <Card>
          <CardContent className="pt-6">
            <div className="flex flex-col sm:flex-row gap-4">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Tìm theo mã quyết toán, mã booking..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-9"
                />
              </div>
              <Tabs value={statusFilter} onValueChange={setStatusFilter} className="w-full sm:w-auto">
                <TabsList>
                  <TabsTrigger value="all">Tất cả</TabsTrigger>
                  <TabsTrigger value="PENDING">Chờ xử lý</TabsTrigger>
                  <TabsTrigger value="READY">Sẵn sàng</TabsTrigger>
                  <TabsTrigger value="IN_PAYOUT">Đang chi trả</TabsTrigger>
                  <TabsTrigger value="PAID">Đã thanh toán</TabsTrigger>
                  <TabsTrigger value="ON_HOLD">Tạm giữ</TabsTrigger>
                </TabsList>
              </Tabs>
            </div>
          </CardContent>
        </Card>

        {/* Settlements List */}
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
        ) : filteredSettlements && filteredSettlements.length > 0 ? (
          <>
            <div className="grid gap-4">
              {filteredSettlements.map((settlement: any) => (
                <Card key={settlement.settlementId} className="hover:shadow-md transition-shadow">
                  <CardContent className="p-6">
                    <div className="flex items-start justify-between mb-4">
                      <div>
                        <div className="flex items-center gap-2 mb-1">
                          <h3 className="font-semibold text-lg">
                            Quyết toán #{settlement.settlementId}
                          </h3>
                          {getStatusBadge(settlement.status)}
                        </div>
                        <p className="text-sm text-muted-foreground">
                          Booking #{settlement.bookingId} • {formatDate(settlement.createdAt)}
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="text-sm text-muted-foreground mb-1">Thu nhập thực</p>
                        <p className="text-2xl font-bold text-accent-green">
                          {formatVND(settlement.netToTransportVnd)}
                        </p>
                        <p className="text-xs text-muted-foreground mt-1">
                          Từ {formatVND(settlement.agreedPriceVnd)}
                        </p>
                      </div>
                    </div>

                    {/* Collection Mode */}
                    <div className="flex items-center gap-4 mb-4">
                      <div className="flex items-center gap-2 text-sm">
                        <DollarSign className="h-4 w-4 text-muted-foreground" />
                        <span className="text-muted-foreground">Thu tiền:</span>
                        <span className="font-medium">
                          {settlement.collectionMode === "GATEWAY" ? "Qua cổng thanh toán" : "Tiền mặt"}
                        </span>
                      </div>
                    </div>

                    {/* Dates */}
                    <div className="flex flex-wrap gap-4 text-sm text-muted-foreground mb-4">
                      {settlement.readyAt && (
                        <div className="flex items-center gap-1">
                          <CheckCircle2 className="h-3 w-3" />
                          <span>Sẵn sàng: {formatDate(settlement.readyAt)}</span>
                        </div>
                      )}
                      {settlement.paidAt && (
                        <div className="flex items-center gap-1">
                          <CheckCircle2 className="h-3 w-3 text-success" />
                          <span>Đã thanh toán: {formatDate(settlement.paidAt)}</span>
                        </div>
                      )}
                    </div>

                    {/* Actions */}
                    <div className="flex gap-2">
                      <Button asChild variant="outline" className="flex-1">
                        <Link href={`/transport/settlements/${settlement.settlementId}`}>
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
            title="Chưa có quyết toán nào"
            description="Các quyết toán từ công việc hoàn thành sẽ hiển thị ở đây"
            action={
              <Button asChild>
                <Link href="/transport/active">Xem công việc đang thực hiện</Link>
              </Button>
            }
          />
        )}
      </div>
    </DashboardLayout>
  )
}
