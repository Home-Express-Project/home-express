"use client"

import { useState, useEffect, Suspense } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { useLanguage } from "@/contexts/language-context"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { Label } from "@/components/ui/label"
import { Separator } from "@/components/ui/separator"
import { CheckCircle2, AlertCircle, CreditCard, Building2, Copy, Check, RefreshCw, Wallet } from "lucide-react"
import { BookingFlowBreadcrumb } from "@/components/customer/booking-flow-breadcrumb"
import PaymentWatcher from "./payment-watcher"
import { apiClient } from "@/lib/api-client"
import { formatVND } from "@/lib/currency"
import { toast } from "sonner"
import type { BookingDetailResponse, BankInfo, PaymentMethodOption } from "@/types"

function CheckoutPageContent() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const bookingId = Number(searchParams.get("bookingId"))
  const { t } = useLanguage()

  const [booking, setBooking] = useState<BookingDetailResponse | null>(null)
  const [bankInfo, setBankInfo] = useState<BankInfo | null>(null)
  const [loading, setLoading] = useState(true)
  const [processing, setProcessing] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethodOption | null>(null)
  const [copied, setCopied] = useState(false)
  const [paymentInitiated, setPaymentInitiated] = useState(false)

  useEffect(() => {
    if (!bookingId || isNaN(bookingId)) {
      toast.error("KhÃ´ng tÃ¬m tháº¥y booking ID")
      router.push("/customer/bookings/create")
      return
    }

    const loadData = async () => {
      try {
        const [bookingData, bankData] = await Promise.all([
          apiClient.getBookingDetail(bookingId),
          apiClient.getBankInfo(),
        ])

        setBooking(bookingData)
        setBankInfo(bankData)
      } catch (err) {
        console.error("Failed to load checkout data:", err)
        setError(err instanceof Error ? err.message : "CÃ³ lá»—i xáº£y ra")
      } finally {
        setLoading(false)
      }
    }

    loadData()
  }, [bookingId, router])

  const handleRetryLoad = () => {
    setError(null)
    setLoading(true)
    
    const loadData = async () => {
      try {
        const [bookingData, bankData] = await Promise.all([
          apiClient.getBookingDetail(bookingId),
          apiClient.getBankInfo(),
        ])
        setBooking(bookingData)
        setBankInfo(bankData)
      } catch (err) {
        console.error("Failed to load checkout data:", err)
        setError(err instanceof Error ? err.message : "CÃ³ lá»—i xáº£y ra")
      } finally {
        setLoading(false)
      }
    }
    loadData()
  }

  const handleCopy = (text: string) => {
    navigator.clipboard.writeText(text)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
    toast.success("ÄÃ£ sao chÃ©p!")
  }

  const handlePayment = async () => {
    if (!booking) return

    setError(null)

    if (!paymentMethod) {
      const message = "Vui lòng chọn phương thức thanh toán để tiếp tục"
      setError(message)
      toast.error(message)
      return
    }

    setProcessing(true)

    try {
      const response = await apiClient.initiateDeposit({
        bookingId: booking.booking.booking_id,
        paymentMethod,
      })

      if (!response.success) {
        throw new Error(response.message || "KhÃ´ng thá»ƒ khá»Ÿi táº¡o thanh toÃ¡n")
      }

      setPaymentInitiated(true)
      toast.success("ÄÃ£ khá»Ÿi táº¡o thanh toÃ¡n. Vui lÃ²ng chuyá»ƒn khoáº£n theo thÃ´ng tin bÃªn dÆ°á»›i")
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : "CÃ³ lá»—i xáº£y ra"
      setError(errorMessage)
      toast.error(errorMessage)
    } finally {
      setProcessing(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-background to-muted/20 py-8 px-4">
        <div className="max-w-5xl mx-auto">
          <BookingFlowBreadcrumb currentStep={5} />
          <Card>
            <CardContent className="p-12 text-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4" />
              <p className="text-muted-foreground">Äang táº£i...</p>
            </CardContent>
          </Card>
        </div>
      </div>
    )
  }

  if (!booking || !bankInfo) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-background to-muted/20 py-8 px-4">
        <div className="max-w-5xl mx-auto">
          <BookingFlowBreadcrumb currentStep={5} />
          <Card>
            <CardContent className="p-12 text-center space-y-4">
              <AlertCircle className="h-12 w-12 text-destructive mx-auto" />
              <p className="text-lg font-semibold">KhÃ´ng tÃ¬m tháº¥y thÃ´ng tin Ä‘Æ¡n hÃ ng</p>
              {error && <p className="text-sm text-muted-foreground">{error}</p>}
              <div className="flex gap-3 justify-center">
                <Button variant="outline" onClick={handleRetryLoad}>
                  <RefreshCw className="h-4 w-4 mr-2" />
                  Thá»­ láº¡i
                </Button>
                <Button onClick={() => router.push("/customer/bookings/create")}>Quay láº¡i</Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    )
  }

  const depositAmount = Math.round((booking.booking.final_price || 0) * 0.3)
  const transferContent = `HOMEEXPRESS ${bookingId}`

  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted/20 py-8 px-4">
      <div className="max-w-5xl mx-auto">
        <BookingFlowBreadcrumb currentStep={5} />
        <div className="grid lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <CreditCard className="h-5 w-5" />
                  PhÆ°Æ¡ng thá»©c thanh toÃ¡n
                </CardTitle>
                <CardDescription>Chá»n phÆ°Æ¡ng thá»©c thanh toÃ¡n Ä‘áº·t cá»c 30%</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <RadioGroup
                  value={paymentMethod ?? ""}
                  onValueChange={(value: PaymentMethodOption) => setPaymentMethod(value)}
                >

                  <div className="flex items-center space-x-3 border rounded-lg p-4 cursor-pointer hover:bg-muted/50 transition-colors">
                    <RadioGroupItem value="bank" id="bank" />
                    <Label htmlFor="bank" className="flex items-center gap-3 cursor-pointer flex-1">
                      <div className="h-10 w-10 rounded bg-blue-100 flex items-center justify-center">
                        <Building2 className="h-5 w-5 text-blue-600" />
                      </div>
                      <div>
                        <p className="font-semibold">Chuyen khoan ngan hang</p>
                        <p className="text-sm text-muted-foreground">Thanh toan 30% qua chuyen khoan</p>
                        <p className="text-xs text-muted-foreground mt-1">
                          Theo workflow 3.6, khoan chuyen khoan nay giup Home Express khoa lich va phat hanh hop dong.
                        </p>
                      </div>
                    </Label>
                  </div>

                  <div className="flex items-center space-x-3 border rounded-lg p-4 cursor-pointer hover:bg-muted/50 transition-colors">
                    <RadioGroupItem value="cash" id="cash" />
                    <Label htmlFor="cash" className="flex items-center gap-3 cursor-pointer flex-1">
                      <div className="h-10 w-10 rounded bg-amber-100 flex items-center justify-center">
                        <Wallet className="h-5 w-5 text-amber-600" />
                      </div>
                      <div>
                        <p className="font-semibold">Tien mat (COD)</p>
                        <p className="text-sm text-muted-foreground">Giao tien truc tiep cho tai xe</p>
                        <p className="text-xs text-muted-foreground mt-1">
                          Chuan bi {formatVND(depositAmount)} de ban giao khi doi van chuyen den diem lay hang (workflow 3.6).
                        </p>
                      </div>
                    </Label>
                  </div>
                </RadioGroup>
                {!paymentMethod && (
                  <p className="text-sm text-destructive">Vui long chon phuong thuc thanh toan truoc khi tiep tuc</p>
                )}
                {paymentMethod === "bank" && bankInfo && (
                  <Alert>
                    <Building2 className="h-4 w-4" />
                    <AlertDescription>
                      <div className="space-y-2 mt-2">
                        <div className="flex justify-between items-center">
                          <span className="text-sm text-muted-foreground">NgÃ¢n hÃ ng:</span>
                          <span className="font-semibold">{bankInfo.bank}</span>
                        </div>
                        <Separator />
                        <div className="flex justify-between items-center">
                          <span className="text-sm text-muted-foreground">Sá»‘ tÃ i khoáº£n:</span>
                          <div className="flex items-center gap-2">
                            <span className="font-semibold">{bankInfo.accountNumber}</span>
                            <Button
                              variant="ghost"
                              size="icon"
                              className="h-6 w-6"
                              onClick={() => handleCopy(bankInfo.accountNumber)}
                            >
                              {copied ? <Check className="h-3 w-3" /> : <Copy className="h-3 w-3" />}
                            </Button>
                          </div>
                        </div>
                        <Separator />
                        <div className="flex justify-between items-center">
                          <span className="text-sm text-muted-foreground">Chá»§ tÃ i khoáº£n:</span>
                          <span className="font-semibold">{bankInfo.accountName}</span>
                        </div>
                        {bankInfo.branch && (
                          <>
                            <Separator />
                            <div className="flex justify-between items-center">
                              <span className="text-sm text-muted-foreground">Chi nhÃ¡nh:</span>
                              <span className="font-semibold">{bankInfo.branch}</span>
                            </div>
                          </>
                        )}
                        <Separator />
                        <div className="flex justify-between items-center">
                          <span className="text-sm text-muted-foreground">Ná»™i dung:</span>
                          <div className="flex items-center gap-2">
                            <span className="font-semibold">{transferContent}</span>
                            <Button
                              variant="ghost"
                              size="icon"
                              className="h-6 w-6"
                              onClick={() => handleCopy(transferContent)}
                            >
                              {copied ? <Check className="h-3 w-3" /> : <Copy className="h-3 w-3" />}
                            </Button>
                          </div>
                        </div>
                        <Separator />
                        <p className="text-xs text-muted-foreground">
                          Ghi Ä‘Ãºng n?i dung nÃ y Ä‘? h? th?ng t? Ä‘?ng kh?p booking #{bookingId} vÃ  g?i xÃ¡c nh?n Ä‘?t c?c.
                        </p>
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
                          Chon phuong an tien mat neu ban muon giao dich truc tiep. Chuan bi {formatVND(depositAmount)} de ban giao tai diem lay hang.
                        </p>
                        <p>
                          Sau khi tai xe xac nhan khoan dat coc (workflow 3.6), Home Express cap nhat hop dong va thong bao cho ban.
                        </p>
                      </div>
                    </AlertDescription>
                  </Alert>
                )}

                {error && (
                  <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>
                      <div className="flex items-center justify-between gap-4">
                        <span>{error}</span>
                        <Button 
                          variant="outline" 
                          size="sm" 
                          onClick={() => {
                            setError(null)
                            setProcessing(false)
                          }}
                        >
                          ÄÃ³ng
                        </Button>
                      </div>
                    </AlertDescription>
                  </Alert>
                )}

                <Button
                  onClick={handlePayment}
                  disabled={processing || !paymentMethod}
                  size="lg"
                  className="w-full"
                >
                  {processing ? "Äang xá»­ lÃ½..." : `Thanh toÃ¡n ${formatVND(depositAmount)}`}
                </Button>
              </CardContent>
            </Card>
            {paymentInitiated && <PaymentWatcher />}
          </div>

          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="text-base">TÃ³m táº¯t Ä‘Æ¡n hÃ ng</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <p className="text-sm text-muted-foreground">MÃ£ Ä‘Æ¡n hÃ ng</p>
                  <p className="font-semibold">#{booking.booking.booking_id}</p>
                </div>
                <Separator />

                <div>
                  <p className="text-sm text-muted-foreground">Äiá»ƒm Ä‘Ã³n</p>
                  <p className="font-medium text-sm">{booking.booking.pickup_address_line}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Äiá»ƒm tráº£</p>
                  <p className="font-medium text-sm">{booking.booking.delivery_address_line}</p>
                </div>
                <Separator />

                <div>
                  <p className="text-sm text-muted-foreground">Thá»i gian</p>
                  <p className="font-semibold">{booking.booking.preferred_date}</p>
                </div>
                <Separator />

                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">Tá»•ng tiá»n</span>
                    <span className="font-medium">{formatVND(booking.booking.final_price || 0)}</span>
                  </div>
                  <div className="flex justify-between items-center pt-2 border-t-2">
                    <span className="font-semibold">Äáº·t cá»c 30%</span>
                    <span className="text-xl font-bold text-primary">{formatVND(depositAmount)}</span>
                  </div>
                </div>

                <Alert>
                  <CheckCircle2 className="h-4 w-4" />
                  <AlertDescription className="text-xs">
                    Sá»‘ tiá»n cÃ²n láº¡i sáº½ thanh toÃ¡n sau khi hoÃ n táº¥t dá»‹ch vá»¥
                  </AlertDescription>
                </Alert>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  )
}

export default function CheckoutPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen bg-gradient-to-b from-background to-muted/20 py-8 px-4">
          <div className="max-w-5xl mx-auto">
            <Card>
              <CardContent className="p-12 text-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4" />
                <p className="text-muted-foreground">Äang táº£i...</p>
              </CardContent>
            </Card>
          </div>
        </div>
      }
    >
      <CheckoutPageContent />
    </Suspense>
  )
}
