"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/contexts/auth-context"
import { apiClient } from "@/lib/api-client"
import { DashboardLayout } from "@/components/dashboard/dashboard-layout"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import {
  AlertCircle,
  Clock,
  CheckCircle2,
  Search,
  ChevronRight,
  Lock,
  AlertTriangle,
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

interface Dispute {
  disputeId: number
  bookingId: number
  settlingId?: number
  title: string
  description: string
  status: string
  walletFrozenUntil?: string
  createdAt: string
  updatedAt: string
  messageCount: number
}

export default function DisputesPage() {
  const { user, loading } = useAuth()
  const router = useRouter()
  const [disputes, setDisputes] = useState<Dispute[]>([])
  const [filteredDisputes, setFilteredDisputes] = useState<Dispute[]>([])
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
        const data = await apiClient.getTransportDisputes({ size: 100 })
        setDisputes(data.disputes)
        setFilteredDisputes(data.disputes)
      } catch (error) {
        console.error("Error fetching disputes:", error)
      } finally {
        setDataLoading(false)
      }
    }

    fetchData()
  }, [user])

  useEffect(() => {
    let filtered = [...disputes]

    if (searchTerm) {
      filtered = filtered.filter(
        (d) =>
          d.bookingId.toString().includes(searchTerm) ||
          d.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
          d.disputeId.toString().includes(searchTerm)
      )
    }

    if (statusFilter !== "all") {
      filtered = filtered.filter((d) => d.status === statusFilter)
    }

    setFilteredDisputes(filtered)
  }, [disputes, searchTerm, statusFilter])

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "OPEN":
        return (
          <Badge variant="destructive">
            <AlertCircle className="w-3 h-3 mr-1" />
            Đang mở
          </Badge>
        )
      case "PENDING_REVIEW":
        return (
          <Badge className="bg-amber-500">
            <Clock className="w-3 h-3 mr-1" />
            Chờ xem xét
          </Badge>
        )
      case "IN_PROGRESS":
        return (
          <Badge className="bg-blue-500">
            <AlertCircle className="w-3 h-3 mr-1" />
            Đang xử lý
          </Badge>
        )
      case "RESOLVED":
        return (
          <Badge className="bg-accent-green">
            <CheckCircle2 className="w-3 h-3 mr-1" />
            Đã giải quyết
          </Badge>
        )
      case "CLOSED":
        return (
          <Badge variant="outline">
            <CheckCircle2 className="w-3 h-3 mr-1" />
            Đã đóng
          </Badge>
        )
      default:
        return <Badge variant="outline">{status}</Badge>
    }
  }

  const frozenDisputes = disputes.filter((d) => d.walletFrozenUntil)
  const totalFrozenAmount = 0 // Would need settlement data to calculate

  if (loading || dataLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-accent-green" />
      </div>
    )
  }

  if (!user) return null

  return (
    <DashboardLayout navItems={navItems} title="Tranh chấp">
      <div className="max-w-7xl mx-auto space-y-8 py-10">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Tranh chấp</h1>
            <p className="text-muted-foreground mt-1">
              Quản lý các tranh chấp và yêu cầu khiếu nại
            </p>
          </div>
        </div>

        {/* Warning for frozen wallets */}
        {frozenDisputes.length > 0 && (
          <Card className="border-destructive/50 bg-destructive/5">
            <CardContent className="pt-6">
              <div className="flex items-center gap-3">
                <Lock className="w-6 h-6 text-destructive flex-shrink-0" />
                <div className="flex-1">
                  <p className="font-semibold text-destructive">Ví bị khóa</p>
                  <p className="text-sm text-muted-foreground mt-1">
                    Bạn có {frozenDisputes.length} tranh chấp với ví bị khóa. Tiền sẽ được khóa
                    cho đến khi tranh chấp được giải quyết.
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <Card className="hover:shadow-md transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                Tổng tranh chấp
              </CardTitle>
              <AlertTriangle className="w-5 h-5 text-amber-500" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{disputes.length}</div>
              <p className="text-sm text-muted-foreground mt-1">
                {frozenDisputes.length} có ví bị khóa
              </p>
            </CardContent>
          </Card>

          <Card className="hover:shadow-md transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                Đang mở
              </CardTitle>
              <AlertCircle className="w-5 h-5 text-red-500" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {disputes.filter((d) => ["OPEN", "IN_PROGRESS"].includes(d.status)).length}
              </div>
            </CardContent>
          </Card>

          <Card className="hover:shadow-md transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                Đã giải quyết
              </CardTitle>
              <CheckCircle2 className="w-5 h-5 text-accent-green" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {disputes.filter((d) => ["RESOLVED", "CLOSED"].includes(d.status)).length}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Disputes List */}
        <Card className="hover:shadow-md transition-shadow">
          <CardHeader>
            <CardTitle>Danh sách tranh chấp</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {/* Filters */}
            <div className="flex flex-col md:flex-row gap-4">
              <div className="flex-1 relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <Input
                  placeholder="Tìm kiếm mã đơn, tiêu đề..."
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
                  <SelectItem value="all">Tất cả</SelectItem>
                  <SelectItem value="OPEN">Đang mở</SelectItem>
                  <SelectItem value="PENDING_REVIEW">Chờ xem xét</SelectItem>
                  <SelectItem value="IN_PROGRESS">Đang xử lý</SelectItem>
                  <SelectItem value="RESOLVED">Đã giải quyết</SelectItem>
                  <SelectItem value="CLOSED">Đã đóng</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Disputes */}
            <div className="space-y-3">
              {filteredDisputes.length === 0 ? (
                <div className="text-center py-12">
                  <AlertCircle className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">Không tìm thấy tranh chấp nào</p>
                </div>
              ) : (
                filteredDisputes.map((dispute) => (
                  <Card
                    key={dispute.disputeId}
                    className="border-2 cursor-pointer hover:border-accent-green transition-colors"
                    onClick={() =>
                      router.push(`/transport/earnings/disputes/${dispute.disputeId}`)
                    }
                  >
                    <CardContent className="p-4">
                      <div className="flex items-start justify-between gap-4">
                        <div className="flex-1 space-y-2">
                          <div className="flex items-center gap-3">
                            <span className="font-mono text-sm text-muted-foreground">
                              TC #{dispute.disputeId}
                            </span>
                            {getStatusBadge(dispute.status)}
                            {dispute.walletFrozenUntil && (
                              <Badge variant="destructive" className="gap-1">
                                <Lock className="w-3 h-3" />
                                Ví khóa
                              </Badge>
                            )}
                          </div>
                          <div className="flex items-center gap-2">
                            <span className="font-semibold">{dispute.title}</span>
                            <span className="text-muted-foreground">•</span>
                            <span className="text-sm text-muted-foreground">
                              Đơn hàng #{dispute.bookingId}
                            </span>
                          </div>
                          <div className="flex items-center gap-4 text-sm text-muted-foreground">
                            <div>Tạo: {formatDate(dispute.createdAt, true)}</div>
                            <div>{dispute.messageCount} tin nhắn</div>
                            {dispute.walletFrozenUntil && (
                              <div className="text-destructive">
                                Khóa đến: {formatDate(dispute.walletFrozenUntil, true)}
                              </div>
                            )}
                          </div>
                          {dispute.description && (
                            <p className="text-sm text-muted-foreground line-clamp-2">
                              {dispute.description}
                            </p>
                          )}
                        </div>
                        <div className="text-right">
                          <ChevronRight className="w-5 h-5 text-muted-foreground" />
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))
              )}
            </div>

            {/* Pagination Info */}
            {filteredDisputes.length > 0 && (
              <div className="flex items-center justify-between pt-4 border-t">
                <p className="text-sm text-muted-foreground">
                  Hiển thị {filteredDisputes.length} / {disputes.length} tranh chấp
                </p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  )
}
