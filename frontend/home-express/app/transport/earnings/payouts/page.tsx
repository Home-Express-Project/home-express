"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/contexts/auth-context"
import { apiClient } from "@/lib/api-client"
import { DashboardLayout } from "@/components/dashboard/dashboard-layout"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import {
  DollarSign,
  Clock,
  CheckCircle2,
  AlertCircle,
  Search,
  ChevronRight,
  Zap,
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

interface Payout {
  payoutId: number
  payoutNumber: string
  totalAmountVnd: number
  itemCount: number
  status: string
  createdAt: string
  processedAt?: string
  completedAt?: string
}

export default function PayoutsPage() {
  const { user, loading } = useAuth()
  const router = useRouter()
  const [payouts, setPayouts] = useState<Payout[]>([])
  const [filteredPayouts, setFilteredPayouts] = useState<Payout[]>([])
  const [pendingData, setPendingData] = useState<{
    settlements: Array<{
      settlementId: number
      bookingId: number
      netToTransportVnd: number
      createdAt: string
      readyAt?: string
    }>
    count: number
    totalAmount: number
  } | null>(null)
  const [dataLoading, setDataLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState("")
  const [statusFilter, setStatusFilter] = useState<string>("all")

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
        const [payoutsData, pendingData] = await Promise.all([
          apiClient.getPayouts({ size: 100 }),
          apiClient.getPendingSettlementsForPayout(),
        ])
        setPayouts(payoutsData.payouts)
        setFilteredPayouts(payoutsData.payouts)
        setPendingData(pendingData)
      } catch (error) {
        console.error("Error fetching payouts:", error)
      } finally {
        setDataLoading(false)
      }
    }

    fetchData()
  }, [user])

  useEffect(() => {
    let filtered = [...payouts]

    if (searchTerm) {
      filtered = filtered.filter(
        (p) =>
          p.payoutNumber.includes(searchTerm) ||
          p.payoutId.toString().includes(searchTerm)
      )
    }

    if (statusFilter !== "all") {
      filtered = filtered.filter((p) => p.status === statusFilter)
    }

    setFilteredPayouts(filtered)
  }, [payouts, searchTerm, statusFilter])

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

  const statsCards = [
    {
      label: "Chờ rút tiền",
      value: pendingData?.count || 0,
      amount: pendingData?.totalAmount || 0,
      icon: Clock,
      color: "text-amber-500",
    },
    {
      label: "Tổng rút trong tháng",
      value: payouts.filter(p => {
        const d = new Date(p.createdAt)
        const now = new Date()
        return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear()
      }).length,
      amount: payouts
        .filter(p => {
          const d = new Date(p.createdAt)
          const now = new Date()
          return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear()
        })
        .reduce((sum, p) => sum + p.totalAmountVnd, 0),
      icon: Zap,
      color: "text-blue-500",
    },
  ]

  if (loading || dataLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-accent-green" />
      </div>
    )
  }

  if (!user) return null

  return (
    <DashboardLayout navItems={navItems} title="Rút tiền">
      <div className="max-w-7xl mx-auto space-y-8 py-10">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Rút tiền & Lịch sử thanh toán</h1>
            <p className="text-muted-foreground mt-1">
              Theo dõi các yêu cầu rút tiền và quá trình thanh toán
            </p>
          </div>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {statsCards.map((card) => {
            const Icon = card.icon
            return (
              <Card key={card.label} className="hover:shadow-md transition-shadow">
                <CardHeader className="flex flex-row items-center justify-between pb-2">
                  <CardTitle className="text-sm font-medium text-muted-foreground">
                    {card.label}
                  </CardTitle>
                  <Icon className={`w-5 h-5 ${card.color}`} />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{formatVND(card.amount)}</div>
                  <p className="text-sm text-muted-foreground mt-1">{card.value} giao dịch</p>
                </CardContent>
              </Card>
            )
          })}
        </div>

        {/* Pending Settlements Alert */}
        {pendingData && pendingData.count > 0 && (
          <Card className="border-blue-200 bg-blue-50">
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <AlertCircle className="w-5 h-5 text-blue-500" />
                  <div>
                    <p className="font-medium text-blue-900">
                      Có {pendingData.count} quyết toán sẵn sàng rút tiền
                    </p>
                    <p className="text-sm text-blue-700">
                      Tổng cộng: {formatVND(pendingData.totalAmount)}
                    </p>
                  </div>
                </div>
                <Button
                  onClick={() => router.push("/transport/earnings/settlements")}
                  className="bg-blue-500 hover:bg-blue-600"
                >
                  Xem chi tiết
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Payouts List */}
        <Card className="hover:shadow-md transition-shadow">
          <CardHeader>
            <CardTitle>Lịch sử rút tiền</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {/* Filters */}
            <div className="flex flex-col md:flex-row gap-4">
              <div className="flex-1 relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <Input
                  placeholder="Tìm kiếm mã rút tiền..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger className="w-full md:w-[200px]">
                  <SelectValue placeholder="Trạng thái" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">Tất cả trạng thái</SelectItem>
                  <SelectItem value="PENDING">Chờ xử lý</SelectItem>
                  <SelectItem value="PROCESSING">Đang xử lý</SelectItem>
                  <SelectItem value="COMPLETED">Hoàn thành</SelectItem>
                  <SelectItem value="FAILED">Thất bại</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Payouts */}
            <div className="space-y-3">
              {filteredPayouts.length === 0 ? (
                <div className="text-center py-12">
                  <AlertCircle className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">Không tìm thấy yêu cầu rút tiền nào</p>
                </div>
              ) : (
                filteredPayouts.map((payout) => (
                  <Card
                    key={payout.payoutId}
                    className="border-2 cursor-pointer hover:border-accent-green transition-colors"
                    onClick={() =>
                      router.push(`/transport/earnings/payouts/${payout.payoutId}`)
                    }
                  >
                    <CardContent className="p-4">
                      <div className="flex items-start justify-between gap-4">
                        <div className="flex-1 space-y-2">
                          <div className="flex items-center gap-3">
                            <span className="font-mono text-sm text-muted-foreground">
                              {payout.payoutNumber}
                            </span>
                            {getStatusBadge(payout.status)}
                          </div>
                          <div className="flex items-center gap-4 text-sm text-muted-foreground">
                            <div>
                              <span className="font-medium">{payout.itemCount}</span> quyết toán
                            </div>
                            <div>Tạo: {formatDate(payout.createdAt, true)}</div>
                            {payout.completedAt && (
                              <div>Hoàn thành: {formatDate(payout.completedAt, true)}</div>
                            )}
                          </div>
                        </div>
                        <div className="text-right space-y-1 flex items-end gap-4">
                          <div>
                            <p className="text-xs text-muted-foreground mb-1">Tổng số tiền</p>
                            <p className="text-2xl font-bold text-accent-green">
                              {formatVND(payout.totalAmountVnd)}
                            </p>
                          </div>
                          <ChevronRight className="w-5 h-5 text-muted-foreground" />
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))
              )}
            </div>

            {/* Pagination Info */}
            {filteredPayouts.length > 0 && (
              <div className="flex items-center justify-between pt-4 border-t">
                <p className="text-sm text-muted-foreground">
                  Hiển thị {filteredPayouts.length} / {payouts.length} rút tiền
                </p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  )
}
