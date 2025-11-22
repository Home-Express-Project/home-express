"use client"

import { useState } from "react"
import Image from "next/image"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { ImageIcon, FileText, Edit3, Trash2, Save, Package, Check, X, Pencil } from "lucide-react"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Checkbox } from "@/components/ui/checkbox"
import type { ItemCandidate } from "@/types"
import { ImageUploadTab } from "./tabs/image-upload-tab"
import { PasteTextTab } from "./tabs/paste-text-tab"
import { EnhancedManualTab } from "./tabs/enhanced-manual-tab"
import { apiClient } from "@/lib/api-client"

interface IntakeCollectorProps {
  sessionId: number
  onContinue: (candidates: ItemCandidate[]) => void
  hideFooter?: boolean
  initialCandidates?: ItemCandidate[]
}

export function IntakeCollector({ 
  sessionId, 
  onContinue, 
  hideFooter = false,
  initialCandidates = []
}: IntakeCollectorProps) {
  const [candidates, setCandidates] = useState<ItemCandidate[]>(initialCandidates)
  const [activeTab, setActiveTab] = useState("manual")
  const [editingId, setEditingId] = useState<string | null>(null)

  function addCandidates(items: ItemCandidate[]) {
    const newCandidates = [...candidates, ...items]
    setCandidates(newCandidates)
    // Auto-update parent when hideFooter (embedded in multi-step form)
    if (hideFooter) {
      onContinue(newCandidates)
    }
  }

  function removeCandidate(id: string) {
    const newCandidates = candidates.filter((c) => c.id !== id)
    setCandidates(newCandidates)
    if (editingId === id) setEditingId(null)
    // Auto-update parent when hideFooter
    if (hideFooter) {
      onContinue(newCandidates)
    }
  }

  function updateCandidate(id: string, updates: Partial<ItemCandidate>) {
    const newCandidates = candidates.map(c => 
      c.id === id ? { ...c, ...updates } : c
    )
    setCandidates(newCandidates)
    if (hideFooter) {
      onContinue(newCandidates)
    }
  }

  function toggleEdit(id: string) {
    setEditingId(editingId === id ? null : id)
  }

  function handleContinue() {
    onContinue(candidates)
  }

  async function handleSaveToStorage() {
    if (candidates.length === 0) {
      const { toast } = await import("sonner")
      toast.error("Chưa có vật phẩm để lưu")
      return
    }

    try {
      const itemsToSave = candidates.map(c => ({
        name: c.name,
        brand: c.metadata && typeof c.metadata === 'object' && 'brand' in c.metadata ? String(c.metadata.brand) : null,
        model: c.metadata && typeof c.metadata === 'object' && 'model' in c.metadata ? String(c.metadata.model) : null,
        categoryId: c.category_id || null,
        size: c.size || null,
        weightKg: c.weight_kg || null,
        dimensions: c.dimensions ? JSON.stringify(c.dimensions) : null,
        declaredValueVnd: c.metadata && typeof c.metadata === 'object' && 'declared_value' in c.metadata 
          ? Number(c.metadata.declared_value) 
          : null,
        quantity: c.quantity,
        isFragile: c.is_fragile || false,
        requiresDisassembly: c.requires_disassembly || false,
        requiresPackaging: c.requires_packaging || false,
        notes: c.notes || null,
        metadata: c.metadata ? JSON.stringify(c.metadata) : null,
      }))

      const response = await apiClient.saveItemsToStorage(itemsToSave)
      const { toast } = await import("sonner")
      toast.success(response.message || `Đã lưu ${response.count} vật phẩm vào kho`)
      
      // Clear candidates after saving
      setCandidates([])
    } catch (error) {
      const { toast } = await import("sonner")
      toast.error("Không thể lưu vào kho")
      console.error(error)
    }
  }

  return (
    <div className="space-y-6">
      <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
        <TabsList className="grid w-full grid-cols-3 lg:grid-cols-3 gap-2 h-auto p-2 bg-muted/50">
          <TabsTrigger value="manual" className="flex items-center justify-center gap-2 py-3 px-4 text-sm min-w-0 whitespace-nowrap data-[state=active]:bg-background data-[state=active]:shadow-sm">
            <Edit3 className="h-4 w-4 flex-shrink-0" />
            <span className="hidden sm:inline truncate">Nhập thủ công</span>
            <span className="sm:hidden truncate">Thủ công</span>
          </TabsTrigger>
          <TabsTrigger value="image" className="flex items-center justify-center gap-2 py-3 px-4 text-sm min-w-0 whitespace-nowrap data-[state=active]:bg-background data-[state=active]:shadow-sm">
            <ImageIcon className="h-4 w-4 flex-shrink-0" />
            <span className="hidden sm:inline truncate">Chụp ảnh</span>
            <span className="sm:hidden truncate">Ảnh</span>
          </TabsTrigger>
          <TabsTrigger value="paste" className="flex items-center justify-center gap-2 py-3 px-4 text-sm min-w-0 whitespace-nowrap data-[state=active]:bg-background data-[state=active]:shadow-sm">
            <FileText className="h-4 w-4 flex-shrink-0" />
            <span className="hidden sm:inline truncate">Dán text</span>
            <span className="sm:hidden truncate">Text</span>
          </TabsTrigger>
        </TabsList>

        <div className="mt-8">
          <TabsContent value="manual" className="mt-0">
            <EnhancedManualTab onAddCandidates={addCandidates} />
          </TabsContent>

          <TabsContent value="image" className="mt-0">
            <ImageUploadTab sessionId={sessionId} onAddCandidates={addCandidates} />
          </TabsContent>

          <TabsContent value="paste" className="mt-0">
            <PasteTextTab onAddCandidates={addCandidates} />
          </TabsContent>
        </div>
      </Tabs>

      {/* Candidates list */}
      {candidates.length > 0 && (
        <Card className="border-2 shadow-sm">
          <CardContent className="p-6">
            <h3 className="font-semibold text-lg mb-4">Danh sách vật phẩm đã thu thập</h3>
            <div className="space-y-3 max-h-96 overflow-y-auto">
              {candidates.map((candidate) => (
                <Card key={candidate.id} className={editingId === candidate.id ? "border-primary border-2 shadow-md" : "border"}>
                  <CardContent className="p-4">
                    {editingId === candidate.id ? (
                      /* Edit mode */
                      <div className="space-y-3">
                        <div className="grid grid-cols-2 gap-3">
                          <div className="col-span-2 space-y-1">
                            <Label className="text-xs">Tên vật phẩm</Label>
                            <Input
                              value={candidate.name}
                              onChange={(e) => updateCandidate(candidate.id, { name: e.target.value })}
                              className="h-9"
                            />
                          </div>
                          <div className="space-y-1">
                            <Label className="text-xs">Số lượng</Label>
                            <Input
                              type="number"
                              min="1"
                              value={candidate.quantity}
                              onChange={(e) => updateCandidate(candidate.id, { quantity: parseInt(e.target.value) || 1 })}
                              className="h-9"
                            />
                          </div>
                          <div className="space-y-1">
                            <Label className="text-xs">Giá trị (VNĐ)</Label>
                            <Input
                              type="number"
                              step="100000"
                              value={(candidate.metadata as any)?.declared_value || ""}
                              onChange={(e) => updateCandidate(candidate.id, { 
                                metadata: { 
                                  ...(candidate.metadata || {}), 
                                  declared_value: e.target.value ? parseFloat(e.target.value) : null 
                                }
                              })}
                              placeholder="15000000"
                              className="h-9"
                            />
                          </div>
                        </div>
                        <div className="flex gap-3 text-sm">
                          <div className="flex items-center space-x-2">
                            <Checkbox
                              id={`edit-fragile-${candidate.id}`}
                              checked={candidate.is_fragile}
                              onCheckedChange={(checked) => updateCandidate(candidate.id, { is_fragile: checked as boolean })}
                            />
                            <Label htmlFor={`edit-fragile-${candidate.id}`} className="text-xs font-normal cursor-pointer">
                              Dễ vỡ
                            </Label>
                          </div>
                          <div className="flex items-center space-x-2">
                            <Checkbox
                              id={`edit-disassembly-${candidate.id}`}
                              checked={candidate.requires_disassembly}
                              onCheckedChange={(checked) => updateCandidate(candidate.id, { requires_disassembly: checked as boolean })}
                            />
                            <Label htmlFor={`edit-disassembly-${candidate.id}`} className="text-xs font-normal cursor-pointer">
                              Cần tháo lắp
                            </Label>
                          </div>
                        </div>
                        <div className="flex gap-2">
                          <Button variant="outline" size="sm" onClick={() => toggleEdit(candidate.id)} className="flex-1 h-8">
                            <Check className="h-3 w-3 mr-1" />
                            Xong
                          </Button>
                          <Button variant="ghost" size="sm" onClick={() => removeCandidate(candidate.id)} className="h-8">
                            <Trash2 className="h-3 w-3" />
                          </Button>
                        </div>
                      </div>
                    ) : (
                      /* View mode */
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3 flex-1">
                          {candidate.image_url && (
                            <Image
                              src={candidate.image_url || "/placeholder.svg"}
                              alt={candidate.name}
                              width={40}
                              height={40}
                              className="h-10 w-10 rounded object-cover"
                            />
                          )}
                          <div className="flex-1">
                            <div className="font-medium">{candidate.name}</div>
                            <div className="text-sm text-muted-foreground flex items-center gap-2 flex-wrap">
                              <span>SL: {candidate.quantity}</span>
                              {candidate.size && <Badge variant="outline" className="text-xs">{candidate.size}</Badge>}
                              {candidate.is_fragile && <Badge variant="destructive" className="text-xs">Dễ vỡ</Badge>}
                              {candidate.requires_disassembly && <Badge className="text-xs">Cần tháo lắp</Badge>}
                              {(candidate.metadata as any)?.declared_value && (
                                <Badge className="bg-green-100 text-green-800 text-xs">
                                  {new Intl.NumberFormat('vi-VN', { 
                                    style: 'currency', 
                                    currency: 'VND',
                                    maximumFractionDigits: 0,
                                    notation: 'compact'
                                  }).format((candidate.metadata as any).declared_value)}
                                </Badge>
                              )}
                              {candidate.confidence && (
                                <Badge variant="secondary" className="text-xs">{Math.round(candidate.confidence * 100)}%</Badge>
                              )}
                            </div>
                          </div>
                        </div>
                        <div className="flex gap-1">
                          <Button variant="ghost" size="sm" onClick={() => toggleEdit(candidate.id)}>
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button variant="ghost" size="sm" onClick={() => removeCandidate(candidate.id)}>
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </div>
                    )}
                  </CardContent>
                </Card>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Sticky footer - only show if not embedded */}
      {!hideFooter && (
        <div className="sticky bottom-0 bg-background border-t p-4 flex items-center justify-between shadow-lg rounded-t-lg">
          <div className="text-sm text-muted-foreground">
            Đã thu thập <span className="font-bold text-foreground">{candidates.length}</span> mục
          </div>
          <div className="flex gap-2">
            <Button
              onClick={handleSaveToStorage}
              disabled={candidates.length === 0}
              size="lg"
              variant="outline"
            >
              <Save className="h-4 w-4 mr-2" />
              Lưu vào kho
            </Button>
            <Button
              onClick={handleContinue}
              disabled={candidates.length === 0}
              size="lg"
              className="bg-accent-green hover:bg-accent-green-dark"
            >
              Tiếp tục → Xác nhận
            </Button>
          </div>
        </div>
      )}
    </div>
  )
}


