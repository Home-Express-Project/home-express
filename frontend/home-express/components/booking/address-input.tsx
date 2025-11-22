"use client"

import { MapPin } from "lucide-react"

import { useVietnameseAddress } from "@/hooks/use-vietnamese-address"
import { cn } from "@/lib/utils"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Checkbox } from "@/components/ui/checkbox"

export interface AddressData {
  address: string
  province: string
  district: string
  ward: string
  contactName: string
  contactPhone: string
  floor: number | null
  hasElevator: boolean
}

interface AddressInputProps {
  label: string
  value: AddressData
  onChange: (value: AddressData) => void
  errors?: Partial<Record<keyof AddressData, string>>
  className?: string
}

export function AddressInput({ label, value, onChange, errors, className }: AddressInputProps) {
  const {
    provinces,
    districts,
    wards,
    isLoadingProvinces,
    isLoadingDistricts,
    isLoadingWards,
    provincesError,
    districtsError,
    wardsError,
  } = useVietnameseAddress(value.province, value.district)

  const updateAddress = (patch: Partial<AddressData>) => {
    onChange({ ...value, ...patch })
  }

  const handleProvinceChange = (code: string) => {
    updateAddress({
      province: code,
      district: "",
      ward: "",
    })
  }

  const handleDistrictChange = (code: string) => {
    updateAddress({
      district: code,
      ward: "",
    })
  }

  const handleWardChange = (code: string) => {
    updateAddress({ ward: code })
  }

  const provinceErrorMessage = provincesError ? "Không tải được danh sách tỉnh/thành. Vui lòng thử lại." : null
  const districtErrorMessage = districtsError ? "Không tải được danh sách quận/huyện. Vui lòng thử lại." : null
  const wardErrorMessage = wardsError ? "Không tải được danh sách phường/xã. Vui lòng thử lại." : null

  return (
    <div className={cn("space-y-4 rounded-lg border bg-card p-6", className)}>
      <div className="mb-4 flex items-center gap-2">
        <MapPin className="h-5 w-5 text-primary" />
        <h3 className="text-lg font-semibold">{label}</h3>
      </div>

      <div className="space-y-2">
        <Label htmlFor={`${label}-address`}>
          Số nhà, tên đường <span className="text-destructive">*</span>
        </Label>
        <Input
          id={`${label}-address`}
          placeholder="Ví dụ: 123 Nguyễn Huệ"
          value={value.address}
          onChange={(event) => updateAddress({ address: event.target.value })}
          className={cn(errors?.address && "border-destructive")}
        />
        {errors?.address && <p className="text-sm text-destructive">{errors.address}</p>}
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
        <div className="space-y-2">
          <Label htmlFor={`${label}-province`}>
            Tỉnh/Thành phố <span className="text-destructive">*</span>
          </Label>
          <Select
            value={value.province}
            onValueChange={handleProvinceChange}
            disabled={isLoadingProvinces}
          >
            <SelectTrigger id={`${label}-province`} className={cn(errors?.province && "border-destructive")}>
              <SelectValue placeholder="Chọn tỉnh/thành" />
            </SelectTrigger>
            <SelectContent>
              {provincesError ? (
                <SelectItem value="province-error" disabled>
                  Không tải được dữ liệu
                </SelectItem>
              ) : isLoadingProvinces ? (
                <SelectItem value="province-loading" disabled>
                  Đang tải...
                </SelectItem>
              ) : provinces.length > 0 ? (
                provinces.map((province) => (
                  <SelectItem key={province.code} value={province.code}>
                    {province.name}
                  </SelectItem>
                ))
              ) : (
                <SelectItem value="province-empty" disabled>
                  Không có dữ liệu
                </SelectItem>
              )}
            </SelectContent>
          </Select>
          {errors?.province && <p className="text-sm text-destructive">{errors.province}</p>}
          {provinceErrorMessage && <p className="text-sm text-destructive">{provinceErrorMessage}</p>}
        </div>

        <div className="space-y-2">
          <Label htmlFor={`${label}-district`}>
            Quận/Huyện <span className="text-destructive">*</span>
          </Label>
          <Select
            value={value.district}
            onValueChange={handleDistrictChange}
            disabled={!value.province || isLoadingDistricts}
          >
            <SelectTrigger id={`${label}-district`} className={cn(errors?.district && "border-destructive")}>
              <SelectValue placeholder="Chọn quận/huyện" />
            </SelectTrigger>
            <SelectContent>
              {!value.province ? (
                <SelectItem value="select-province" disabled>
                  Chọn tỉnh trước
                </SelectItem>
              ) : districtsError ? (
                <SelectItem value="district-error" disabled>
                  Không tải được dữ liệu
                </SelectItem>
              ) : isLoadingDistricts ? (
                <SelectItem value="district-loading" disabled>
                  Đang tải...
                </SelectItem>
              ) : districts.length > 0 ? (
                districts.map((district) => (
                  <SelectItem key={district.code} value={district.code}>
                    {district.name}
                  </SelectItem>
                ))
              ) : (
                <SelectItem value="district-empty" disabled>
                  Không có dữ liệu
                </SelectItem>
              )}
            </SelectContent>
          </Select>
          {errors?.district && <p className="text-sm text-destructive">{errors.district}</p>}
          {districtErrorMessage && <p className="text-sm text-destructive">{districtErrorMessage}</p>}
        </div>

        <div className="space-y-2">
          <Label htmlFor={`${label}-ward`}>
            Phường/Xã <span className="text-destructive">*</span>
          </Label>
          <Select
            value={value.ward}
            onValueChange={handleWardChange}
            disabled={!value.district || isLoadingWards}
          >
            <SelectTrigger id={`${label}-ward`} className={cn(errors?.ward && "border-destructive")}>
              <SelectValue placeholder="Chọn phường/xã" />
            </SelectTrigger>
            <SelectContent>
              {!value.district ? (
                <SelectItem value="select-district" disabled>
                  Chọn quận trước
                </SelectItem>
              ) : wardsError ? (
                <SelectItem value="ward-error" disabled>
                  Không tải được dữ liệu
                </SelectItem>
              ) : isLoadingWards ? (
                <SelectItem value="ward-loading" disabled>
                  Đang tải...
                </SelectItem>
              ) : wards.length > 0 ? (
                wards.map((ward) => (
                  <SelectItem key={ward.code} value={ward.code}>
                    {ward.name}
                  </SelectItem>
                ))
              ) : (
                <SelectItem value="ward-empty" disabled>
                  Không có dữ liệu
                </SelectItem>
              )}
            </SelectContent>
          </Select>
          {errors?.ward && <p className="text-sm text-destructive">{errors.ward}</p>}
          {wardErrorMessage && <p className="text-sm text-destructive">{wardErrorMessage}</p>}
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        <div className="space-y-2">
          <Label htmlFor={`${label}-contact-name`}>
            Người liên hệ <span className="text-destructive">*</span>
          </Label>
          <Input
            id={`${label}-contact-name`}
            value={value.contactName}
            onChange={(event) => updateAddress({ contactName: event.target.value })}
            className={cn(errors?.contactName && "border-destructive")}
          />
          {errors?.contactName && <p className="text-sm text-destructive">{errors.contactName}</p>}
        </div>

        <div className="space-y-2">
          <Label htmlFor={`${label}-contact-phone`}>
            Số điện thoại <span className="text-destructive">*</span>
          </Label>
          <Input
            id={`${label}-contact-phone`}
            value={value.contactPhone}
            onChange={(event) => updateAddress({ contactPhone: event.target.value })}
            className={cn(errors?.contactPhone && "border-destructive")}
          />
          {errors?.contactPhone && <p className="text-sm text-destructive">{errors.contactPhone}</p>}
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        <div className="space-y-2">
          <Label htmlFor={`${label}-floor`}>Tầng</Label>
          <Input
            id={`${label}-floor`}
            type="number"
            min={0}
            placeholder="0"
            value={value.floor ?? ""}
            onChange={(event) =>
              updateAddress({
                floor: event.target.value === "" ? null : Number(event.target.value),
              })
            }
          />
        </div>

        <div className="flex items-center gap-3 pt-6">
          <Checkbox
            id={`${label}-elevator`}
            checked={value.hasElevator}
            onCheckedChange={(checked) => updateAddress({ hasElevator: Boolean(checked) })}
          />
          <Label htmlFor={`${label}-elevator`} className="font-medium">
            Có thang máy
          </Label>
        </div>
      </div>
    </div>
  )
}
