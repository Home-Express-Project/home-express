/**
 * Transport Dashboard Home Page
 *
 * Displays transport company statistics, revenue chart, and pending quotations
 */

"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/contexts/auth-context"
import { apiClient } from "@/lib/api-client"
import { DashboardLayout } from "@/components/dashboard/dashboard-layout"
import { StatCard } from "@/components/dashboard/stat-card"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { LayoutDashboard, Truck, Package, Star, TrendingUp, DollarSign, Clock, FileText, AlertTriangle } from "lucide-react"
import { formatVND, formatDate } from "@/lib/format"
import type { TransportQuotationSummary, TransportStats } from "@/types"
import Link from "next/link"
import { useReadyToQuoteStatus } from "@/hooks/use-vehicles"

const navItems = [
  { label: "Tổng quan", href: "/transport", icon: "LayoutDashboard" },
  { label: "Công việc", href: "/transport/jobs", icon: "Package" },
  { label: "Báo giá", href: "/transport/quotations", icon: "FileText" },
  { label: "Xe", href: "/transport/vehicles", icon: "Truck" },
  { label: "Giá cả", href: "/transport/pricing/categories", icon: "DollarSign" },
  { label: "Hồ sơ", href: "/transport/profile", icon: "Star" },
]

export default function TransportDashboard() {
  const { user, loading } = useAuth()
  const router = useRouter()
  const [stats, setStats] = useState<TransportStats | null>(null)
  const [quotations, setQuotations] = useState<TransportQuotationSummary[]>([])
  const [dataLoading, setDataLoading] = useState(true)
  const { status: readyStatus, isLoading: readyStatusLoading } = useReadyToQuoteStatus()

  useEffect(() => {
    if (!loading && (!user || user.role !== "TRANSPORT")) {
      router.push("/login")
    }
  }, [user, loading, router])

  useEffect(() => {
    const fetchData = async () => {
      if (!user || user.role !== "TRANSPORT") return

      try {
        setDataLoading(true)
        const [statsData, quotationsData] = await Promise.all([
          apiClient.getTransportStats(),
          apiClient.getTransportQuotations(),
        ])

        setStats(statsData)
        setQuotations(quotationsData)
      } catch (error) {
        // TODO: surface error to a toast or error boundary if needed
      } finally {
        setDataLoading(false)
      }
    }

    fetchData()
  }, [user])

  if (loading || dataLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-accent-green" />
      </div>
    )
  }

  if (!user || !stats) return null

  return (
    <DashboardLayout navItems={navItems} title="Dashboard Vận chuyển">
      <div className="space-y-8">
        {/* Header */}
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Tổng quan</h1>
          <p className="text-muted-foreground mt-1">Theo dõi doanh thu và hiệu suất kinh doanh của bạn</p>
        </div>

        {readyStatus && (
          <Alert
            variant={readyStatus.ready_to_quote ? "default" : "warning"}
            className={`border-l-4 mt-4 ${readyStatus.ready_to_quote ? "border-l-emerald-500" : "border-l-amber-500"}`}
          >
            <AlertTriangle className="h-5 w-5" />
            <AlertDescription className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
              <div>
                <p className="font-semibold">
                  {readyStatus.ready_to_quote
                    ? "Bảng giá của bạn đang hoạt động."
                    : "Bạn chưa sẵn sàng để nhận công việc."}
                </p>
                <p className="text-sm">
                  {readyStatus.reason ||
                    (readyStatus.ready_to_quote
                      ? "Bạn có ít nhất một bảng giá còn hiệu lực. Hệ thống có thể dùng để gợi ý giá báo."
                      : "Vui lòng cấu hình ít nhất một bảng giá còn hiệu lực để mở khoá danh sách công việc.")}
                </p>
                <div className="mt-1 text-xs text-muted-foreground space-x-2">
                  <span>Bảng giá hiện tại: {readyStatus.rate_cards_count}</span>
                  <span>| Hết hạn: {readyStatus.expired_cards_count}</span>
                  {readyStatus.next_expiry_at && (
                    <span>
                      | Bảng giá sắp hết hạn: {" "}
                      {new Date(readyStatus.next_expiry_at).toLocaleString("vi-VN")}
                    </span>
                  )}
                </div>
              </div>
              <Button
                size="sm"
                variant={readyStatus.ready_to_quote ? "outline" : "default"}
                asChild
                className="mt-2 md:mt-0"
              >
                <Link href="/transport/pricing/categories">
                  {readyStatus.ready_to_quote ? "Điều chỉnh bảng giá" : "Cài đặt bảng giá ngay"}
                </Link>
              </Button>
            </AlertDescription>
          </Alert>
        )}

        {/* Stats Cards - 5 cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
          <StatCard
            title="Tổng thu nhập"
            value={formatVND(stats.total_income)}
            icon={<DollarSign className="w-6 h-6" />}
            trend="+15%"
            variant="success"
          />
          <StatCard
            title="Đơn hoàn thành"
            value={stats.completed_bookings}
            icon={<Package className="w-6 h-6" />}
            variant="default"
          />
          <StatCard
            title="Đang vận chuyển"
            value={stats.in_progress_bookings}
            icon={<Truck className="w-6 h-6" />}
            variant="warning"
          />
          <StatCard
            title="Đánh giá"
            value={`${stats.average_rating}/5`}
            icon={<Star className="w-6 h-6" />}
            variant="success"
          />
          <StatCard
            title="Tỷ lệ hoàn thành"
            value={`${stats.completion_rate}%`}
            icon={<TrendingUp className="w-6 h-6" />}
            variant="success"
          />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Revenue Chart */}
          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle>Doanh thu theo tháng</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {stats.monthly_revenue.map((item) => (
                  <div key={item.month} className="flex items-center gap-4">
                    <div className="w-12 text-sm font-medium text-muted-foreground">{item.month}</div>
                    <div className="flex-1">
                      <div className="h-8 bg-muted rounded-lg overflow-hidden">
                        <div
                          className="h-full bg-accent-green transition-all duration-300"
                          style={{
                            width: `${(item.revenue / Math.max(...stats.monthly_revenue.map((r) => r.revenue))) * 100}%`,
                          }}
                        />
                      </div>
                    </div>
                    <div className="w-32 text-sm font-semibold text-right">{formatVND(item.revenue)}</div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* Quick Stats */}
          <Card>
            <CardHeader>
              <CardTitle>Thống kê nhanh</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="p-4 bg-muted/50 rounded-lg">
                <p className="text-sm text-muted-foreground mb-1">Báo giá chờ xử lý</p>
                <p className="text-2xl font-bold">{stats.pending_quotations}</p>
              </div>
              <div className="p-4 bg-muted/50 rounded-lg">
                <p className="text-sm text-muted-foreground mb-1">Tổng đơn hàng</p>
                <p className="text-2xl font-bold">{stats.completed_bookings + stats.in_progress_bookings}</p>
              </div>
              <div className="p-4 bg-muted/50 rounded-lg">
                <p className="text-sm text-muted-foreground mb-1">Doanh thu trung bình</p>
                <p className="text-2xl font-bold">
                  {formatVND(
                    stats.completed_bookings > 0
                      ? Math.round(stats.total_income / stats.completed_bookings)
                      : 0,
                  )}
                </p>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Pending Quotations */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>Báo giá gần đây ({quotations.length})</CardTitle>
              <Button variant="ghost" size="sm" asChild>
                <Link href="/transport/quotations">Xem tất cả</Link>
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {quotations.map((quotation) => (
                <Card key={quotation.quotation_id} className="border-2">
                  <CardContent className="p-4">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex-1 space-y-2">
                        <div className="flex items-center gap-2">
                          <span className="font-mono text-sm text-muted-foreground">#{quotation.booking_id}</span>
                          <Badge variant="secondary">{quotation.status}</Badge>
                        </div>
                        <div className="text-sm text-muted-foreground space-y-1">
                          <p>
                            <span className="font-medium">Từ:</span> {quotation.pickup_location}
                          </p>
                          <p>
                            <span className="font-medium">Đến:</span> {quotation.delivery_location}
                          </p>
                          <p className="flex items-center gap-1">
                            <Clock className="h-3 w-3" />
                            {formatDate(quotation.preferred_date)}
                          </p>
                          <p className="text-xs text-muted-foreground">
                            Hạng của bạn: #{quotation.my_rank} trong {quotation.competitor_quotes_count + 1} báo giá
                          </p>
                        </div>
                      </div>
                      <div className="text-right space-y-2">
                        <p className="text-sm text-muted-foreground">Giá bạn đã báo</p>
                        <p className="text-xl font-bold text-accent-green">
                          {formatVND(quotation.my_quote_price)}
                        </p>
                        {quotation.lowest_competitor_price !== null && (
                          <p className="text-xs text-muted-foreground">
                            Thấp nhất: {formatVND(quotation.lowest_competitor_price)}
                          </p>
                        )}
                        <Button size="sm" className="bg-accent-green hover:bg-accent-green-dark" asChild>
                          <Link href="/transport/quotations">Xem chi tiết</Link>
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  )
}

