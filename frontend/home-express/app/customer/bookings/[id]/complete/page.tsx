"use client"

import { useState, useEffect } from "react"
import { useRouter, useParams } from "next/navigation"
import { useLanguage } from "@/contexts/language-context"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Separator } from "@/components/ui/separator"
import { CheckCircle2, AlertCircle, Building2, ArrowLeft, Gift, Wallet } from "lucide-react"
import { apiClient } from "@/lib/api-client"
import { formatVND } from "@/lib/currency"
import { toast } from "sonner"
import type { BankInfo, BookingDetailResponse, PaymentMethodOption } from "@/types"

export default function CompletePaymentPage() {
  const router = useRouter()
  const params = useParams()
  const bookingId = Number(params.id)
  const { t } = useLanguage()

  const [booking, setBooking] = useState<BookingDetailResponse | null>(null)
  const [bankInfo, setBankInfo] = useState<BankInfo | null>(null)
  const [loading, setLoading] = useState(true)
  const [processing, setProcessing] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethodOption | null>(null)
  const [tipAmount, setTipAmount] = useState<string>("")

  useEffect(() => {
    if (!bookingId || isNaN(bookingId)) {
      toast.error("KhÃ´ng tÃ¬m tháº¥y booking ID")
      router.push("/customer/bookings")
      return
    }

    const loadData = async () => {
      try {
        const [bookingData, bankData] = await Promise.all([
          apiClient.getBookingDetail(bookingId),
          apiClient.getBankInfo(),
        ])

        if (bookingData.booking.status !== "COMPLETED") {
          toast.error("Booking chÆ°a hoÃ n thÃ nh. KhÃ´ng thá»ƒ thanh toÃ¡n pháº§n cÃ²n láº¡i.")
          router.push(`/customer/bookings/${bookingId}`)
          return
        }

        setBooking(bookingData)
        setBankInfo(bankData)
      } catch (err) {
        console.error("Failed to load booking data:", err)
        setError(err instanceof Error ? err.message : "CÃ³ lá»—i xáº£y ra")
      } finally {
        setLoading(false)
      }
    }

    loadData()
  }, [bookingId, router])

  const calculateRemainingAmount = () => {
    if (!booking?.booking.final_price) return 0
    return Math.round(booking.booking.final_price * 0.7)
  }

  const calculateTotalAmount = () => {
    const remaining = calculateRemainingAmount()
    const tip = parseInt(tipAmount) || 0
    return remaining + tip
  }

  const handlePayment = async () => {
    if (!booking) return

    setError(null)

    if (!paymentMethod) {
      const message = "Vui lòng chọn phương thức thanh toán cho phần còn lại"
      setError(message)
      toast.error(message)
      return
    }

    setProcessing(true)

    try {
      const tipAmountVnd = parseInt(tipAmount) || 0

      const response = await apiClient.initiateRemainingPayment({
        bookingId: booking.booking.booking_id,
        paymentMethod,
        tipAmountVnd: tipAmountVnd > 0 ? tipAmountVnd : undefined,
      })

      if (!response.success) {
        throw new Error(response.message || "KhÃ´ng thá»ƒ khá»Ÿi táº¡o thanh toÃ¡n")
      }

      toast.success("ÄÃ£ khá»Ÿi táº¡o thanh toÃ¡n. Vui lÃ²ng thanh toÃ¡n pháº§n cÃ²n láº¡i theo hÆ°á»›ng dáº«n cá»§a Home Express.")
      router.push(`/customer/bookings/${bookingId}`)
    } catch (err) {
      console.error("Payment initiation failed:", err)
      const errorMessage = err instanceof Error ? err.message : "CÃ³ lá»—i xáº£y ra khi khá»Ÿi táº¡o thanh toÃ¡n"
      setError(errorMessage)
      toast.error(errorMessage)
    } finally {
      setProcessing(false)
    }
  }

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-2xl mx-auto">
          <Card>
            <CardContent className="p-8">
              <div className="flex items-center justify-center">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                <span className="ml-3">Äang táº£i...</span>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    )
  }

  if (error && !booking) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-2xl mx-auto">
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>{error}</AlertDescription>
          </Alert>
          <div className="mt-4">
            <Button onClick={() => router.push("/customer/bookings")} variant="outline">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Quay láº¡i danh sÃ¡ch booking
            </Button>
          </div>
        </div>
      </div>
    )
  }

  if (!booking) return null

  const remainingAmount = calculateRemainingAmount()
  const totalAmount = calculateTotalAmount()

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-2xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => router.push(`/customer/bookings/${bookingId}`)}
          >
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div>
            <h1 className="text-2xl font-bold">HoÃ n táº¥t thanh toÃ¡n</h1>
            <p className="text-muted-foreground">Booking #{bookingId}</p>
          </div>
        </div>

        {/* Payment Summary */}
        <Card>
          <CardHeader>
            <CardTitle>Tá»•ng quan thanh toÃ¡n</CardTitle>
            <CardDescription>
              Thanh toÃ¡n pháº§n cÃ²n láº¡i (70%) cho dá»‹ch vá»¥ Ä‘Ã£ hoÃ n thÃ nh
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Tá»•ng giÃ¡ trá»‹ booking:</span>
                <span className="font-medium">{formatVND(booking.booking.final_price || 0)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">ÄÃ£ thanh toÃ¡n (30%):</span>
                <span className="font-medium text-green-600">
                  {formatVND(Math.round((booking.booking.final_price || 0) * 0.3))}
                </span>
              </div>
              <Separator />
              <div className="flex justify-between">
                <span className="font-medium">CÃ²n láº¡i (70%):</span>
                <span className="text-lg font-bold">{formatVND(remainingAmount)}</span>
              </div>
            </div>

            {/* Tip Input */}
            <div className="space-y-2 pt-4 border-t">
              <Label htmlFor="tip" className="flex items-center gap-2">
                <Gift className="h-4 w-4" />
                Tiá»n tip cho tÃ i xáº¿ (tÃ¹y chá»n)
              </Label>
              <Input
                id="tip"
                type="number"
                placeholder="0"
                value={tipAmount}
                onChange={(e) => setTipAmount(e.target.value)}
                min="0"
                step="10000"
              />
              <p className="text-xs text-muted-foreground">
                Náº¿u báº¡n hÃ i lÃ²ng vá»›i dá»‹ch vá»¥, hÃ£y Ä‘á»ƒ láº¡i tip cho tÃ i xáº¿
              </p>
            </div>

            {parseInt(tipAmount) > 0 && (
              <>
                <Separator />
                <div className="flex justify-between text-lg font-bold">
                  <span>Tá»•ng cá»™ng:</span>
                  <span className="text-primary">{formatVND(totalAmount)}</span>
                </div>
              </>
            )}
          </CardContent>
        </Card>

        {/* Payment Method Selection */}
        <Card>
          <CardHeader>
            <CardTitle>PhÆ°Æ¡ng thá»©c thanh toÃ¡n</CardTitle>
            <CardDescription>Chá»n phÆ°Æ¡ng thá»©c thanh toÃ¡n phÃ¹ há»£p</CardDescription>
          </CardHeader>
          <CardContent>
            <RadioGroup
              value={paymentMethod ?? ""}
              onValueChange={(value: PaymentMethodOption) => setPaymentMethod(value)}
            >
              <div className="space-y-3">
                <div className="flex items-center space-x-3 border rounded-lg p-4 cursor-pointer hover:bg-accent">
                  <RadioGroupItem value="bank" id="bank" />
                  <Label htmlFor="bank" className="flex items-center gap-3 cursor-pointer flex-1">
                    <Building2 className="h-5 w-5 text-green-600" />
                    <div>
                      <div className="font-medium">Chuyen khoan ngan hang</div>
                      <div className="text-sm text-muted-foreground">Thanh toan 70% + tip qua chuyen khoan</div>
                      <p className="text-xs text-muted-foreground mt-1">
                        Ghi HOMEEXPRESS #{bookingId} + REMAINING trong noi dung de he thong settlement tu dong (workflow 3.8).
                      </p>
                    </div>
                  </Label>
                </div>
                <div className="flex items-center space-x-3 border rounded-lg p-4 cursor-pointer hover:bg-accent">
                  <RadioGroupItem value="cash" id="cash" />
                  <Label htmlFor="cash" className="flex items-center gap-3 cursor-pointer flex-1">
                    <Wallet className="h-5 w-5 text-amber-600" />
                    <div>
                      <div className="font-medium">Tien mat (COD)</div>
                      <div className="text-sm text-muted-foreground">Tra truc tiep sau khi cong viec hoan tat</div>
                      <p className="text-xs text-muted-foreground mt-1">
                        Ban giao {formatVND(totalAmount)} cho tai xe/doi van chuyen khi cong viec duoc xac nhan hoan thanh.
                      </p>
                    </div>
                  </Label>
                </div>
              </div>
            </RadioGroup>
            {!paymentMethod && (
              <p className="text-sm text-destructive mt-2">Vui long chon phuong thuc thanh toan truoc khi khoi tao</p>
            )}
          </CardContent>
        </Card>

        {paymentMethod === "bank" && bankInfo && (
          <Alert>
            <Building2 className="h-4 w-4" />
            <AlertDescription>
              <div className="space-y-2 mt-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Ngân hàng:</span>
                  <span className="font-semibold">{bankInfo.bank}</span>
                </div>
                <Separator />
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Số tài khoản:</span>
                  <span className="font-semibold">{bankInfo.accountNumber}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Chủ tài khoản:</span>
                  <span className="font-semibold">{bankInfo.accountName}</span>
                </div>
                {bankInfo.branch && (
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Chi nhánh:</span>
                    <span className="font-semibold">{bankInfo.branch}</span>
                  </div>
                )}
                <Separator />
                <div className="space-y-1 text-xs text-muted-foreground">
                  <p>
                    Chuyển khoản phần còn lại + tip (nếu có) với nội dung: <span className="font-semibold">HOMEEXPRESS {bookingId} REMAINING</span>.
                  </p>
                  <p>Ghi đúng nội dung và số tiền để hệ thống tự động cập nhật Payment REMAINING và TIP.</p>
                </div>
              </div>
            </AlertDescription>
          </Alert>
        )}

        {paymentMethod === "cash" && (
          <Alert>
            <Wallet className="h-4 w-4" />
            <AlertDescription>
              <div className="space-y-2 mt-2 text-sm">
                <p>
                  Chuẩn bị {formatVND(calculateTotalAmount())} tiền mặt (bao gồm phần còn lại và tip nếu có) để giao lại cho tài xế/đội vận chuyển
                  khi việc vận chuyển kết thúc.
                </p>
                <p>
                  Sau khi tài xế xác nhận (workflow 3.8), hệ thống tạo bản ghi REMAINING_PAYMENT và TIP để hoàn tất settlement.
                </p>
              </div>
            </AlertDescription>
          </Alert>
        )}

        {/* Error Alert */}
        {error && (
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {/* Action Buttons */}
        <div className="flex gap-4">
          <Button
            variant="outline"
            onClick={() => router.push(`/customer/bookings/${bookingId}`)}
            disabled={processing}
            className="flex-1"
          >
            Há»§y
          </Button>
          <Button
            onClick={handlePayment}
            disabled={processing || !booking || !paymentMethod}
            className="flex-1"
          >
            {processing ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                Äang xá»­ lÃ½...
              </>
            ) : (
              <>
                <CheckCircle2 className="mr-2 h-4 w-4" />
                Thanh toÃ¡n {formatVND(totalAmount)}
              </>
            )}
          </Button>
        </div>
      </div>
    </div>
  )
}
