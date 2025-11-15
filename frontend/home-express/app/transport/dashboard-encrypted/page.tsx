/**
 * Enhanced Transport Dashboard with Encrypted Payment Data
 * 
 * Displays transport statistics with encrypted financial data
 * Supports role-based access control and data masking
 */

"use client"

import { useEffect, useMemo, useState } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/contexts/auth-context"
import { useToast } from "@/hooks/use-toast"
import { apiClient } from "@/lib/api-client"
import { DashboardLayout } from "@/components/dashboard/dashboard-layout"
import { StatCard } from "@/components/dashboard/stat-card"
import { EncryptedStatsCard } from "@/components/transport/encrypted-stats-card"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { 
  LayoutDashboard, 
  Truck, 
  Package, 
  Star, 
  TrendingUp, 
  DollarSign, 
  FileText,
  Shield,
  Download,
  Eye,
  AlertTriangle,
  Loader2,
} from "lucide-react"
import { formatVND } from "@/lib/format"
import Link from "next/link"
import type { TransportExportJob } from "@/types"

const navItems = [
  { label: "Tổng quan", href: "/transport", icon: "LayoutDashboard" },
  { label: "Dashboard Mã hóa", href: "/transport/dashboard-encrypted", icon: "Shield" },
  { label: "Công việc", href: "/transport/jobs", icon: "Package" },
  { label: "Báo giá", href: "/transport/quotations", icon: "FileText" },
  { label: "Xe", href: "/transport/vehicles", icon: "Truck" },
  { label: "Giá cả", href: "/transport/pricing/categories", icon: "DollarSign" },
  { label: "Hồ sơ", href: "/transport/profile", icon: "Star" },
]

interface EncryptedStats {
  transportId: number
  completedBookings: number
  inProgressBookings: number
  pendingQuotations: number
  averageRating: number
  totalReviews: number
  completionRate: number
  encryptedTotalIncome: string
  maskedTotalIncome: string
  totalIncome?: number
  monthlyRevenueSeries: Array<{
    month: string
    encryptedRevenue: string
    maskedRevenue: string
    revenue?: number
  }>
  canViewEncryptedData: boolean
  canExportData: boolean
}

export default function EncryptedTransportDashboard() {
  const { user, loading } = useAuth()
  const router = useRouter()
  const { toast } = useToast()
  const [stats, setStats] = useState<EncryptedStats | null>(null)
  const [dataLoading, setDataLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [exportJob, setExportJob] = useState<TransportExportJob | null>(null)
  const [exporting, setExporting] = useState(false)
  const [lastDownloadedJobId, setLastDownloadedJobId] = useState<number | null>(null)
  const [pollError, setPollError] = useState<string | null>(null)

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
        setError(null)
        const statsData = await apiClient.getEncryptedTransportStats()
        setStats(statsData)
      } catch (error) {
        console.error("Failed to fetch encrypted stats:", error)
        setError("Không thể tải dữ liệu mã hóa. Vui lòng thử lại sau.")
      } finally {
        setDataLoading(false)
      }
    }

    fetchData()
  }, [user])

  const handleExportData = async () => {
    if (!stats?.canExportData) {
      alert("Bạn không có quyền xuất dữ liệu")
      return
    }
    
    // TODO: Implement data export with audit logging
    alert("Chức năng xuất dữ liệu đang được phát triển")
  }

  useEffect(() => {
    if (!exportJob || exportJob.status !== "PROCESSING") {
      return
    }

    const interval = setInterval(async () => {
      try {
        const updated = await apiClient.getTransportExportJob(exportJob.job_id)
        setExportJob(updated)
        setPollError(null)
      } catch (err) {
        console.error("Failed to poll export job", err)
        setPollError("KhA'ng c??p nh??-t tr???ng thA?i bA?o cA?o.")
      }
    }, 3000)

    return () => clearInterval(interval)
  }, [exportJob])

  useEffect(() => {
    if (
      !exportJob ||
      exportJob.status !== "COMPLETED" ||
      !exportJob.download_token ||
      lastDownloadedJobId === exportJob.job_id
    ) {
      return
    }

    const downloadReport = async () => {
      try {
        const blob = await apiClient.downloadTransportExportJob(exportJob.job_id, exportJob.download_token!)
        const url = URL.createObjectURL(blob)
        const link = document.createElement("a")
        link.href = url
        link.download = `wallet-transactions-${exportJob.job_id}.csv`
        document.body.appendChild(link)
        link.click()
        link.remove()
        URL.revokeObjectURL(url)
        setLastDownloadedJobId(exportJob.job_id)
        toast({
          title: "??A? tA?i bA?o cA?o",
          description: "B?o cA?o giao d??<ch vA? ?`A? tA?i xuO?ng thA?nh cA?ng.",
        })
      } catch (err) {
        toast({
          title: "KhA'ng th??? tA?i bA?o cA?o",
          description: err instanceof Error ? err.message : "Vui lA?ng th??- l???i sau.",
          variant: "destructive",
        })
      }
    }

    downloadReport().catch((err) => console.error(err))
  }, [exportJob, lastDownloadedJobId, toast])

  const exportStatusDescription = useMemo(() => {
    if (!exportJob) return null

    switch (exportJob.status) {
      case "PROCESSING":
        return "H??? th??`ng ?`ang sinh bA?o cA?o giao d??<ch v�."
      case "COMPLETED":
        return "B?o cA?o A? sA?n sA?ng. ChA? tiA?p tA?i xuO?ng ho???c kiA?m tra email."
      case "FAILED":
        return exportJob.failure_reason || "KhA'ng tA?o ?`A?c bA?o cA?o."
      default:
        return null
    }
  }, [exportJob])
  if (loading || dataLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-accent-green" />
      </div>
    )
  }

  if (error) {
    return (
      <DashboardLayout navItems={navItems} title="Dashboard Mã hóa">
        <Alert variant="destructive">
          <AlertTriangle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </DashboardLayout>
    )
  }

  if (!user || !stats) return null

  return (
    <DashboardLayout navItems={navItems} title="Dashboard Mã hóa">
      <div className="space-y-8">
        {/* Header */}
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
              <Shield className="h-8 w-8 text-green-600" />
              Dashboard Bảo mật
            </h1>
            <p className="text-muted-foreground mt-1">
              Dữ liệu tài chính được mã hóa AES-256-GCM
            </p>
          </div>
          
          {stats.canExportData && (
            <Button onClick={handleExportData} variant="outline" className="gap-2">
              <Download className="h-4 w-4" />
              Xuất dữ liệu
            </Button>
          )}
        </div>

        {/* Security Notice */}
        <Alert>
          <Shield className="h-4 w-4" />
          <AlertDescription>
            {stats.canViewEncryptedData ? (
              <>
                <strong>Quyền truy cập:</strong> Bạn có quyền xem dữ liệu tài chính đã giải mã. 
                Tất cả truy cập được ghi nhận để đảm bảo tuân thủ quy định.
              </>
            ) : (
              <>
                <strong>Dữ liệu bị hạn chế:</strong> Bạn chỉ có thể xem dữ liệu tài chính ở dạng ẩn. 
                Liên hệ quản trị viên để được cấp quyền truy cập đầy đủ.
              </>
            )}
          </AlertDescription>
        </Alert>

        {/* Encrypted Financial Stats */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <EncryptedStatsCard
            title="Tổng thu nhập"
            maskedValue={stats.maskedTotalIncome}
            decryptedValue={stats.totalIncome}
            icon={<DollarSign className="w-6 h-6" />}
            variant="success"
            canViewEncrypted={stats.canViewEncryptedData}
            trend="+15%"
          />
          
          <StatCard
            title="Đơn hoàn thành"
            value={stats.completedBookings}
            icon={<Package className="w-6 h-6" />}
            variant="default"
          />
          
          <StatCard
            title="Đang vận chuyển"
            value={stats.inProgressBookings}
            icon={<Truck className="w-6 h-6" />}
            variant="warning"
          />
        </div>

        {/* Non-sensitive Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <StatCard
            title="Đánh giá trung bình"
            value={`${stats.averageRating}/5`}
            icon={<Star className="w-6 h-6" />}
            variant="success"
          />
          
          <StatCard
            title="Tỷ lệ hoàn thành"
            value={`${stats.completionRate}%`}
            icon={<TrendingUp className="w-6 h-6" />}
            variant="success"
          />
          
          <StatCard
            title="Báo giá chờ xử lý"
            value={stats.pendingQuotations}
            icon={<FileText className="w-6 h-6" />}
            variant="default"
          />
        </div>

        {/* Encrypted Monthly Revenue Chart */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="flex items-center gap-2">
                <Shield className="h-5 w-5 text-green-600" />
                Doanh thu theo tháng (Mã hóa)
              </CardTitle>
              {stats.canViewEncryptedData && (
                <Badge variant="outline" className="gap-1">
                  <Eye className="h-3 w-3" />
                  Có thể xem chi tiết
                </Badge>
              )}
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {stats.monthlyRevenueSeries.map((item) => {
                const displayValue = stats.canViewEncryptedData && item.revenue !== undefined
                  ? formatVND(item.revenue)
                  : item.maskedRevenue
                
                const maxRevenue = Math.max(
                  ...stats.monthlyRevenueSeries
                    .filter(r => r.revenue !== undefined)
                    .map(r => r.revenue!)
                )
                
                const percentage = item.revenue !== undefined && maxRevenue > 0
                  ? (item.revenue / maxRevenue) * 100
                  : 50 // Default width for masked data

                return (
                  <div key={item.month} className="flex items-center gap-4">
                    <div className="w-16 text-sm font-medium text-muted-foreground">
                      {item.month}
                    </div>
                    <div className="flex-1">
                      <div className="h-8 bg-muted rounded-lg overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-green-500 to-green-600 transition-all duration-300"
                          style={{ width: `${percentage}%` }}
                        />
                      </div>
                    </div>
                    <div className="w-32 text-sm font-semibold text-right">
                      {displayValue}
                    </div>
                  </div>
                )
              })}
            </div>
          </CardContent>
        </Card>

        {/* Compliance Information */}
        <Card>
          <CardHeader>
            <CardTitle>Thông tin Tuân thủ</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-sm">
            <div className="flex items-start gap-2">
              <Shield className="h-4 w-4 text-green-600 mt-0.5" />
              <div>
                <p className="font-medium">Mã hóa AES-256-GCM</p>
                <p className="text-muted-foreground">
                  Dữ liệu tài chính được mã hóa với thuật toán AES-256-GCM, đáp ứng tiêu chuẩn PCI-DSS
                </p>
              </div>
            </div>
            <div className="flex items-start gap-2">
              <Eye className="h-4 w-4 text-blue-600 mt-0.5" />
              <div>
                <p className="font-medium">Ghi nhận truy cập</p>
                <p className="text-muted-foreground">
                  Mọi truy cập vào dữ liệu mã hóa được ghi nhận và lưu trữ để kiểm toán
                </p>
              </div>
            </div>
            <div className="flex items-start gap-2">
              <Download className="h-4 w-4 text-purple-600 mt-0.5" />
              <div>
                <p className="font-medium">Xuất dữ liệu có kiểm soát</p>
                <p className="text-muted-foreground">
                  Chỉ người dùng được ủy quyền mới có thể xuất dữ liệu tài chính
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  )
}


