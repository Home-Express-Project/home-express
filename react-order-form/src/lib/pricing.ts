// Đơn giá demo: có thể thay bằng API backend thực
export type EstimateInput = { distanceKm: number; billableWeightKg: number }

/**
 * Logic demo:
 * - Base fee: 20,000đ
 * - Price per km tier:
 *   0-5km: 10,000đ/km
 *   5-15km: 8,000đ/km
 *   >15km: 6,000đ/km
 * - Surcharge theo khối lượng tính phí:
 *   > 50kg: +30,000đ
 *   > 100kg: +70,000đ
 */
export function estimatePrice({ distanceKm, billableWeightKg }: EstimateInput){
  const base = 20000
  let perKm = 0
  if (distanceKm <= 5) perKm = 10000
  else if (distanceKm <= 15) perKm = 8000
  else perKm = 6000

  const travel = distanceKm * perKm
  let surcharge = 0
  if (billableWeightKg > 100) surcharge = 70000
  else if (billableWeightKg > 50) surcharge = 30000

  return Math.max(0, Math.round(base + travel + surcharge))
}

export function formatVND(v:number){
  return v.toLocaleString('vi-VN') + 'đ'
}
