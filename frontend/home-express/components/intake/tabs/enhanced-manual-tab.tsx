"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Combobox, type ComboboxOption } from "@/components/ui/combobox"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Checkbox } from "@/components/ui/checkbox"
import { Badge } from "@/components/ui/badge"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Plus, Trash2, Sparkles, ChevronDown, ChevronUp, Info, AlertCircle } from "lucide-react"
import type { ItemCandidate } from "@/types"
import { apiClient } from "@/lib/api-client"

interface EnhancedManualTabProps {
  onAddCandidates: (candidates: ItemCandidate[]) => void
  submitButtonText?: string
  showSuccessToast?: boolean
}

interface ManualItem {
  // Brand/Model info
  brand: string
  model: string
  productName: string
  
  // Basic info
  name: string
  category: string
  size: "S" | "M" | "L"
  quantity: number
  declared_value: string
  
  // Dimensions
  weight_kg: string
  width_cm: string
  height_cm: string
  depth_cm: string
  
  // Attributes
  is_fragile: boolean
  requires_disassembly: boolean
  
  // UI state
  showAdvanced: boolean
  modelId?: number
}

const CATEGORIES = ["Nội thất", "Điện tử", "Đồ gia dụng", "Quần áo", "Sách vở", "Đồ chơi", "Khác"]

export function EnhancedManualTab({ 
  onAddCandidates,
  submitButtonText = "Thêm vào danh sách",
  showSuccessToast = true
}: EnhancedManualTabProps) {
  const [items, setItems] = useState<ManualItem[]>([
    {
      brand: "",
      model: "",
      productName: "",
      name: "",
      category: "",
      size: "M",
      quantity: 1,
      declared_value: "",
      weight_kg: "",
      width_cm: "",
      height_cm: "",
      depth_cm: "",
      is_fragile: false,
      requires_disassembly: false,
      showAdvanced: false,
    },
  ])

  const [brandOptions, setBrandOptions] = useState<Record<number, ComboboxOption[]>>({})
  const [modelOptions, setModelOptions] = useState<Record<number, ComboboxOption[]>>({})
  const [modelSuggestions, setModelSuggestions] = useState<Record<number, any[]>>({})

  // Search brands
  const brandSearchKey = items.map((i) => i.brand).join(",")
  useEffect(() => {
    items.forEach((item, index) => {
      if (!item.brand) {
        setBrandOptions((prev) => ({ ...prev, [index]: [] }))
        return
      }

      const timer = setTimeout(async () => {
        try {
          const data = await apiClient.searchBrands(item.brand)
          setBrandOptions((prev) => ({
            ...prev,
            [index]: data.brands.map((b) => ({ value: b, label: b })),
          }))
        } catch (err) {
          console.error("Failed to search brands", err)
        }
      }, 300)

      return () => clearTimeout(timer)
    })
  }, [brandSearchKey, items])

  // Search models by brand
  const modelSearchKey = items.map((i) => `${i.brand}:${i.model}`).join(",")
  useEffect(() => {
    items.forEach((item, index) => {
      if (!item.brand) {
        setModelOptions((prev) => ({ ...prev, [index]: [] }))
        setModelSuggestions((prev) => ({ ...prev, [index]: [] }))
        return
      }

      const timer = setTimeout(async () => {
        try {
          const data = await apiClient.searchModels(item.model || "", item.brand)
          setModelOptions((prev) => ({
            ...prev,
            [index]: data.models.map((m) => ({ value: m.model, label: m.model })),
          }))
          setModelSuggestions((prev) => ({ ...prev, [index]: data.models }))
        } catch (err) {
          console.error("Failed to search models", err)
        }
      }, 300)

      return () => clearTimeout(timer)
    })
  }, [modelSearchKey, items])

  const addRow = () => {
    setItems([
      ...items,
      {
        brand: "",
        model: "",
        productName: "",
        name: "",
        category: "",
        size: "M",
        quantity: 1,
        declared_value: "",
        weight_kg: "",
        width_cm: "",
        height_cm: "",
        depth_cm: "",
        is_fragile: false,
        requires_disassembly: false,
        showAdvanced: false,
      },
    ])
  }

  const removeRow = (index: number) => {
    setItems(items.filter((_, i) => i !== index))
  }

  const updateItem = (index: number, field: keyof ManualItem, value: any) => {
    const newItems = [...items]
    newItems[index] = { ...newItems[index], [field]: value }

    // Auto-fill name from brand + model
    if (field === "brand" || field === "model") {
      const item = newItems[index]
      if (item.brand && item.model) {
        // Auto-generate name if not already set or if it was auto-generated before
        if (!item.name || item.name === `${item.brand} ${item.model}` || item.name === newItems[index].name) {
          newItems[index].name = `${item.brand} ${item.model}`
        }
      }
    }

    // Auto-fill from model suggestions
    if (field === "model" && value) {
      const suggestion = modelSuggestions[index]?.find((s) => s.model === value)
      if (suggestion) {
        newItems[index].modelId = suggestion.modelId
        newItems[index].productName = suggestion.productName || ""
        // Only auto-fill name if it's empty or matches the old pattern
        if (!newItems[index].name || newItems[index].name === `${items[index].brand} ${items[index].model}`) {
          newItems[index].name = suggestion.productName || `${suggestion.brand} ${suggestion.model}`
        }
        
        if (suggestion.weightKg) {
          newItems[index].weight_kg = suggestion.weightKg.toString()
        }
        
        if (suggestion.dimensionsMm) {
          try {
            const dims = JSON.parse(suggestion.dimensionsMm)
            newItems[index].width_cm = (dims.width / 10).toString()
            newItems[index].height_cm = (dims.height / 10).toString()
            newItems[index].depth_cm = (dims.depth / 10).toString()
          } catch (e) {
            console.error("Failed to parse dimensions", e)
          }
        }
      }
    }

    setItems(newItems)
  }

  const toggleAdvanced = (index: number) => {
    updateItem(index, "showAdvanced", !items[index].showAdvanced)
  }

  const handleSubmit = async () => {
    // Validate
    const validationErrors: string[] = []
    items.forEach((item, index) => {
      // Nếu không có brand, name là bắt buộc
      if (!item.brand.trim() && !item.name.trim()) {
        validationErrors.push(`Vật phẩm #${index + 1}: Vui lòng nhập tên vật phẩm`)
      }
      if (item.quantity < 1) {
        validationErrors.push(`Vật phẩm #${index + 1}: Số lượng phải từ 1 trở lên`)
      }
      // Nếu có brand nhưng không có model, cần model hoặc name
      if (item.brand.trim() && !item.model.trim() && !item.name.trim()) {
        validationErrors.push(`Vật phẩm #${index + 1}: Vui lòng nhập model hoặc tên vật phẩm`)
      }
    })

    if (validationErrors.length > 0) {
      const { toast } = await import("sonner")
      toast.error("Vui lòng kiểm tra lại", {
        description: validationErrors.join("\n"),
      })
      return
    }

    const validItems = items.filter(
      (item) => (item.name.trim() || item.brand.trim()) && item.quantity > 0
    )

    if (validItems.length === 0) {
      const { toast } = await import("sonner")
      toast.error("Chưa có vật phẩm hợp lệ")
      return
    }

    const candidates: ItemCandidate[] = validItems.map((item, index) => ({
      id: `manual-${Date.now()}-${index}`,
      name: item.name || `${item.brand} ${item.model}`.trim() || "Vật phẩm",
      category_id: null,
      category_name: item.category || null,
      size: item.size,
      weight_kg: item.weight_kg ? parseFloat(item.weight_kg) : null,
      dimensions:
        item.width_cm || item.height_cm || item.depth_cm
          ? {
              width_cm: item.width_cm ? parseFloat(item.width_cm) : null,
              height_cm: item.height_cm ? parseFloat(item.height_cm) : null,
              depth_cm: item.depth_cm ? parseFloat(item.depth_cm) : null,
            }
          : null,
      quantity: item.quantity,
      is_fragile: item.is_fragile,
      requires_disassembly: item.requires_disassembly,
      requires_packaging: false,
      source: "manual",
      confidence: 1,
      image_url: null,
      notes: item.brand && item.model ? `${item.brand} ${item.model}` : null,
      metadata: {
        model_id: item.modelId || null,
        brand: item.brand || null,
        model: item.model || null,
        declared_value: item.declared_value ? parseFloat(item.declared_value) : null,
      },
    }))

    // Save new models to DB (for brand/model autocomplete)
    for (const item of validItems) {
      if (item.brand && item.model && !item.modelId) {
        try {
          await apiClient.saveProductModel({
            brand: item.brand,
            model: item.model,
            productName: item.productName || item.name || undefined,
            weightKg: item.weight_kg ? parseFloat(item.weight_kg) : undefined,
            dimensionsMm:
              item.width_cm || item.height_cm || item.depth_cm
                ? JSON.stringify({
                    width: parseFloat(item.width_cm || "0") * 10,
                    height: parseFloat(item.height_cm || "0") * 10,
                    depth: parseFloat(item.depth_cm || "0") * 10,
                  })
                : undefined,
            source: "manual_entry",
          })
        } catch (err) {
          console.error("Failed to save model", err)
        }
      } else if (item.modelId) {
        try {
          await apiClient.recordModelUsage(item.modelId)
        } catch (err) {
          console.error("Failed to record model usage", err)
        }
      }
    }

    onAddCandidates(candidates)

    if (showSuccessToast) {
      const { toast } = await import("sonner")
      const newModelsCount = validItems.filter(i => i.brand && i.model && !i.modelId).length
      const existingModelsCount = validItems.filter(i => i.modelId).length
      
      let successMessage = `Đã thêm ${validItems.length} vật phẩm`
      if (newModelsCount > 0) {
        successMessage += ` (${newModelsCount} model mới được lưu)`
      }
      if (existingModelsCount > 0) {
        successMessage += ` (${existingModelsCount} từ DB)`
      }
      
      toast.success(successMessage)
    }

    // Reset form
    setItems([
      {
        brand: "",
        model: "",
        productName: "",
        name: "",
        category: "",
        size: "M",
        quantity: 1,
        declared_value: "",
        weight_kg: "",
        width_cm: "",
        height_cm: "",
        depth_cm: "",
        is_fragile: false,
        requires_disassembly: false,
        showAdvanced: false,
      },
    ])
  }

  return (
    <div className="space-y-6">
      <div className="space-y-6">
        {items.map((item, index) => (
          <Card key={index} className="border">
              <CardContent className="p-6 space-y-6">
                <div className="flex items-center justify-between pb-3 border-b">
                  <div className="flex items-center gap-3">
                    <span className="text-sm font-medium text-muted-foreground">Vật phẩm #{index + 1}</span>
                    {item.modelId && <Badge variant="secondary" className="text-xs"><Sparkles className="h-3 w-3 mr-1" />Từ DB</Badge>}
                  </div>
                  {items.length > 1 && (
                    <Button variant="ghost" size="sm" onClick={() => removeRow(index)} className="h-8 w-8 p-0">
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  )}
                </div>

                {/* Brand & Model */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label className="text-sm font-medium">
                      Thương hiệu <span className="text-xs text-muted-foreground font-normal">(Tùy chọn)</span>
                    </Label>
                    <Combobox
                      options={brandOptions[index] || []}
                      value={item.brand}
                      onValueChange={(value) => updateItem(index, "brand", value)}
                      placeholder="Chọn hoặc nhập thương hiệu"
                      searchPlaceholder="Tìm hoặc nhập thương hiệu..."
                      emptyText="Nhập tên thương hiệu"
                      allowCustom
                    />
                    <p className="text-xs text-muted-foreground">
                      Chọn để gợi ý model
                    </p>
                  </div>

                  <div className="space-y-2">
                    <Label className="text-sm font-medium flex items-center gap-2">
                      Model
                      {item.brand && modelSuggestions[index]?.length > 0 && (
                        <Badge variant="secondary" className="text-xs">
                          {modelSuggestions[index].length} có sẵn
                        </Badge>
                      )}
                    </Label>
                    <Combobox
                      options={modelOptions[index] || []}
                      value={item.model}
                      onValueChange={(value) => updateItem(index, "model", value)}
                      placeholder={item.brand ? "Chọn hoặc nhập model" : "Chọn thương hiệu trước"}
                      searchPlaceholder="Tìm hoặc nhập model..."
                      emptyText={item.brand ? "Nhập tên model" : "Chọn thương hiệu trước"}
                      allowCustom
                      disabled={!item.brand}
                    />
                    {item.brand && item.model && !item.modelId && (
                      <Alert className="py-2">
                        <Info className="h-3 w-3" />
                        <AlertDescription className="text-xs">
                          Model mới sẽ được lưu để gợi ý cho khách hàng khác
                        </AlertDescription>
                      </Alert>
                    )}
                  </div>
                </div>

                {/* Basic info */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <div className="space-y-2 md:col-span-2">
                    <Label className="text-sm font-medium flex items-center gap-2">
                      Tên vật phẩm 
                      {!item.brand && <span className="text-destructive font-semibold">*</span>}
                      {item.brand && item.model && (
                        <Badge variant="outline" className="text-xs font-normal">
                          Tự động
                        </Badge>
                      )}
                    </Label>
                    <div className="space-y-2">
                      <Input
                        type="text"
                        placeholder="Nhập tên vật phẩm"
                        value={item.name}
                        onChange={(e) => updateItem(index, "name", e.target.value)}
                        className={!item.name && !item.brand ? "border-destructive border-2 focus-visible:ring-destructive/20" : ""}
                        disabled={!!(item.brand && item.model && item.modelId)}
                      />
                      {!item.name && !item.brand ? (
                        <div className="flex items-start gap-2 text-xs text-destructive font-semibold">
                          <AlertCircle className="h-4 w-4 mt-0.5 flex-shrink-0" />
                          <span>Vui lòng nhập tên vật phẩm</span>
                        </div>
                      ) : (
                        <p className="text-xs text-muted-foreground">
                          Ví dụ: Tủ lạnh 2 cánh Samsung
                        </p>
                      )}
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label className="text-sm font-medium">
                      Số lượng <span className="text-destructive font-semibold">*</span>
                    </Label>
                    <div className="space-y-2">
                      <Input
                        type="number"
                        min="1"
                        value={item.quantity}
                        onChange={(e) => updateItem(index, "quantity", parseInt(e.target.value) || 1)}
                        className={item.quantity < 1 ? "border-destructive border-2 focus-visible:ring-destructive/20" : ""}
                      />
                      {item.quantity < 1 && (
                        <div className="flex items-start gap-2 text-xs text-destructive font-semibold">
                          <AlertCircle className="h-4 w-4 mt-0.5 flex-shrink-0" />
                          <span>Số lượng phải từ 1 trở lên</span>
                        </div>
                      )}
                    </div>
                  </div>
                </div>

                {/* Declared Value */}
                <div className="space-y-2">
                  <Label className="text-sm font-medium">
                    Giá trị khai báo (VND) <span className="text-xs text-muted-foreground font-normal">(Cho bảo hiểm)</span>
                  </Label>
                  <div className="space-y-2">
                    <div className="relative">
                      <Input
                        type="text"
                        placeholder="₫ 0"
                        value={item.declared_value ? new Intl.NumberFormat('vi-VN').format(parseFloat(item.declared_value)) : ""}
                        onChange={(e) => {
                          const numericValue = e.target.value.replace(/[^\d]/g, '')
                          updateItem(index, "declared_value", numericValue)
                        }}
                        className="pl-8"
                      />
                      <span className="absolute left-3 top-1/2 -translate-y-1/2 text-sm text-muted-foreground pointer-events-none">₫</span>
                    </div>
                    <p className="text-xs text-muted-foreground">
                      Dùng để tính bảo hiểm
                    </p>
                  </div>
                </div>

                {/* Advanced toggle */}
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => toggleAdvanced(index)}
                  className="w-full justify-between hover:bg-muted"
                >
                  <span className="text-sm">Chi tiết nâng cao</span>
                  {item.showAdvanced ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
                </Button>

                {/* Advanced fields */}
                {item.showAdvanced && (
                  <div className="space-y-6 pt-6 border-t">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-3">
                        <Label className="text-sm font-medium">Loại</Label>
                        <Select value={item.category} onValueChange={(value) => updateItem(index, "category", value)}>
                          <SelectTrigger>
                            <SelectValue placeholder="Chọn loại" />
                          </SelectTrigger>
                          <SelectContent>
                            {CATEGORIES.map((cat) => (
                              <SelectItem key={cat} value={cat}>
                                {cat}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>

                      <div className="space-y-3">
                        <Label className="text-sm font-medium">Kích thước</Label>
                        <Select
                          value={item.size}
                          onValueChange={(value: "S" | "M" | "L") => updateItem(index, "size", value)}
                        >
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="S">S (Nhỏ)</SelectItem>
                            <SelectItem value="M">M (Vừa)</SelectItem>
                            <SelectItem value="L">L (Lớn)</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>

                      <div className="space-y-3">
                        <Label className="text-sm font-medium">Cân nặng (kg)</Label>
                        <Input
                          type="number"
                          step="0.1"
                          placeholder="VD: 50"
                          value={item.weight_kg}
                          onChange={(e) => updateItem(index, "weight_kg", e.target.value)}
                        />
                      </div>

                      <div className="space-y-3">
                        <Label className="text-sm font-medium">Kích thước (cm)</Label>
                        <div className="grid grid-cols-3 gap-2">
                          <Input
                            type="number"
                            placeholder="Rộng"
                            value={item.width_cm}
                            onChange={(e) => updateItem(index, "width_cm", e.target.value)}
                          />
                          <Input
                            type="number"
                            placeholder="Cao"
                            value={item.height_cm}
                            onChange={(e) => updateItem(index, "height_cm", e.target.value)}
                          />
                          <Input
                            type="number"
                            placeholder="Sâu"
                            value={item.depth_cm}
                            onChange={(e) => updateItem(index, "depth_cm", e.target.value)}
                          />
                        </div>
                      </div>
                    </div>

                    <div className="flex gap-4">
                      <div className="flex items-center space-x-2">
                        <Checkbox
                          id={`fragile-${index}`}
                          checked={item.is_fragile}
                          onCheckedChange={(checked) => updateItem(index, "is_fragile", checked)}
                        />
                        <Label htmlFor={`fragile-${index}`} className="text-sm cursor-pointer">
                          Dễ vỡ
                        </Label>
                      </div>

                      <div className="flex items-center space-x-2">
                        <Checkbox
                          id={`disassembly-${index}`}
                          checked={item.requires_disassembly}
                          onCheckedChange={(checked) => updateItem(index, "requires_disassembly", checked)}
                        />
                        <Label htmlFor={`disassembly-${index}`} className="text-sm cursor-pointer">
                          Cần tháo rời
                        </Label>
                      </div>
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          ))}
      </div>

      <div className="flex gap-3 pt-4 sticky bottom-0 bg-background/95 backdrop-blur-sm pb-2 border-t">
        <Button variant="outline" onClick={addRow} className="h-10">
          <Plus className="h-4 w-4 mr-2" />
          Thêm vật phẩm khác
        </Button>
        <Button onClick={handleSubmit} className="flex-1 bg-accent-green hover:bg-accent-green-dark h-10 font-semibold">
          {submitButtonText}
        </Button>
      </div>
    </div>
  )
}
