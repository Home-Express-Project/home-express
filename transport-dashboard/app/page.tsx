"use client"

import { useState, useMemo } from "react"
import {
  ChevronLeft,
  ChevronRight,
  Eye,
  CheckCircle,
  XCircle,
  PlayCircle,
  PauseCircle,
  FileText,
  X,
} from "lucide-react"

type TransportStatus = "pending" | "rejected" | "active" | "inactive"

interface Vehicle {
  id: number
  type: string
  price: number
}

interface DistancePricing {
  first4km: number
  from5to40: number
  after40: number
}

interface Category {
  id: number
  name: string
  height: number
  weight: number
  price: number
}

interface Transport {
  id: string
  name: string
  email: string
  phone: string
  createdAt: string
  status: TransportStatus
  license: string
  bankQr: string
  income: number
  rating: number
  vehicles: Vehicle[]
  distancePricing: DistancePricing
  categories: Category[]
}

const mockTransports: Transport[] = [
  {
    id: "TR001",
    name: "Công ty TNHH Vận Tải ABC",
    email: "contact@abcvantai.com",
    phone: "0912345678",
    createdAt: "2023-10-15",
    status: "pending",
    license: "BSX-12345",
    bankQr: "qr_abc.png",
    income: 15000000,
    rating: 4.5,
    vehicles: [
      { id: 1, type: "Xe tải 1 tấn", price: 500000 },
      { id: 2, type: "Xe tải 2.5 tấn", price: 800000 },
    ],
    distancePricing: {
      first4km: 20000,
      from5to40: 15000,
      after40: 10000,
    },
    categories: [
      { id: 1, name: "Đồ điện tử", height: 0.5, weight: 5, price: 50000 },
      { id: 2, name: "Nội thất", height: 2, weight: 50, price: 200000 },
      { id: 3, name: "Đồ dễ vỡ", height: 0.3, weight: 10, price: 80000 },
    ],
  },
  {
    id: "TR002",
    name: "Dịch Vụ Vận Chuyển XYZ",
    email: "info@xyzservice.com",
    phone: "0923456789",
    createdAt: "2023-10-18",
    status: "active",
    license: "BSX-67890",
    bankQr: "qr_xyz.png",
    income: 25000000,
    rating: 4.8,
    vehicles: [
      { id: 3, type: "Xe tải 5 tấn", price: 1200000 },
      { id: 4, type: "Xe container", price: 3000000 },
    ],
    distancePricing: {
      first4km: 25000,
      from5to40: 18000,
      after40: 12000,
    },
    categories: [
      { id: 4, name: "Văn phòng phẩm", height: 0.2, weight: 3, price: 30000 },
      { id: 5, name: "Máy móc", height: 1.5, weight: 100, price: 350000 },
    ],
  },
  {
    id: "TR003",
    name: "Công Ty Vận Tải Minh Anh",
    email: "minhanh@vantai.com",
    phone: "0934567890",
    createdAt: "2023-10-20",
    status: "inactive",
    license: "BSX-54321",
    bankQr: "qr_minhanh.png",
    income: 35000000,
    rating: 4.9,
    vehicles: [
      { id: 5, type: "Xe tải 3.5 tấn", price: 900000 },
      { id: 6, type: "Xe tải 7 tấn", price: 1500000 },
    ],
    distancePricing: {
      first4km: 22000,
      from5to40: 16000,
      after40: 11000,
    },
    categories: [
      { id: 6, name: "Hàng hóa thông thường", height: 1, weight: 20, price: 70000 },
      { id: 7, name: "Hàng đông lạnh", height: 1.2, weight: 30, price: 150000 },
    ],
  },
  {
    id: "TR004",
    name: "Dịch Vụ Chuyển Nhà Hà Nội",
    email: "hanoimove@service.com",
    phone: "0945678901",
    createdAt: "2023-10-22",
    status: "rejected",
    license: "BSX-98765",
    bankQr: "qr_hanoi.png",
    income: 0,
    rating: 0,
    vehicles: [{ id: 7, type: "Xe tải 1.5 tấn", price: 600000 }],
    distancePricing: {
      first4km: 18000,
      from5to40: 14000,
      after40: 9000,
    },
    categories: [{ id: 8, name: "Đồ gia dụng", height: 0.8, weight: 15, price: 60000 }],
  },
  {
    id: "TR005",
    name: "Công Ty Vận Tải Phương Nam",
    email: "phuongnam@transport.com",
    phone: "0956789012",
    createdAt: "2023-10-25",
    status: "pending",
    license: "BSX-13579",
    bankQr: "qr_phuongnam.png",
    income: 0,
    rating: 0,
    vehicles: [
      { id: 8, type: "Xe tải 2 tấn", price: 700000 },
      { id: 9, type: "Xe tải 4 tấn", price: 1000000 },
    ],
    distancePricing: {
      first4km: 21000,
      from5to40: 15000,
      after40: 10000,
    },
    categories: [
      { id: 9, name: "Hàng hóa công nghiệp", height: 2.5, weight: 200, price: 500000 },
      { id: 10, name: "Vật liệu xây dựng", height: 1.8, weight: 80, price: 180000 },
    ],
  },
]

export default function TransportDashboard() {
  const [transports, setTransports] = useState<Transport[]>(mockTransports)
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [searchText, setSearchText] = useState("")
  const [dateFrom, setDateFrom] = useState("")
  const [dateTo, setDateTo] = useState("")
  const [isViewingApplications, setIsViewingApplications] = useState(false)
  const [currentPage, setCurrentPage] = useState(1)
  const [selectedTransport, setSelectedTransport] = useState<Transport | null>(null)
  const [confirmAction, setConfirmAction] = useState<{
    transportId: string
    action: "approve" | "reject" | "toggle"
    message: string
  } | null>(null)

  const itemsPerPage = 4

  const filteredTransports = useMemo(() => {
    return transports.filter((transport) => {
      if (isViewingApplications && transport.status !== "pending" && transport.status !== "rejected") {
        return false
      }

      if (statusFilter !== "all" && transport.status !== statusFilter) {
        return false
      }

      if (
        searchText &&
        !transport.id.toLowerCase().includes(searchText.toLowerCase()) &&
        !transport.name.toLowerCase().includes(searchText.toLowerCase()) &&
        !transport.email.toLowerCase().includes(searchText.toLowerCase())
      ) {
        return false
      }

      if (dateFrom && transport.createdAt < dateFrom) {
        return false
      }

      if (dateTo && transport.createdAt > dateTo) {
        return false
      }

      return true
    })
  }, [transports, statusFilter, searchText, dateFrom, dateTo, isViewingApplications])

  const paginatedTransports = useMemo(() => {
    const startIndex = (currentPage - 1) * itemsPerPage
    return filteredTransports.slice(startIndex, startIndex + itemsPerPage)
  }, [filteredTransports, currentPage])

  const totalPages = Math.ceil(filteredTransports.length / itemsPerPage)

  const stats = useMemo(() => {
    return {
      total: transports.length,
      active: transports.filter((t) => t.status === "active").length,
      pending: transports.filter((t) => t.status === "pending").length,
      rejected: transports.filter((t) => t.status === "rejected").length,
    }
  }, [transports])

  const getStatusBadge = (status: TransportStatus) => {
    const badges = {
      pending: { class: "bg-orange-100 text-orange-700", text: "Chờ duyệt" },
      rejected: { class: "bg-red-100 text-red-700", text: "Đã từ chối" },
      active: { class: "bg-green-100 text-green-700", text: "Đang hoạt động" },
      inactive: { class: "bg-gray-100 text-gray-700", text: "Ngừng hoạt động" },
    }
    const badge = badges[status]
    return (
      <span
        className={`px-4 py-2 rounded-full text-xs font-semibold inline-block min-w-[120px] text-center ${badge.class}`}
      >
        {badge.text}
      </span>
    )
  }

  const handleActionRequest = (transportId: string, action: "approve" | "reject" | "toggle") => {
    const transport = transports.find((t) => t.id === transportId)
    if (!transport) return

    let message = ""
    if (action === "approve") {
      message =
        transport.status === "rejected"
          ? `Bạn có chắc chắn muốn duyệt lại nhà cung cấp "${transport.name}"?`
          : `Bạn có chắc chắn muốn duyệt nhà cung cấp "${transport.name}"?`
    } else if (action === "reject") {
      message = `Bạn có chắc chắn muốn từ chối nhà cung cấp "${transport.name}"?`
    } else if (action === "toggle") {
      message =
        transport.status === "active"
          ? `Bạn có chắc chắn muốn ngừng hoạt động nhà cung cấp "${transport.name}"?`
          : `Bạn có chắc chắn muốn kích hoạt lại nhà cung cấp "${transport.name}"?`
    }

    setConfirmAction({ transportId, action, message })
  }

  const handleActionConfirm = () => {
    if (!confirmAction) return

    const { transportId, action } = confirmAction

    setTransports((prev) =>
      prev.map((transport) => {
        if (transport.id !== transportId) return transport

        switch (action) {
          case "approve":
            return { ...transport, status: "active" as TransportStatus }
          case "reject":
            return { ...transport, status: "rejected" as TransportStatus }
          case "toggle":
            return {
              ...transport,
              status: transport.status === "active" ? ("inactive" as TransportStatus) : ("active" as TransportStatus),
            }
          default:
            return transport
        }
      }),
    )

    setConfirmAction(null)
    setSelectedTransport(null)
  }

  return (
    <div className="min-h-screen bg-neutral-50 p-4 md:p-8">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8">
          <button
            className="w-10 h-10 rounded-full bg-neutral-100 hover:bg-neutral-200 flex items-center justify-center transition-all hover:-translate-x-1"
            onClick={() => window.history.back()}
          >
            <ChevronLeft className="w-5 h-5" />
          </button>

          <div className="flex items-center gap-3">
            <div className="w-12 h-12 bg-black text-white rounded-xl flex items-center justify-center">
              <span className="text-2xl">🏠</span>
            </div>
            <div className="text-xl font-black">HOME EXPRESS</div>
          </div>
        </div>

        <h1 className="text-4xl font-extrabold mb-2 bg-gradient-to-r from-black to-neutral-600 bg-clip-text text-transparent">
          Quản lý Nhà Cung Cấp Vận Chuyển
        </h1>
        <p className="text-neutral-600 mb-8">Theo dõi và quản lý tất cả các nhà cung cấp vận chuyển trong hệ thống</p>

        {/* Dashboard Container */}
        <div className="bg-white rounded-3xl shadow-xl p-6 md:p-10 mb-8">
          {/* Stats */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
            <div className="bg-neutral-50 rounded-2xl p-6 hover:-translate-y-1 transition-transform">
              <div className="text-4xl font-extrabold mb-2">{stats.total}</div>
              <div className="text-neutral-600 text-sm font-medium">Tổng số Nhà Cung Cấp</div>
            </div>
            <div className="bg-neutral-50 rounded-2xl p-6 hover:-translate-y-1 transition-transform">
              <div className="text-4xl font-extrabold mb-2">{stats.active}</div>
              <div className="text-neutral-600 text-sm font-medium">Đang hoạt động</div>
            </div>
            <div className="bg-neutral-50 rounded-2xl p-6 hover:-translate-y-1 transition-transform">
              <div className="text-4xl font-extrabold mb-2">{stats.pending}</div>
              <div className="text-neutral-600 text-sm font-medium">Chờ duyệt</div>
            </div>
            <div className="bg-neutral-50 rounded-2xl p-6 hover:-translate-y-1 transition-transform">
              <div className="text-4xl font-extrabold mb-2">{stats.rejected}</div>
              <div className="text-neutral-600 text-sm font-medium">Đã từ chối</div>
            </div>
          </div>

          {/* Filters */}
          <div className="flex flex-wrap gap-4 mb-8 items-center">
            <div className="flex items-center gap-2">
              <span className="font-semibold text-neutral-700 text-sm">Trạng thái:</span>
              <select
                className="px-4 py-2 rounded-lg border border-neutral-200 bg-white focus:outline-none focus:ring-2 focus:ring-black"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="all">Tất cả</option>
                <option value="pending">Chờ duyệt</option>
                <option value="rejected">Đã từ chối</option>
                <option value="active">Đang hoạt động</option>
                <option value="inactive">Ngừng hoạt động</option>
              </select>
            </div>

            <div className="flex items-center gap-2">
              <span className="font-semibold text-neutral-700 text-sm">Tìm kiếm:</span>
              <input
                type="text"
                className="px-4 py-2 rounded-lg border border-neutral-200 bg-white focus:outline-none focus:ring-2 focus:ring-black"
                placeholder="Tìm theo ID, tên..."
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
              />
            </div>

            <div className="flex items-center gap-2">
              <span className="font-semibold text-neutral-700 text-sm">Ngày đăng ký:</span>
              <input
                type="date"
                className="px-4 py-2 rounded-lg border border-neutral-200 bg-white focus:outline-none focus:ring-2 focus:ring-black"
                value={dateFrom}
                onChange={(e) => setDateFrom(e.target.value)}
              />
              <span>đến</span>
              <input
                type="date"
                className="px-4 py-2 rounded-lg border border-neutral-200 bg-white focus:outline-none focus:ring-2 focus:ring-black"
                value={dateTo}
                onChange={(e) => setDateTo(e.target.value)}
              />
            </div>

            <button
              className={`px-4 py-2 rounded-lg flex items-center gap-2 font-medium transition-colors ${
                isViewingApplications ? "bg-black text-white" : "bg-neutral-100 hover:bg-neutral-200"
              }`}
              onClick={() => setIsViewingApplications(!isViewingApplications)}
            >
              <FileText className="w-4 h-4" />
              {isViewingApplications ? "Xem tất cả" : "Xem đơn"}
            </button>
          </div>

          {/* Table */}
          <div className="overflow-x-auto mb-8">
            <table className="w-full">
              <thead>
                <tr className="border-b-2 border-neutral-100">
                  <th className="text-left p-4 font-semibold text-neutral-700">ID</th>
                  <th className="text-left p-4 font-semibold text-neutral-700">Tên Nhà Cung Cấp</th>
                  <th className="text-left p-4 font-semibold text-neutral-700">Email</th>
                  <th className="text-left p-4 font-semibold text-neutral-700">Số điện thoại</th>
                  <th className="text-left p-4 font-semibold text-neutral-700">Ngày đăng ký</th>
                  <th className="text-left p-4 font-semibold text-neutral-700">Trạng thái</th>
                  <th className="text-left p-4 font-semibold text-neutral-700">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {paginatedTransports.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="text-center p-8 text-neutral-500">
                      Không tìm thấy nhà cung cấp nào phù hợp
                    </td>
                  </tr>
                ) : (
                  paginatedTransports.map((transport) => (
                    <tr
                      key={transport.id}
                      className="border-b border-neutral-50 hover:bg-neutral-50 transition-all cursor-pointer"
                      onClick={() => setSelectedTransport(transport)}
                    >
                      <td className="p-4">{transport.id}</td>
                      <td className="p-4">{transport.name}</td>
                      <td className="p-4">{transport.email}</td>
                      <td className="p-4">{transport.phone}</td>
                      <td className="p-4">{transport.createdAt}</td>
                      <td className="p-4">{getStatusBadge(transport.status)}</td>
                      <td className="p-4">
                        <button
                          className="px-4 py-2 rounded-lg bg-blue-50 text-blue-700 hover:bg-blue-100 flex items-center gap-2 font-semibold transition-colors"
                          onClick={(e) => {
                            e.stopPropagation()
                            setSelectedTransport(transport)
                          }}
                        >
                          <Eye className="w-4 h-4" />
                          Xem
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          <div className="flex justify-center gap-2">
            <button
              className={`w-10 h-10 rounded-lg border border-neutral-200 flex items-center justify-center transition-colors ${
                currentPage === 1 ? "opacity-50 cursor-not-allowed" : "hover:bg-neutral-50"
              }`}
              onClick={() => setCurrentPage((prev) => Math.max(1, prev - 1))}
              disabled={currentPage === 1}
            >
              <ChevronLeft className="w-5 h-5" />
            </button>

            {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
              <button
                key={page}
                className={`w-10 h-10 rounded-lg border font-semibold transition-colors ${
                  currentPage === page ? "bg-black text-white border-black" : "border-neutral-200 hover:bg-neutral-50"
                }`}
                onClick={() => setCurrentPage(page)}
              >
                {page}
              </button>
            ))}

            <button
              className={`w-10 h-10 rounded-lg border border-neutral-200 flex items-center justify-center transition-colors ${
                currentPage === totalPages ? "opacity-50 cursor-not-allowed" : "hover:bg-neutral-50"
              }`}
              onClick={() => setCurrentPage((prev) => Math.min(totalPages, prev + 1))}
              disabled={currentPage === totalPages}
            >
              <ChevronRight className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>

      {/* Detail Modal */}
      {selectedTransport && (
        <div
          className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 md:p-8"
          onClick={() => setSelectedTransport(null)}
        >
          <div
            className="bg-white rounded-3xl p-6 md:p-10 max-w-4xl w-full max-h-[90vh] overflow-y-auto"
            onClick={(e) => e.stopPropagation()}
          >
            {/* Modal Header */}
            <div className="flex justify-between items-center mb-8">
              <h2 className="text-2xl font-bold">Chi tiết Nhà Cung Cấp</h2>
              <button
                className="text-neutral-500 hover:text-black transition-colors"
                onClick={() => setSelectedTransport(null)}
              >
                <X className="w-6 h-6" />
              </button>
            </div>

            {/* Transport Details */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
              <div>
                <div className="text-sm font-semibold text-neutral-600 mb-2">ID Nhà Cung Cấp</div>
                <div className="text-base">{selectedTransport.id}</div>
              </div>
              <div>
                <div className="text-sm font-semibold text-neutral-600 mb-2">Tên Nhà Cung Cấp</div>
                <div className="text-base">{selectedTransport.name}</div>
              </div>
              <div>
                <div className="text-sm font-semibold text-neutral-600 mb-2">Email</div>
                <div className="text-base">{selectedTransport.email}</div>
              </div>
              <div>
                <div className="text-sm font-semibold text-neutral-600 mb-2">Số điện thoại</div>
                <div className="text-base">{selectedTransport.phone}</div>
              </div>
              <div>
                <div className="text-sm font-semibold text-neutral-600 mb-2">Ngày đăng ký</div>
                <div className="text-base">{selectedTransport.createdAt}</div>
              </div>
              <div>
                <div className="text-sm font-semibold text-neutral-600 mb-2">Trạng thái</div>
                <div>{getStatusBadge(selectedTransport.status)}</div>
              </div>
              <div>
                <div className="text-sm font-semibold text-neutral-600 mb-2">Giấy phép kinh doanh</div>
                <div className="text-base">{selectedTransport.license}</div>
              </div>
              <div>
                <div className="text-sm font-semibold text-neutral-600 mb-2">Mã QR Ngân hàng</div>
                <div className="text-base">{selectedTransport.bankQr}</div>
              </div>
              {(selectedTransport.status === "active" || selectedTransport.status === "inactive") && (
                <>
                  <div>
                    <div className="text-sm font-semibold text-neutral-600 mb-2">Thu nhập</div>
                    <div className="text-base">{selectedTransport.income.toLocaleString()} VNĐ</div>
                  </div>
                  <div>
                    <div className="text-sm font-semibold text-neutral-600 mb-2">Đánh giá</div>
                    <div className="text-base">{selectedTransport.rating}/5</div>
                  </div>
                </>
              )}
              <div>
                <div className="text-sm font-semibold text-neutral-600 mb-2">Loại xe và giá cơ bản</div>
                <div className="text-base">
                  {selectedTransport.vehicles.map((vehicle) => (
                    <div key={vehicle.id}>
                      {vehicle.type}: {vehicle.price.toLocaleString()} VNĐ
                    </div>
                  ))}
                </div>
              </div>
              <div>
                <div className="text-sm font-semibold text-neutral-600 mb-2">Giá theo khoảng cách</div>
                <div className="text-base">
                  <div>4km đầu: {selectedTransport.distancePricing.first4km.toLocaleString()} VNĐ</div>
                  <div>Từ 5-40km: {selectedTransport.distancePricing.from5to40.toLocaleString()} VNĐ/km</div>
                  <div>Sau 40km: {selectedTransport.distancePricing.after40.toLocaleString()} VNĐ/km</div>
                </div>
              </div>
            </div>

            {/* Categories */}
            {selectedTransport.categories.length > 0 && (
              <div className="pt-8 border-t border-neutral-100">
                <h3 className="text-xl font-bold mb-6">Danh mục đồ đạc và giá</h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                  {selectedTransport.categories.map((category) => (
                    <div
                      key={category.id}
                      className="bg-neutral-50 rounded-xl p-5 hover:-translate-y-1 hover:shadow-lg transition-all"
                    >
                      <div className="font-semibold mb-3">{category.name}</div>
                      <div className="text-sm text-neutral-600 space-y-1">
                        <div>Chiều cao: {category.height}m</div>
                        <div>Cân nặng: {category.weight}kg</div>
                        <div>Giá: {category.price.toLocaleString()} VNĐ</div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Actions */}
            <div className="flex flex-col sm:flex-row gap-4 justify-end pt-8 mt-8 border-t border-neutral-100">
              {selectedTransport.status === "pending" && (
                <>
                  <button
                    className="px-6 py-3 rounded-lg bg-green-500 text-white hover:bg-green-600 flex items-center justify-center gap-2 font-semibold transition-colors"
                    onClick={() => handleActionRequest(selectedTransport.id, "approve")}
                  >
                    <CheckCircle className="w-5 h-5" />
                    Duyệt
                  </button>
                  <button
                    className="px-6 py-3 rounded-lg bg-red-500 text-white hover:bg-red-600 flex items-center justify-center gap-2 font-semibold transition-colors"
                    onClick={() => handleActionRequest(selectedTransport.id, "reject")}
                  >
                    <XCircle className="w-5 h-5" />
                    Từ chối
                  </button>
                </>
              )}
              {selectedTransport.status === "rejected" && (
                <button
                  className="px-6 py-3 rounded-lg bg-green-500 text-white hover:bg-green-600 flex items-center justify-center gap-2 font-semibold transition-colors"
                  onClick={() => handleActionRequest(selectedTransport.id, "approve")}
                >
                  <CheckCircle className="w-5 h-5" />
                  Duyệt lại
                </button>
              )}
              {(selectedTransport.status === "active" || selectedTransport.status === "inactive") && (
                <button
                  className={`px-6 py-3 rounded-lg flex items-center justify-center gap-2 font-semibold transition-colors ${
                    selectedTransport.status === "active"
                      ? "bg-orange-500 text-white hover:bg-orange-600"
                      : "bg-green-500 text-white hover:bg-green-600"
                  }`}
                  onClick={() => handleActionRequest(selectedTransport.id, "toggle")}
                >
                  {selectedTransport.status === "active" ? (
                    <>
                      <PauseCircle className="w-5 h-5" />
                      Ngừng hoạt động
                    </>
                  ) : (
                    <>
                      <PlayCircle className="w-5 h-5" />
                      Kích hoạt
                    </>
                  )}
                </button>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Confirmation Dialog */}
      {confirmAction && (
        <div
          className="fixed inset-0 bg-black/50 flex items-center justify-center z-[60] p-4"
          onClick={() => setConfirmAction(null)}
        >
          <div className="bg-white rounded-2xl p-8 max-w-md w-full" onClick={(e) => e.stopPropagation()}>
            <h3 className="text-xl font-bold mb-4">Xác nhận thay đổi</h3>
            <p className="text-neutral-600 mb-6">{confirmAction.message}</p>
            <div className="flex gap-4">
              <button
                className="flex-1 px-6 py-3 rounded-lg bg-neutral-100 hover:bg-neutral-200 font-semibold transition-colors"
                onClick={() => setConfirmAction(null)}
              >
                Hủy
              </button>
              <button
                className="flex-1 px-6 py-3 rounded-lg bg-black text-white hover:bg-neutral-800 font-semibold transition-colors"
                onClick={handleActionConfirm}
              >
                Xác nhận
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
