"use client"

import { useEffect, useState } from "react"
import { useRouter, useParams } from "next/navigation"
import { useAuth } from "@/contexts/auth-context"
import { apiClient } from "@/lib/api-client"
import { DashboardLayout } from "@/components/dashboard/dashboard-layout"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import {
  DollarSign,
  Clock,
  CheckCircle2,
  AlertCircle,
  Pause,
  ChevronLeft,
  FileText,
} from "lucide-react"
import { formatVND, formatDate } from "@/lib/format"

const navItems = [
  { label: "Tổng quan", href: "/transport", icon: "LayoutDashboard" },
  { label: "Công việc", href: "/transport/jobs", icon: "Package" },
  { label: "Báo giá", href: "/transport/quotations", icon: "FileText" },
  { label: "Xe", href: "/transport/vehicles", icon: "Truck" },
  { label: "Thu nhập", href: "/transport/earnings", icon: "DollarSign" },
  { label: "Hồ sơ", href: "/transport/profile", icon: "Star" },
]

interface SettlementDetail {
  settlementId: number
  bookingId: number
  transportId: number
  agreedPriceVnd: number
  totalCollectedVnd: number
  gatewayFeeVnd: number
  commissionRateBps: number
  platformFeeVnd: number
  adjustmentVnd: number
  netToTransportVnd: number
  collectionMode: string
  status: string
  onHoldReason?: string
  createdAt: string
  readyAt?: string
  paidAt?: string
  updatedAt: string
  notes?: string
}

export default function SettlementDetailPage() {
  const { user, loading } = useAuth()
  const router = useRouter()
  const params = useParams()
  const settlementId = params.id as string

  const [settlement, setSettlement] = useState<SettlementDetail | null>(null)
  const [dataLoading, setDataLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!loading && (!user || user.role !== "TRANSPORT")) {
      router.push("/login")
    }
  }, [user, loading, router])

  useEffect(() => {
    const fetchData = async () => {
      if (!user || user.role !== "TRANSPORT" || !settlementId) return

      try {
        setDataLoading(true)
        const data = await apiClient.getSettlementDetails(parseInt(settlementId))
        setSettlement(data)
      } catch (err) {
        console.error("Error fetching settlement details:", err)
        setError("Không thể tải dữ liệu quyết toán. Vui lòng thử lại.")
      } finally {
        setDataLoading(false)
      }
    }

    fetchData()
  }, [user, settlementId])

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "PENDING":
        return (
          <Badge variant="secondary">
            <Clock className="w-3 h-3 mr-1" />
            Chờ xử lý
          </Badge>
        )
      case "READY":
        return (
          <Badge className="bg-blue-500">
            <CheckCircle2 className="w-3 h-3 mr-1" />
            Sẵn sàng
          </Badge>
        )
      case "IN_PAYOUT":
        return (
          <Badge className="bg-amber-500">
            <AlertCircle className="w-3 h-3 mr-1" />
            Đang rút tiền
          </Badge>
        )
      case "PAID":
        return (
          <Badge className="bg-accent-green">
            <CheckCircle2 className="w-3 h-3 mr-1" />
            Đã thanh toán
          </Badge>
        )
      case "ON_HOLD":
        return (
          <Badge variant="destructive">
            <Pause className="w-3 h-3 mr-1" />
            Tạm giữ
          </Badge>
        )
      case "DISPUTED":
        return (
          <Badge variant="destructive">
            <AlertCircle className="w-3 h-3 mr-1" />
            Tranh chấp
          </Badge>
        )
      default:
        return <Badge variant="outline">{status}</Badge>
    }
  }

  const getCollectionModeLabel = (mode: string) => {
    switch (mode) {
      case "ALL_ONLINE":
        return "Thanh toán 100% online"
      case "CASH_ON_DELIVERY":
        return "Cọc + thu tiền mặt"
      case "PARTIAL_ONLINE":
        return "Thanh toán không hoàn toàn"
      case "ALL_CASH":
        return "Thanh toán 100% tiền mặt"
      case "MIXED":
        return "Thanh toán hỗn hợp"
      default:
        return mode
    }
  }

  if (loading || dataLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-accent-green" />
      </div>
    )
  }

  if (error || !settlement) {
    return (
      <DashboardLayout navItems={navItems} title="Chi tiết quyết toán">
        <div className="max-w-4xl mx-auto py-10">
          <Button
            variant="ghost"
            className="mb-6 gap-2"
            onClick={() => router.back()}
          >
            <ChevronLeft className="w-4 h-4" />
            Quay lại
          </Button>

          <Card className="border-destructive/20 bg-destructive/5">
            <CardContent className="p-6">
              <div className="flex items-center gap-3">
                <AlertCircle className="w-6 h-6 text-destructive" />
                <p className="text-destructive font-medium">
                  {error || "Không tìm thấy quyết toán"}
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      </DashboardLayout>
    )
  }

  const commissionVnd = Math.round(
    (settlement.agreedPriceVnd * settlement.commissionRateBps) / 10000
  )

  return (
    <DashboardLayout navItems={navItems} title="Chi tiết quyết toán">
      <div className="max-w-4xl mx-auto space-y-8 py-10">
        {/* Header */}
        <div>
          <Button
            variant="ghost"
            className="mb-6 gap-2"
            onClick={() => router.back()}
          >
            <ChevronLeft className="w-4 h-4" />
            Quay lại
          </Button>

          <div className="flex items-start justify-between">
            <div>
              <h1 className="text-3xl font-bold tracking-tight">
                Chi tiết Quyết toán QT #{settlement.settlementId}
              </h1>
              <p className="text-muted-foreground mt-1">
                Đơn hàng #{settlement.bookingId}
              </p>
            </div>
            <div>{getStatusBadge(settlement.status)}</div>
          </div>
        </div>

        {/* Status Timeline */}
        {settlement.status !== "PENDING" && (
          <Card className="hover:shadow-md transition-shadow">
            <CardHeader>
              <CardTitle>Quá trình xử lý</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {/* Created */}
                <div className="flex items-start gap-4">
                  <div className="flex flex-col items-center">
                    <div className="w-8 h-8 rounded-full bg-accent-green flex items-center justify-center">
                      <CheckCircle2 className="w-4 h-4 text-white" />
                    </div>
                    <div className="w-0.5 h-12 bg-gray-200 my-2" />
                  </div>
                  <div className="pt-1">
                    <p className="font-medium">Đã tạo quyết toán</p>
                    <p className="text-sm text-muted-foreground">
                      {formatDate(settlement.createdAt, true)}
                    </p>
                  </div>
                </div>

                {/* Ready */}
                {settlement.readyAt && (
                  <div className="flex items-start gap-4">
                    <div className="flex flex-col items-center">
                      <div className="w-8 h-8 rounded-full bg-blue-500 flex items-center justify-center">
                        <CheckCircle2 className="w-4 h-4 text-white" />
                      </div>
                      {settlement.status !== "READY" && (
                        <div className="w-0.5 h-12 bg-gray-200 my-2" />
                      )}
                    </div>
                    <div className="pt-1">
                      <p className="font-medium">Sẵn sàng rút tiền</p>
                      <p className="text-sm text-muted-foreground">
                        {formatDate(settlement.readyAt, true)}
                      </p>
                    </div>
                  </div>
                )}

                {/* Paid */}
                {settlement.paidAt && (
                  <div className="flex items-start gap-4">
                    <div className="flex flex-col items-center">
                      <div className="w-8 h-8 rounded-full bg-accent-green flex items-center justify-center">
                        <CheckCircle2 className="w-4 h-4 text-white" />
                      </div>
                    </div>
                    <div className="pt-1">
                      <p className="font-medium">Đã thanh toán</p>
                      <p className="text-sm text-muted-foreground">
                        {formatDate(settlement.paidAt, true)}
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        )}

        {/* On Hold / Disputed Info */}
        {settlement.status === "ON_HOLD" && settlement.onHoldReason && (
          <Card className="border-destructive/20 bg-destructive/5">
            <CardHeader>
              <CardTitle className="text-destructive">Lý do tạm giữ</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm">{settlement.onHoldReason}</p>
            </CardContent>
          </Card>
        )}

        {settlement.status === "DISPUTED" && settlement.notes && (
          <Card className="border-destructive/20 bg-destructive/5">
            <CardHeader>
              <CardTitle className="text-destructive">Ghi chú tranh chấp</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm">{settlement.notes}</p>
            </CardContent>
          </Card>
        )}

        {/* Main Details */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Payment Info */}
          <Card className="hover:shadow-md transition-shadow">
            <CardHeader>
              <CardTitle className="text-base">Thông tin thanh toán</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-muted-foreground text-sm">Loại thanh toán</span>
                  <span className="font-medium text-sm">
                    {getCollectionModeLabel(settlement.collectionMode)}
                  </span>
                </div>

                <div className="border-t pt-3">
                  <div className="flex items-center justify-between">
                    <span className="text-muted-foreground text-sm">Giá đồng ý</span>
                    <span className="font-medium">
                      {formatVND(settlement.agreedPriceVnd)}
                    </span>
                  </div>
                </div>

                <div className="flex items-center justify-between">
                  <span className="text-muted-foreground text-sm">Tổng thu được</span>
                  <span className="font-medium">
                    {formatVND(settlement.totalCollectedVnd)}
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Fee Breakdown */}
          <Card className="hover:shadow-md transition-shadow">
            <CardHeader>
              <CardTitle className="text-base">Chi phí & Điều chỉnh</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">Phí cổng thanh toán</span>
                <span className="font-medium">
                  -{formatVND(settlement.gatewayFeeVnd)}
                </span>
              </div>

              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">
                  Hoa hồng nền tảng ({(settlement.commissionRateBps / 100).toFixed(2)}%)
                </span>
                <span className="font-medium">
                  -{formatVND(commissionVnd)}
                </span>
              </div>

              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">Phí nền tảng</span>
                <span className="font-medium">
                  -{formatVND(settlement.platformFeeVnd)}
                </span>
              </div>

              {settlement.adjustmentVnd !== 0 && (
                <div className="flex items-center justify-between text-sm border-t pt-3">
                  <span className="text-muted-foreground">Điều chỉnh</span>
                  <span className="font-medium">
                    {settlement.adjustmentVnd > 0 ? "+" : ""}
                    {formatVND(settlement.adjustmentVnd)}
                  </span>
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Net Amount */}
        <Card className="border-2 border-accent-green/30 bg-accent-green/5 hover:shadow-md transition-shadow">
          <CardHeader>
            <CardTitle>Thu nhập ròng</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-baseline gap-2">
              <span className="text-4xl font-bold text-accent-green">
                {formatVND(settlement.netToTransportVnd)}
              </span>
              <span className="text-muted-foreground">
                ({((settlement.netToTransportVnd / settlement.agreedPriceVnd) * 100).toFixed(1)}% của giá đồng ý)
              </span>
            </div>
          </CardContent>
        </Card>

        {/* Notes */}
        {settlement.notes && settlement.status !== "DISPUTED" && (
          <Card>
            <CardHeader>
              <CardTitle className="text-base flex items-center gap-2">
                <FileText className="w-4 h-4" />
                Ghi chú
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm whitespace-pre-wrap">{settlement.notes}</p>
            </CardContent>
          </Card>
        )}

        {/* Meta Info */}
        <Card className="bg-muted/50">
          <CardContent className="p-4">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
              <div>
                <p className="text-muted-foreground">Mã quyết toán</p>
                <p className="font-mono font-medium text-xs mt-1">
                  {settlement.settlementId}
                </p>
              </div>
              <div>
                <p className="text-muted-foreground">Mã đơn hàng</p>
                <p className="font-mono font-medium text-xs mt-1">
                  {settlement.bookingId}
                </p>
              </div>
              <div>
                <p className="text-muted-foreground">Tạo lúc</p>
                <p className="text-xs mt-1">
                  {formatDate(settlement.createdAt, true)}
                </p>
              </div>
              <div>
                <p className="text-muted-foreground">Cập nhật lúc</p>
                <p className="text-xs mt-1">
                  {formatDate(settlement.updatedAt, true)}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  )
}
