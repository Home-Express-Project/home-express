import React, { useMemo, useState } from 'react'
import { CATEGORIES } from '../lib/categories'
import { estimatePrice, formatVND } from '../lib/pricing.ts'

type Form = {
  description: string
  category: string
  material: string
  lengthCm: number | ''
  widthCm: number | ''
  heightCm: number | ''
  weightKg: number | ''
  distanceKm: number | ''
}

export function CreateOrderForm(){
  const [form, setForm] = useState<Form>({
    description: '',
    category: CATEGORIES[0]?.category ?? '',
    material: CATEGORIES[0]?.materials[0]?.name ?? '',
    lengthCm: '',
    widthCm: '',
    heightCm: '',
    weightKg: '',
    distanceKm: 5,
  })

  const mats = useMemo(() => {
    const cat = CATEGORIES.find(c => c.category === form.category)
    return cat?.materials ?? []
  }, [form.category])

  const factor = useMemo(() => {
    const m = mats.find(m => m.name === form.material)
    return m?.factor ?? 200
  }, [mats, form.material])

  const volumeM3 = useMemo(() => {
    const L = Number(form.lengthCm) || 0
    const W = Number(form.widthCm) || 0
    const H = Number(form.heightCm) || 0
    return (L * W * H) / 1_000_000
  }, [form.lengthCm, form.widthCm, form.heightCm])

  const volumetricKg = useMemo(() => volumeM3 * factor, [volumeM3, factor])
  const realWeightKg = Number(form.weightKg) || 0
  const billableWeightKg = useMemo(() => Math.max(realWeightKg, volumetricKg), [realWeightKg, volumetricKg])

  const price = useMemo(() => {
    const d = Number(form.distanceKm) || 0
    return estimatePrice({ distanceKm: d, billableWeightKg })
  }, [form.distanceKm, billableWeightKg])

  function update<K extends keyof Form>(k: K, v: Form[K]){
    setForm(prev => ({ ...prev, [k]: v }))
  }

  function onSubmit(e: React.FormEvent){
    e.preventDefault()
    // Đây là payload demo để gửi lên backend
    const payload = {
      description: form.description,
      category: form.category,
      material: form.material,
      dimensions_cm: {
        length: Number(form.lengthCm) || 0,
        width: Number(form.widthCm) || 0,
        height: Number(form.heightCm) || 0,
      },
      weight_kg: Number(form.weightKg) || 0,
      volume_m3: Number(volumeM3.toFixed(4)),
      volumetric_weight_kg: Number(volumetricKg.toFixed(2)),
      billable_weight_kg: Number(billableWeightKg.toFixed(2)),
      distance_km: Number(form.distanceKm) || 0,
      // price sẽ do backend chốt ở /api/pricing/estimate (ở đây demo tại FE)
      estimated_price_vnd: price,
    }
    alert('Payload submit (demo):\n' + JSON.stringify(payload, null, 2))
  }

  return (
    <form className="card" onSubmit={onSubmit}>
      <h2>Thông tin kiện hàng</h2>

      <div className="row">
        <div>
          <label>Mặt hàng / mô tả</label>
          <textarea
            placeholder="VD: Tủ lạnh 300L, bàn gỗ 1m6, quần áo 20kg..."
            value={form.description}
            onChange={(e)=>update('description', e.target.value)}
          />
        </div>
        <div>
          <label>Khoảng cách (km)</label>
          <input
            type="number"
            min={0}
            step="0.1"
            value={form.distanceKm}
            onChange={(e)=>update('distanceKm', (e.target.value === '' ? '' : Number(e.target.value)) as any)}
          />
          <div className="help">Dùng để ước tính giá (có thể lấy từ Map sau).</div>
        </div>
      </div>

      <div className="row">
        <div>
          <label>Loại hàng (Category)</label>
          <select
            value={form.category}
            onChange={(e)=>{
              const nextCat = e.target.value
              const firstMat = CATEGORIES.find(c=>c.category===nextCat)?.materials[0]?.name ?? ''
              setForm(prev=>({ ...prev, category: nextCat, material: firstMat }))
            }}
          >
            {CATEGORIES.map(c => <option value={c.category} key={c.category}>{c.category}</option>)}
          </select>
        </div>
        <div>
          <label>Chất liệu (Material)</label>
          <select
            value={form.material}
            onChange={(e)=>update('material', e.target.value)}
          >
            {mats.map(m => <option value={m.name} key={m.name}>{m.name}</option>)}
          </select>
          <div className="muted" style={{marginTop:6}}>Hệ số quy đổi: <span className="pill">{factor} kg/m³</span></div>
        </div>
      </div>

      <div className="row">
        <div>
          <label>Dài (cm)</label>
          <input
            type="number"
            min={0}
            value={form.lengthCm}
            onChange={(e)=>update('lengthCm', (e.target.value === '' ? '' : Number(e.target.value)) as any)}
          />
        </div>
        <div>
          <label>Rộng (cm)</label>
          <input
            type="number"
            min={0}
            value={form.widthCm}
            onChange={(e)=>update('widthCm', (e.target.value === '' ? '' : Number(e.target.value)) as any)}
          />
        </div>
      </div>

      <div className="row">
        <div>
          <label>Cao (cm)</label>
          <input
            type="number"
            min={0}
            value={form.heightCm}
            onChange={(e)=>update('heightCm', (e.target.value === '' ? '' : Number(e.target.value)) as any)}
          />
        </div>
        <div>
          <label>Cân nặng thực tế (kg)</label>
          <input
            type="number"
            min={0}
            step="0.1"
            value={form.weightKg}
            onChange={(e)=>update('weightKg', (e.target.value === '' ? '' : Number(e.target.value)) as any)}
          />
        </div>
      </div>

      <div className="hr"></div>

      <h2>Tính toán</h2>
      <div className="stat"><span>Thể tích (m³)</span><span className="v">{volumeM3.toFixed(4)}</span></div>
      <div className="stat"><span>Trọng lượng quy đổi (kg)</span><span className="v">{volumetricKg.toFixed(2)}</span></div>
      <div className="stat"><span>Khối lượng tính phí (kg)</span><span className="v">{billableWeightKg.toFixed(2)}</span></div>
      <div className="stat"><span>Giá ước tính</span><span className="v price">{formatVND(price)}</span></div>
      <div className="muted">* Giá ước tính chỉ mang tính tham khảo. Giá cuối cùng do backend xác nhận.</div>

      <div className="hr"></div>

      <div className="actions">
        <button type="submit">Tạo đơn (demo)</button>
        <button type="button" className="sec" onClick={()=>{
          // reset form
          setForm({
            description: '',
            category: CATEGORIES[0]?.category ?? '',
            material: CATEGORIES[0]?.materials[0]?.name ?? '',
            lengthCm: '',
            widthCm: '',
            heightCm: '',
            weightKg: '',
            distanceKm: 5,
          })
        }}>Làm mới</button>
      </div>
    </form>
  )
}
