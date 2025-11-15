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
  Pause,
  Search,
  ChevronRight,
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

interface Settlement {
  settlementId: number
  bookingId: number
  agreedPriceVnd: number
  netToTransportVnd: number
  collectionMode: string
  status: string
  createdAt: string
  readyAt?: string
  paidAt?: string
}

interface SettlementSummary {
  pendingCount: number
  totalPendingAmountVnd: number
  readyCount: number
  totalReadyAmountVnd: number
  inPayoutCount: number
  totalInPayoutAmountVnd: number
  paidCount: number
  totalPaidAmountVnd: number
  onHoldCount: number
  totalOnHoldAmountVnd: number
}

export default function SettlementsPage() {
  const { user, loading } = useAuth()
  const router = useRouter()
  const [settlements, setSettlements] = useState<Settlement[]>([])
  const [filteredSettlements, setFilteredSettlements] = useState<Settlement[]>([])
  const [summary, setSummary] = useState<SettlementSummary | null>(null)
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
        const [settlementsData, summaryData] = await Promise.all([
          apiClient.getSettlements({ size: 100 }),
          apiClient.getSettlementSummary(),
        ])
        setSettlements(settlementsData.settlements)
        setFilteredSettlements(settlementsData.settlements)
        setSummary(summaryData)
      } catch (error) {
        console.error("Error fetching settlements:", error)
      } finally {
        setDataLoading(false)
      }
    }

    fetchData()
  }, [user])

  useEffect(() => {
    let filtered = [...settlements]

    if (searchTerm) {
      filtered = filtered.filter(
        (s) =>
          s.bookingId.toString().includes(searchTerm) ||
          s.settlementId.toString().includes(searchTerm)
      )
    }

    if (statusFilter !== "all") {
      filtered = filtered.filter((s) => s.status === statusFilter)
    }

    setFilteredSettlements(filtered)
  }, [settlements, searchTerm, statusFilter])

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

  if (loading || dataLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-accent-green" />
      </div>
    )
  }

  if (!user || !summary) return null

  return (
    <DashboardLayout navItems={navItems} title="Chi tiết quyết toán">
      <div className="max-w-7xl mx-auto space-y-8 py-10">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Chi tiết Quyết toán</h1>
            <p className="text-muted-foreground mt-1">
              Theo dõi trạng thái quyết toán từng đơn hàng
            </p>
          </div>
        </div>

        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-4">
          <Card className="hover:shadow-md transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                Chờ xử lý
              </CardTitle>
              <Clock className="w-5 h-5 text-amber-500" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{summary.pendingCount}</div>
              <p className="text-sm text-muted-foreground mt-1">
                {formatVND(summary.totalPendingAmountVnd)}
              </p>
            </CardContent>
          </Card>

          <Card className="hover:shadow-md transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                Sẵn sàng
              </CardTitle>
              <CheckCircle2 className="w-5 h-5 text-blue-500" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{summary.readyCount}</div>
              <p className="text-sm text-muted-foreground mt-1">
                {formatVND(summary.totalReadyAmountVnd)}
              </p>
            </CardContent>
          </Card>

          <Card className="hover:shadow-md transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                Đang rút tiền
              </CardTitle>
              <AlertCircle className="w-5 h-5 text-amber-500" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{summary.inPayoutCount}</div>
              <p className="text-sm text-muted-foreground mt-1">
                {formatVND(summary.totalInPayoutAmountVnd)}
              </p>
            </CardContent>
          </Card>

          <Card className="hover:shadow-md transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                Đã thanh toán
              </CardTitle>
              <CheckCircle2 className="w-5 h-5 text-accent-green" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{summary.paidCount}</div>
              <p className="text-sm text-muted-foreground mt-1">
                {formatVND(summary.totalPaidAmountVnd)}
              </p>
            </CardContent>
          </Card>

          <Card className="hover:shadow-md transition-shadow border-destructive/20">
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-destructive">
                Tạm giữ
              </CardTitle>
              <Pause className="w-5 h-5 text-destructive" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-destructive">{summary.onHoldCount}</div>
              <p className="text-sm text-muted-foreground mt-1">
                {formatVND(summary.totalOnHoldAmountVnd)}
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Settlements List */}
        <Card className="hover:shadow-md transition-shadow">
          <CardHeader>
            <CardTitle>Danh sách quyết toán</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {/* Filters */}
            <div className="flex flex-col md:flex-row gap-4">
              <div className="flex-1 relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <Input
                  placeholder="Tìm kiếm mã đơn hoặc quyết toán..."
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
                  <SelectItem value="READY">Sẵn sàng</SelectItem>
                  <SelectItem value="IN_PAYOUT">Đang rút tiền</SelectItem>
                  <SelectItem value="PAID">Đã thanh toán</SelectItem>
                  <SelectItem value="ON_HOLD">Tạm giữ</SelectItem>
                  <SelectItem value="DISPUTED">Tranh chấp</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Settlements */}
            <div className="space-y-3">
              {filteredSettlements.length === 0 ? (
                <div className="text-center py-12">
                  <AlertCircle className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">Không tìm thấy quyết toán nào</p>
                </div>
              ) : (
                filteredSettlements.map((settlement) => (
                  <Card
                    key={settlement.settlementId}
                    className="border-2 cursor-pointer hover:border-accent-green transition-colors"
                    onClick={() => 
                      router.push(`/transport/earnings/settlements/${settlement.settlementId}`)
                    }
                  >
                    <CardContent className="p-4">
                      <div className="flex items-start justify-between gap-4">
                        <div className="flex-1 space-y-2">
                          <div className="flex items-center gap-3">
                            <span className="font-mono text-sm text-muted-foreground">
                              QT #{settlement.settlementId}
                            </span>
                            {getStatusBadge(settlement.status)}
                          </div>
                          <div className="flex items-center gap-2">
                            <span className="font-semibold">Đơn hàng #{settlement.bookingId}</span>
                            <span className="text-muted-foreground">•</span>
                            <span className="text-sm text-muted-foreground">
                              {settlement.collectionMode === "ALL_ONLINE" && "Thanh toán 100% online"}
                              {settlement.collectionMode === "CASH_ON_DELIVERY" && "Cọc + thu tiền mặt"}
                              {settlement.collectionMode === "PARTIAL_ONLINE" && "Thanh toán không hoàn toàn"}
                              {settlement.collectionMode === "ALL_CASH" && "Thanh toán 100% tiền mặt"}
                              {settlement.collectionMode === "MIXED" && "Thanh toán hỗn hợp"}
                            </span>
                          </div>
                          <div className="flex items-center gap-4 text-sm text-muted-foreground">
                            <div>Tạo: {formatDate(settlement.createdAt, true)}</div>
                            {settlement.paidAt && (
                              <div>Thanh toán: {formatDate(settlement.paidAt, true)}</div>
                            )}
                          </div>
                        </div>
                        <div className="text-right space-y-1 flex items-end gap-4">
                          <div>
                            <p className="text-xs text-muted-foreground mb-1">Giá đồng ý</p>
                            <p className="text-lg font-semibold">
                              {formatVND(settlement.agreedPriceVnd)}
                            </p>
                          </div>
                          <div>
                            <p className="text-xs text-muted-foreground mb-1">Thu nhập ròng</p>
                            <p className="text-lg font-bold text-accent-green">
                              {formatVND(settlement.netToTransportVnd)}
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
            {filteredSettlements.length > 0 && (
              <div className="flex items-center justify-between pt-4 border-t">
                <p className="text-sm text-muted-foreground">
                  Hiển thị {filteredSettlements.length} / {settlements.length} quyết toán
                </p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  )
}
