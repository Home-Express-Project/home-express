import React from 'react'
import { CreateOrderForm } from './components/CreateOrderForm'

export default function App(){
  return (
    <div className="wrap">
      <div className="title">Tạo đơn hàng</div>
      <div className="subtitle">Nhập thông tin, kích thước 3 chiều & vật liệu — hệ thống sẽ tự tính khối tích, trọng lượng quy đổi và khối lượng tính phí.</div>
      <div className="grid">
        <CreateOrderForm />
        <div className="card">
          <h2>Hướng dẫn nhanh</h2>
          <ul className="muted">
            <li>• Thể tích (m³) = (Dài × Rộng × Cao) / 1,000,000</li>
            <li>• Trọng lượng quy đổi (kg) = Thể tích × Hệ số theo Category/Material</li>
            <li>• Khối lượng tính phí = max(Cân nặng thực tế, Trọng lượng quy đổi)</li>
          </ul>
          <div className="hr"></div>
          <div className="kicker">Gợi ý</div>
          <div className="flex" style={{marginTop:8}}>
            <span className="tag">Điện lạnh → 200 kg/m³</span>
            <span className="tag">Đồ gỗ → 150 kg/m³</span>
            <span className="tag">Quần áo → 100 kg/m³</span>
            <span className="tag">Dễ vỡ → 220 kg/m³</span>
          </div>
        </div>
      </div>
    </div>
  )
}
