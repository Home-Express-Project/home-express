"use client"

import { useEffect, useState } from "react"
import { useRouter, useParams } from "next/navigation"
import { useAuth } from "@/contexts/auth-context"
import { apiClient } from "@/lib/api-client"
import { DashboardLayout } from "@/components/dashboard/dashboard-layout"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import {
  ArrowLeft,
  AlertCircle,
  Clock,
  CheckCircle2,
  Lock,
  Send,
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

interface DisputeDetail {
  disputeId: number
  bookingId: number
  settlementId?: number
  title: string
  description: string
  status: string
  walletFrozen: boolean
  walletFrozenUntil?: string
  messages: Array<{
    messageId: number
    sentByRole: string
    messageText: string
    createdAt: string
  }>
  createdAt: string
  updatedAt: string
}

export default function DisputeDetailPage() {
  const { user, loading } = useAuth()
  const router = useRouter()
  const params = useParams()
  const disputeId = params?.id as string
  const [dispute, setDispute] = useState<DisputeDetail | null>(null)
  const [dataLoading, setDataLoading] = useState(true)
  const [messageText, setMessageText] = useState("")
  const [isSending, setIsSending] = useState(false)

  useEffect(() => {
    if (!loading && (!user || user.role !== "TRANSPORT")) {
      router.push("/login")
    }
  }, [user, loading, router])

  useEffect(() => {
    const fetchData = async () => {
      if (!user || user.role !== "TRANSPORT" || !disputeId) return

      try {
        setDataLoading(true)
        const data = await apiClient.getTransportDisputeDetails(parseInt(disputeId))
        setDispute(data)
      } catch (error) {
        console.error("Error fetching dispute details:", error)
      } finally {
        setDataLoading(false)
      }
    }

    fetchData()
  }, [user, disputeId])

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

  const handleSendMessage = async () => {
    if (!messageText.trim() || !dispute) return

    try {
      setIsSending(true)
      // Note: This would call the actual API method when available
      // await apiClient.addTransportDisputeMessage(dispute.disputeId, { messageText })
      console.log("Message sent:", messageText)
      setMessageText("")
      // Refetch dispute to get updated messages
      const data = await apiClient.getTransportDisputeDetails(dispute.disputeId)
      setDispute(data)
    } catch (error) {
      console.error("Error sending message:", error)
    } finally {
      setIsSending(false)
    }
  }

  if (loading || dataLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-accent-green" />
      </div>
    )
  }

  if (!user || !dispute) return null

  return (
    <DashboardLayout navItems={navItems} title="Chi tiết tranh chấp">
      <div className="max-w-4xl mx-auto space-y-8 py-10">
        {/* Header with back button */}
        <div className="flex items-center gap-4">
          <Button
            variant="outline"
            size="icon"
            onClick={() => router.push("/transport/earnings/disputes")}
          >
            <ArrowLeft className="w-4 h-4" />
          </Button>
          <div>
            <h1 className="text-3xl font-bold tracking-tight">
              {dispute.title}
            </h1>
            <p className="text-muted-foreground mt-1">
              Tranh chấp #{dispute.disputeId}
            </p>
          </div>
        </div>

        {/* Status and Info */}
        <Card className="hover:shadow-md transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between">
            <div>
              <CardTitle>Thông tin tranh chấp</CardTitle>
            </div>
            {getStatusBadge(dispute.status)}
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div>
                <p className="text-sm text-muted-foreground mb-1">Mã Tranh chấp</p>
                <p className="font-semibold">{dispute.disputeId}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground mb-1">Mã Booking</p>
                <p className="font-semibold">#{dispute.bookingId}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground mb-1">Ngày tạo</p>
                <p className="font-semibold">{formatDate(dispute.createdAt, true)}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground mb-1">Cập nhật</p>
                <p className="font-semibold">{formatDate(dispute.updatedAt, true)}</p>
              </div>
            </div>

            {dispute.walletFrozen && (
              <div className="mt-4 p-4 bg-destructive/10 border border-destructive/20 rounded-lg">
                <div className="flex items-start gap-2">
                  <Lock className="w-5 h-5 text-destructive flex-shrink-0 mt-0.5" />
                  <div>
                    <p className="font-semibold text-destructive">Ví bị khóa</p>
                    <p className="text-sm text-muted-foreground mt-1">
                      Ví của bạn đã bị khóa để bảo vệ số tiền trong tranh chấp này.
                    </p>
                    {dispute.walletFrozenUntil && (
                      <p className="text-sm text-destructive mt-2">
                        Sẽ mở khóa: {formatDate(dispute.walletFrozenUntil, true)}
                      </p>
                    )}
                  </div>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Description */}
        <Card className="hover:shadow-md transition-shadow">
          <CardHeader>
            <CardTitle>Chi tiết</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="whitespace-pre-wrap text-sm">{dispute.description}</p>
          </CardContent>
        </Card>

        {/* Messages Thread */}
        <Card className="hover:shadow-md transition-shadow">
          <CardHeader>
            <CardTitle>Bình luận ({dispute.messages.length})</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {/* Messages List */}
            <div className="space-y-4 max-h-96 overflow-y-auto">
              {dispute.messages.length === 0 ? (
                <p className="text-center text-muted-foreground py-8">
                  Chưa có bình luận nào
                </p>
              ) : (
                dispute.messages.map((message) => (
                  <div
                    key={message.messageId}
                    className={`p-3 rounded-lg ${
                      message.sentByRole === "TRANSPORT"
                        ? "bg-blue-50 border border-blue-200"
                        : "bg-gray-50 border border-gray-200"
                    }`}
                  >
                    <div className="flex items-center justify-between mb-2">
                      <span className="font-semibold text-sm">
                        {message.sentByRole === "TRANSPORT"
                          ? "Bạn"
                          : "Quản lý"}
                      </span>
                      <span className="text-xs text-muted-foreground">
                        {formatDate(message.createdAt, true)}
                      </span>
                    </div>
                    <p className="text-sm whitespace-pre-wrap">
                      {message.messageText}
                    </p>
                  </div>
                ))
              )}
            </div>

            {/* Message Input */}
            {!["RESOLVED", "CLOSED"].includes(dispute.status) && (
              <div className="border-t pt-4 space-y-3">
                <div className="flex gap-2">
                  <Input
                    placeholder="Nhập bình luận của bạn..."
                    value={messageText}
                    onChange={(e) => setMessageText(e.target.value)}
                    onKeyPress={(e) => {
                      if (e.key === "Enter" && !e.shiftKey) {
                        e.preventDefault()
                        handleSendMessage()
                      }
                    }}
                    disabled={isSending}
                  />
                  <Button
                    onClick={handleSendMessage}
                    disabled={isSending || !messageText.trim()}
                    className="bg-accent-green hover:bg-accent-green/90"
                  >
                    <Send className="w-4 h-4" />
                  </Button>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Actions */}
        <div className="flex gap-4">
          <Button
            variant="outline"
            onClick={() => router.push("/transport/earnings/disputes")}
          >
            Quay lại danh sách
          </Button>
          {["RESOLVED"].includes(dispute.status) && (
            <Button
              variant="outline"
              onClick={() => router.push("/transport/earnings/settlements")}
            >
              Xem chi tiết quyết toán
            </Button>
          )}
        </div>
      </div>
    </DashboardLayout>
  )
}
