"use client"

import { useState } from "react"
import { Save, LayoutDashboard, Truck, Package, Star, DollarSign, FileText, AlertTriangle } from "lucide-react"
import { DashboardLayout } from "@/components/dashboard/dashboard-layout"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Separator } from "@/components/ui/separator"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { useCategories } from "@/hooks/use-categories"
import { useCategoryPricing, useReadyToQuoteStatus } from "@/hooks/use-vehicles"
import { useToast } from "@/hooks/use-toast"
import { apiClient } from "@/lib/api-client"
import { formatVND } from "@/lib/format"

const navItems = [
  { label: "Tổng quan", href: "/transport", icon: "LayoutDashboard" },
  { label: "Công việc", href: "/transport/jobs", icon: "Package" },
  { label: "Báo giá", href: "/transport/quotations", icon: "FileText" },
  { label: "Xe", href: "/transport/vehicles", icon: "Truck" },
  { label: "Giá cả", href: "/transport/pricing/categories", icon: "DollarSign" },
  { label: "Hồ sơ", href: "/transport/profile", icon: "Star" },
]

export default function CategoryPricingPage() {
  const { categories, isLoading: categoriesLoading } = useCategories({ isActive: true }, { scope: "transport" })
  const { rateCards, isLoading: rateCardsLoading, mutate } = useCategoryPricing()
  const { status: readyStatus, isLoading: readyStatusLoading } = useReadyToQuoteStatus()
  const { toast } = useToast()
  const [savingIds, setSavingIds] = useState<Set<number>>(new Set())

  const [pricingData, setPricingData] = useState<Record<number, any>>({})

  const toDateTimeInputValue = (iso: string | null | undefined) => {
    if (!iso) return ""
    // Ensure value is compatible with datetime-local input (no seconds, local timezone if possible)
    const date = new Date(iso)
    const pad = (n: number) => n.toString().padStart(2, "0")
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(
      date.getMinutes(),
    )}`
  }

  const toLocalDateTimeString = (value: string) => {
    // value is from datetime-local input (no timezone)
    if (!value) return ""
    const date = new Date(value)
    return date.toISOString()
  }

  const handlePriceChange = (categoryId: number, field: string, value: number | string) => {
    setPricingData((prev) => ({
      ...prev,
      [categoryId]: {
        ...prev[categoryId],
        [field]: value,
      },
    }))
  }

  const handleSave = async (categoryId: number, categoryName: string, data: any) => {
    if (
      !data?.pricePerUnit ||
      !data?.pricePerKm ||
      !data?.pricePerHour ||
      !data?.minimumCharge ||
      !data?.validFrom ||
      !data?.validUntil
    ) {
      toast({
        title: "Lỗi",
        description: "Vui lòng nhập đầy đủ giá và thời gian hiệu lực cho danh mục này",
        variant: "destructive",
      })
      return
    }

    setSavingIds((prev) => new Set(prev).add(categoryId))
    try {
      await apiClient.createOrUpdateRateCard({
        category_id: categoryId,
        base_price: Number(data.pricePerUnit),
        price_per_km: Number(data.pricePerKm),
        price_per_hour: Number(data.pricePerHour),
        minimum_charge: Number(data.minimumCharge),
        valid_from: toLocalDateTimeString(data.validFrom),
        valid_until: toLocalDateTimeString(data.validUntil),
        additional_rules: {
          fragile_multiplier: data.fragileMultiplier || 1.2,
          disassembly_multiplier: data.disassemblyMultiplier || 1.3,
          heavy_item_multiplier: data.heavyMultiplier || 1.5,
        },
      })

      toast({
        title: "Thành công",
        description: `Đã lưu giá cho ${categoryName}`,
      })

      mutate()
    } catch (error) {
      toast({
        title: "Lỗi",
        description: error instanceof Error ? error.message : "Không thể lưu giá",
        variant: "destructive",
      })
    } finally {
      setSavingIds((prev) => {
        const newSet = new Set(prev)
        newSet.delete(categoryId)
        return newSet
      })
    }
  }

  const getExistingRateCard = (categoryId: number) => {
    return rateCards.find((card: any) => card.category_id === categoryId && card.is_active)
  }

  if (categoriesLoading || rateCardsLoading || readyStatusLoading) {
    return (
      <DashboardLayout navItems={navItems} title="Cài đặt giá theo loại đồ">
        <div className="flex items-center justify-center h-96">
          <p className="text-muted-foreground">Đang tải...</p>
        </div>
      </DashboardLayout>
    )
  }

  return (
    <DashboardLayout navItems={navItems} title="Cài đặt giá theo loại đồ">
      <div className="max-w-7xl mx-auto px-6 py-10 space-y-6">
        {/* Header */}
        <div>
          <h1 className="text-3xl font-bold">Cài đặt giá theo loại đồ</h1>
          <p className="text-muted-foreground">Cấu hình giá và hệ số cho từng loại đồ đạc</p>
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
                    : "Bạn cần cấu hình bảng giá để bắt đầu nhận công việc."}
                </p>
                <p className="text-sm">
                  {readyStatus.reason ||
                    (readyStatus.ready_to_quote
                      ? "Bạn có ít nhất một bảng giá còn hiệu lực. Hệ thống có thể dùng để gợi ý giá báo."
                      : "Tạo bảng giá cho các loại đồ bên dưới. Khi bảng giá có hiệu lực, hệ thống sẽ mở khoá danh sách công việc.")}
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
              {!readyStatus.ready_to_quote && (
                <Button variant="outline" size="sm" disabled>
                  Chưa sẵn sàng nhận công việc
                </Button>
              )}
            </AlertDescription>
          </Alert>
        )}

        {/* Categories List */}
        <div className="space-y-4">
          {categories.map((category: any) => {
            const existingRateCard = getExistingRateCard(category.category_id)
            const baseData = existingRateCard
              ? {
                  pricePerUnit: existingRateCard.base_price,
                  pricePerKm: existingRateCard.price_per_km,
                  pricePerHour: existingRateCard.price_per_hour,
                  minimumCharge: existingRateCard.minimum_charge,
                  fragileMultiplier: existingRateCard.additional_rules?.fragile_multiplier ?? 1.2,
                  disassemblyMultiplier:
                    existingRateCard.additional_rules?.disassembly_multiplier ?? 1.3,
                  heavyMultiplier: existingRateCard.additional_rules?.heavy_item_multiplier ?? 1.5,
                  validFrom: toDateTimeInputValue(existingRateCard.valid_from),
                  validUntil: toDateTimeInputValue(existingRateCard.valid_until),
                }
              : {
                  fragileMultiplier: 1.2,
                  disassemblyMultiplier: 1.3,
                  heavyMultiplier: 1.5,
                  validFrom: "",
                  validUntil: "",
                }

            const currentData = {
              ...baseData,
              ...(pricingData[category.category_id] || {}),
            }

            const isSaving = savingIds.has(category.category_id)

            return (
              <Card key={category.category_id} className="hover:shadow-md transition-shadow">
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      {category.icon && <span className="text-2xl">{category.icon}</span>}
                      <div>
                        <CardTitle>{category.name}</CardTitle>
                        <CardDescription>
                          Mặc định: {category.default_weight_kg}kg, {category.default_volume_m3}m³
                        </CardDescription>
                        {existingRateCard && existingRateCard.valid_until && (
                          <p className="text-xs text-muted-foreground mt-1">
                            Bảng giá hiện tại có hiệu lực đến:{" "}
                            {new Date(existingRateCard.valid_until).toLocaleString("vi-VN")}
                          </p>
                        )}
                      </div>
                    </div>
                    <Button
                      size="sm"
                      onClick={() => handleSave(category.category_id, category.name, currentData)}
                      disabled={isSaving}
                    >
                      <Save className="mr-2 h-4 w-4" />
                      {isSaving ? "Đang lưu..." : "Lưu"}
                    </Button>
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid gap-4 md:grid-cols-2">
                    <div className="space-y-2">
                      <Label htmlFor={`price-${category.category_id}`}>
                        Giá cơ bản / chuyến (VND) *
                      </Label>
                      <Input
                        id={`price-${category.category_id}`}
                        type="number"
                        step="1000"
                        placeholder="80000"
                        value={currentData.pricePerUnit || ""}
                        onChange={(e) =>
                          handlePriceChange(category.category_id, "pricePerUnit", Number(e.target.value))
                        }
                      />
                      {currentData.pricePerUnit && (
                        <p className="text-sm text-muted-foreground">{formatVND(currentData.pricePerUnit)}</p>
                      )}
                    </div>
                    <div className="grid gap-4 md:grid-cols-2">
                      <div className="space-y-2">
                        <Label htmlFor={`per-km-${category.category_id}`}>Giá theo km (VND/km) *</Label>
                        <Input
                          id={`per-km-${category.category_id}`}
                          type="number"
                          step="1000"
                          placeholder="5000"
                          value={currentData.pricePerKm || ""}
                          onChange={(e) =>
                            handlePriceChange(category.category_id, "pricePerKm", Number(e.target.value))
                          }
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor={`per-hour-${category.category_id}`}>Giá theo giờ (VND/giờ) *</Label>
                        <Input
                          id={`per-hour-${category.category_id}`}
                          type="number"
                          step="1000"
                          placeholder="80000"
                          value={currentData.pricePerHour || ""}
                          onChange={(e) =>
                            handlePriceChange(category.category_id, "pricePerHour", Number(e.target.value))
                          }
                        />
                      </div>
                    </div>
                  </div>

                  <div className="grid gap-4 md:grid-cols-3">
                    <div className="space-y-2">
                      <Label htmlFor={`minimum-${category.category_id}`}>Giá tối thiểu (VND) *</Label>
                      <Input
                        id={`minimum-${category.category_id}`}
                        type="number"
                        step="1000"
                        placeholder="100000"
                        value={currentData.minimumCharge || ""}
                        onChange={(e) =>
                          handlePriceChange(category.category_id, "minimumCharge", Number(e.target.value))
                        }
                      />
                      {currentData.minimumCharge && (
                        <p className="text-sm text-muted-foreground">{formatVND(currentData.minimumCharge)}</p>
                      )}
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor={`valid-from-${category.category_id}`}>Áp dụng từ *</Label>
                      <Input
                        id={`valid-from-${category.category_id}`}
                        type="datetime-local"
                        value={currentData.validFrom || ""}
                        onChange={(e) =>
                          handlePriceChange(category.category_id, "validFrom", e.target.value)
                        }
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor={`valid-until-${category.category_id}`}>Đến hết ngày *</Label>
                      <Input
                        id={`valid-until-${category.category_id}`}
                        type="datetime-local"
                        value={currentData.validUntil || ""}
                        onChange={(e) =>
                          handlePriceChange(category.category_id, "validUntil", e.target.value)
                        }
                      />
                    </div>
                  </div>

                  <Separator />

                  <div>
                    <h4 className="font-medium mb-3">Hệ số điều chỉnh:</h4>
                    <div className="grid gap-4 md:grid-cols-3">
                      <div className="space-y-2">
                        <Label htmlFor={`fragile-${category.category_id}`}>Dễ vỡ</Label>
                        <Input
                          id={`fragile-${category.category_id}`}
                          type="number"
                          step="0.1"
                          placeholder="1.2"
                          value={currentData.fragileMultiplier || ""}
                          onChange={(e) =>
                            handlePriceChange(category.category_id, "fragileMultiplier", Number(e.target.value))
                          }
                        />
                        <p className="text-sm text-muted-foreground">
                          ×{currentData.fragileMultiplier || 1.2} (
                          {((currentData.fragileMultiplier || 1.2) * 100).toFixed(0)}%)
                        </p>
                      </div>

                      <div className="space-y-2">
                        <Label htmlFor={`disassembly-${category.category_id}`}>Tháo lắp</Label>
                        <Input
                          id={`disassembly-${category.category_id}`}
                          type="number"
                          step="0.1"
                          placeholder="1.3"
                          value={currentData.disassemblyMultiplier || ""}
                          onChange={(e) =>
                            handlePriceChange(
                              category.category_id,
                              "disassemblyMultiplier",
                              Number(e.target.value),
                            )
                          }
                        />
                        <p className="text-sm text-muted-foreground">
                          ×{currentData.disassemblyMultiplier || 1.3} (
                          {((currentData.disassemblyMultiplier || 1.3) * 100).toFixed(0)}%)
                        </p>
                      </div>

                      <div className="space-y-2">
                        <Label htmlFor={`heavy-${category.category_id}`}>Nặng (&gt;100kg)</Label>
                        <Input
                          id={`heavy-${category.category_id}`}
                          type="number"
                          step="0.1"
                          placeholder="1.5"
                          value={currentData.heavyMultiplier || ""}
                          onChange={(e) =>
                            handlePriceChange(category.category_id, "heavyMultiplier", Number(e.target.value))
                          }
                        />
                        <p className="text-sm text-muted-foreground">
                          ×{currentData.heavyMultiplier || 1.5} (
                          {((currentData.heavyMultiplier || 1.5) * 100).toFixed(0)}
                          %)
                        </p>
                      </div>
                    </div>
                  </div>

                  {currentData.pricePerUnit && (
                    <>
                      <Separator />
                      <div className="p-3 bg-muted rounded-lg">
                        <p className="text-sm font-medium mb-2">Ví dụ tính giá:</p>
                        <div className="space-y-1 text-sm text-muted-foreground">
                          <div>• Thường: {formatVND(currentData.pricePerUnit)}</div>
                          <div>
                            • Dễ vỡ: {formatVND(currentData.pricePerUnit * (currentData.fragileMultiplier || 1.2))}
                          </div>
                          <div>
                            • Tháo lắp:{" "}
                            {formatVND(currentData.pricePerUnit * (currentData.disassemblyMultiplier || 1.3))}
                          </div>
                          <div>
                            • Nặng: {formatVND(currentData.pricePerUnit * (currentData.heavyMultiplier || 1.5))}
                          </div>
                        </div>
                      </div>
                    </>
                  )}
                </CardContent>
              </Card>
            )
          })}
        </div>
      </div>
    </DashboardLayout>
  )
}
