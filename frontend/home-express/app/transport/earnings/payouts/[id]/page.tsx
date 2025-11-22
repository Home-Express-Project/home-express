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
  ChevronLeft,
  Zap,
  Download,
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

interface PayoutDetail {
  payoutId: number
  payoutNumber: string
  transportId: number
  totalAmountVnd: number
  itemCount: number
  status: string
  items: Array<{
    settlementId: number
    bookingId: number
    netToTransportVnd: number
  }>
  createdAt: string
  processedAt?: string
  completedAt?: string
  failureReason?: string
}

export default function PayoutDetailPage() {
  const { user, loading } = useAuth()
  const router = useRouter()
  const params = useParams()
  const payoutId = params.id as string

  const [payout, setPayout] = useState<PayoutDetail | null>(null)
  const [dataLoading, setDataLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!loading && (!user || user.role !== "TRANSPORT")) {
      router.push("/login")
    }
  }, [user, loading, router])

  useEffect(() => {
    const fetchData = async () => {
      if (!user || user.role !== "TRANSPORT" || !payoutId) return

      try {
        setDataLoading(true)
        const data = await apiClient.getPayoutDetails(parseInt(payoutId))
        setPayout(data)
      } catch (err) {
        console.error("Error fetching payout details:", err)
        setError("Không thể tải dữ liệu rút tiền. Vui lòng thử lại.")
      } finally {
        setDataLoading(false)
      }
    }

    fetchData()
  }, [user, payoutId])

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "PENDING":
        return (
          <Badge variant="secondary">
            <Clock className="w-3 h-3 mr-1" />
            Chờ xử lý
          </Badge>
        )
      case "PROCESSING":
        return (
          <Badge className="bg-amber-500">
            <Zap className="w-3 h-3 mr-1" />
            Đang xử lý
          </Badge>
        )
      case "COMPLETED":
        return (
          <Badge className="bg-accent-green">
            <CheckCircle2 className="w-3 h-3 mr-1" />
            Hoàn thành
          </Badge>
        )
      case "FAILED":
        return (
          <Badge variant="destructive">
            <AlertCircle className="w-3 h-3 mr-1" />
            Thất bại
          </Badge>
        )
      default:
        return <Badge variant="outline">{status}</Badge>
    }
  }

  const handleDownloadReceipt = () => {
    if (!payout) return

    const receiptContent = `
RỰT TIỀN - HOÁ ĐƠN
${payout.payoutNumber}

THÔNG TIN CHUNG
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Mã rút tiền: ${payout.payoutId}
Trạng thái: ${payout.status}
Tạo lúc: ${formatDate(payout.createdAt, true)}
${payout.processedAt ? `Xử lý lúc: ${formatDate(payout.processedAt, true)}\n` : ""}${payout.completedAt ? `Hoàn thành lúc: ${formatDate(payout.completedAt, true)}\n` : ""}

DANH SÁCH QUYẾT TOÁN
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
${payout.items
  .map(
    (item, idx) =>
      `${idx + 1}. Quyết toán #${item.settlementId} (Đơn #${item.bookingId}): ${formatVND(item.netToTransportVnd)}`
  )
  .join("\n")}

TỔNG CỘNG
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Số lượng quyết toán: ${payout.itemCount}
Tổng số tiền: ${formatVND(payout.totalAmountVnd)}

Được tạo lúc: ${new Date().toLocaleString("vi-VN")}
    `.trim()

    const blob = new Blob([receiptContent], { type: "text/plain;charset=utf-8;" })
    const link = document.createElement("a")
    link.href = URL.createObjectURL(blob)
    link.download = `payout_${payout.payoutNumber}_${new Date().toISOString().split("T")[0]}.txt`
    link.click()
  }

  if (loading || dataLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-accent-green" />
      </div>
    )
  }

  if (error || !payout) {
    return (
      <DashboardLayout navItems={navItems} title="Chi tiết rút tiền">
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
                  {error || "Không tìm thấy rút tiền"}
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      </DashboardLayout>
    )
  }

  return (
    <DashboardLayout navItems={navItems} title="Chi tiết rút tiền">
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
                Chi tiết Rút tiền {payout.payoutNumber}
              </h1>
              <p className="text-muted-foreground mt-1">
                {payout.itemCount} quyết toán
              </p>
            </div>
            <div className="flex items-center gap-2">
              <Button
                onClick={handleDownloadReceipt}
                variant="outline"
                className="gap-2"
              >
                <Download className="w-4 h-4" />
                Tải hoá đơn
              </Button>
              {getStatusBadge(payout.status)}
            </div>
          </div>
        </div>

        {/* Status Timeline */}
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
                  {payout.status !== "PENDING" && (
                    <div className="w-0.5 h-12 bg-gray-200 my-2" />
                  )}
                </div>
                <div className="pt-1">
                  <p className="font-medium">Yêu cầu rút tiền được tạo</p>
                  <p className="text-sm text-muted-foreground">
                    {formatDate(payout.createdAt, true)}
                  </p>
                </div>
              </div>

              {/* Processing */}
              {payout.processedAt && (
                <div className="flex items-start gap-4">
                  <div className="flex flex-col items-center">
                    <div className="w-8 h-8 rounded-full bg-amber-500 flex items-center justify-center">
                      <Zap className="w-4 h-4 text-white" />
                    </div>
                    {payout.status !== "PROCESSING" && (
                      <div className="w-0.5 h-12 bg-gray-200 my-2" />
                    )}
                  </div>
                  <div className="pt-1">
                    <p className="font-medium">Đang xử lý</p>
                    <p className="text-sm text-muted-foreground">
                      {formatDate(payout.processedAt, true)}
                    </p>
                  </div>
                </div>
              )}

              {/* Completed */}
              {payout.completedAt && (
                <div className="flex items-start gap-4">
                  <div className="flex flex-col items-center">
                    <div className="w-8 h-8 rounded-full bg-accent-green flex items-center justify-center">
                      <CheckCircle2 className="w-4 h-4 text-white" />
                    </div>
                  </div>
                  <div className="pt-1">
                    <p className="font-medium">Hoàn thành</p>
                    <p className="text-sm text-muted-foreground">
                      {formatDate(payout.completedAt, true)}
                    </p>
                  </div>
                </div>
              )}

              {/* Failed */}
              {payout.status === "FAILED" && (
                <div className="flex items-start gap-4">
                  <div className="flex flex-col items-center">
                    <div className="w-8 h-8 rounded-full bg-destructive flex items-center justify-center">
                      <AlertCircle className="w-4 h-4 text-white" />
                    </div>
                  </div>
                  <div className="pt-1">
                    <p className="font-medium text-destructive">Thất bại</p>
                    {payout.failureReason && (
                      <p className="text-sm text-destructive">
                        {payout.failureReason}
                      </p>
                    )}
                  </div>
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Summary */}
        <Card className="border-2 border-accent-green/30 bg-accent-green/5 hover:shadow-md transition-shadow">
          <CardHeader>
            <CardTitle>Thông tin rút tiền</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-6">
              <div>
                <p className="text-sm text-muted-foreground">Mã rút tiền</p>
                <p className="text-2xl font-bold mt-1">{payout.payoutNumber}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Số quyết toán</p>
                <p className="text-2xl font-bold mt-1">{payout.itemCount}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Tổng số tiền</p>
                <p className="text-2xl font-bold text-accent-green mt-1">
                  {formatVND(payout.totalAmountVnd)}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Items */}
        <Card className="hover:shadow-md transition-shadow">
          <CardHeader>
            <CardTitle>Danh sách quyết toán ({payout.items.length})</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2 max-h-96 overflow-y-auto">
              {payout.items.map((item, idx) => (
                <div
                  key={item.settlementId}
                  className="flex items-center justify-between p-3 border rounded-md hover:bg-muted/50 cursor-pointer transition-colors"
                  onClick={() =>
                    router.push(
                      `/transport/earnings/settlements/${item.settlementId}`
                    )
                  }
                >
                  <div className="flex-1 space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="font-medium text-sm">
                        {idx + 1}. QT #{item.settlementId}
                      </span>
                      <span className="text-muted-foreground text-sm">
                        Đơn #{item.bookingId}
                      </span>
                    </div>
                  </div>
                  <p className="font-semibold text-right">
                    {formatVND(item.netToTransportVnd)}
                  </p>
                </div>
              ))}
            </div>

            {/* Total */}
            <div className="border-t mt-4 pt-4 flex items-center justify-between">
              <p className="font-medium">Tổng cộng</p>
              <p className="text-2xl font-bold text-accent-green">
                {formatVND(payout.totalAmountVnd)}
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  )
}
