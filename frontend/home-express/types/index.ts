/**
 * Shared TypeScript type definitions
 */

// ============================================================================
// EVIDENCE TYPES
// ============================================================================

export type EvidenceType =
  | "PICKUP_PHOTO"
  | "DELIVERY_PHOTO"
  | "DAMAGE_PHOTO"
  | "SIGNATURE"
  | "INVOICE"
  | "OTHER"

export type FileType = "IMAGE" | "VIDEO" | "DOCUMENT"

export interface BookingEvidence {
  evidenceId: number
  bookingId: number
  uploadedByUserId: number
  uploaderName?: string
  uploaderRole?: string
  evidenceType: EvidenceType
  fileType: FileType
  fileUrl: string
  fileName: string
  mimeType?: string
  fileSizeBytes?: number
  description?: string
  uploadedAt: string
}

export interface UploadEvidenceRequest {
  evidenceType: EvidenceType
  fileType: FileType
  fileUrl: string
  fileName: string
  mimeType?: string
  fileSizeBytes?: number
  description?: string
}

// ============================================================================
// PAYMENT TYPES
// ============================================================================

export type PaymentStatus = "PENDING" | "PROCESSING" | "COMPLETED" | "FAILED" | "REFUNDED"

export type PaymentMethod = "CASH" | "BANK_TRANSFER"

/**
 * Frontend-facing payment method values used in UI and request payloads.
 * These map to backend PaymentMethod enum values via the API client.
 */
export type PaymentMethodOption = "cash" | "bank"

/**
 * Payment method values as persisted in the database / returned by backend.
 */
export type PaymentMethodResponse = PaymentMethod

export type PaymentType = "DEPOSIT" | "REMAINING_PAYMENT" | "TIP" | "REFUND"

export interface PaymentStatusResponse {
  success: boolean
  status: PaymentStatus
  message?: string
}

export interface InitiateDepositRequest {
  bookingId: number
  paymentMethod: PaymentMethodOption
}

export interface InitiateDepositResponse {
  success: boolean
  paymentId?: string
  paymentUrl?: string
  bookingId: number
  message?: string
}

export interface InitiateRemainingPaymentRequest {
  bookingId: number
  paymentMethod: PaymentMethodOption
  tipAmountVnd?: number
  returnUrl?: string
  cancelUrl?: string
}

export interface InitiateRemainingPaymentResponse {
  success: boolean
  paymentId?: string
  paymentUrl?: string
  bookingId: number
  remainingAmountVnd?: number
  tipAmountVnd?: number
  totalAmountVnd?: number
  message?: string
}

export interface ConfirmCompletionRequest {
  feedback?: string
  rating?: number
}

// ============================================================================
// EXPORT TYPES
// ============================================================================

export type ExportFormat = "CSV" | "PDF" | "EXCEL"

export interface BookingExportRequest {
  startDate?: string
  endDate?: string
  status?: BookingStatus
  transportId?: number
  sortBy?: string
  sortOrder?: "ASC" | "DESC"
  format: ExportFormat
}

export interface BookingExportResponse {
  filename: string
  contentType: string
  fileSize: number
  recordCount: number
  generatedAt: string
  downloadUrl?: string
}

export interface BankInfo {
  bank: string
  accountNumber: string
  accountName: string
  branch?: string
}

// ============================================================================
// BIDDING / QUOTATION UI TYPES
// ============================================================================

export interface Bid {
  id: number
  transporterId: number
  transporterName: string
  transporterAvatar?: string
  rating: number
  completedJobs: number
  amount: number
  note?: string
  estimatedArrival: string
  vehicleType: string
  createdAt: string
}

// ============================================================================
// USER TYPES
// ============================================================================
export interface User {
  user_id: number
  email: string
  role: "CUSTOMER" | "TRANSPORT" | "MANAGER"
  is_active: boolean
  is_verified: boolean
}

export interface Customer {
  customer_id: number
  full_name: string
  phone: string
  address: string | null
  date_of_birth: string | null
  avatar_url: string | null
  preferred_language: string
  created_at: string
  updated_at: string
}

export interface Transport {
  transport_id: number
  company_name: string
  business_license_number: string
  tax_code: string
  phone: string
  address: string
  city: string
  district: string | null
  ward: string | null
  verification_status: "PENDING" | "APPROVED" | "REJECTED"
  verified_at: string | null
  verified_by: number | null
  total_bookings: number
  completed_bookings: number
  cancelled_bookings: number
  average_rating: number
  national_id_number: string | null
  national_id_type: "CMND" | "CCCD" | null
  bank_code: string | null
  bank_name: string | null
  bank_account_number: string | null
  bank_account_holder: string | null
  created_at: string
  updated_at: string
}

export interface Manager {
  manager_id: number
  full_name: string
  phone: string
  employee_id: string | null
  department: string | null
  permissions: string[] | null
  created_at: string
  updated_at: string
}

// Booking types
export type BookingStatus = "PENDING" | "QUOTED" | "CONFIRMED" | "IN_PROGRESS" | "COMPLETED" | "CONFIRMED_BY_CUSTOMER" | "REVIEWED" | "CANCELLED"

export interface Booking {
  booking_id: number
  customer_id: number
  transport_id: number | null
  status: BookingStatus
  pickup_address: string
  delivery_address: string
  pickup_date: string
  estimated_price: number | null
  final_price: number | null
  created_at: string
  updated_at: string
}

// Dashboard stats
export interface CustomerStats {
  total_bookings: number
  pending_bookings: number
  completed_bookings: number
  cancelled_bookings: number
  total_spent: number
}

export interface AdminDashboardStats {
  total_users: number
  total_customers: number
  total_transports: number
  total_managers: number
  active_users: number
  inactive_users: number
  verified_users: number
  new_users_today: number
  new_users_this_week: number
  new_users_this_month: number
  user_growth_rate: string
  top_transports: Array<{
    transport_id: number
    company_name: string
    average_rating: number
    completed_bookings: number
  }>
  pending_transport_verifications: number
}

export interface TransportStats {
  total_income: number
  completed_bookings: number
  in_progress_bookings: number
  average_rating: number
  completion_rate: number
  pending_quotations: number
  monthly_revenue: Array<{
    month: string
    revenue: number
  }>
}

export type ExportJobStatus = "PENDING" | "PROCESSING" | "COMPLETED" | "FAILED"

export interface TransportExportJob {
  job_id: number
  report_type: string
  status: ExportJobStatus
  format: string
  rows_exported: number | null
  created_at: string
  completed_at: string | null
  download_token?: string | null
  failure_reason?: string | null
}

export interface EarningsStats {
  total_earnings: number
  this_month_earnings: number
  this_month_bookings: number
  pending_amount: number
  pending_transactions: number
  average_per_booking: number
  total_bookings: number
  growth_rate: string
  monthly_breakdown: Array<{
    month: string
    revenue: number
    bookings: number
  }>
}

export interface Transaction {
  transaction_id: string
  booking_id: number
  customer_name: string
  amount: number
  status: "COMPLETED" | "PENDING" | "FAILED"
  payment_method: string
  created_at: string
  expected_date: string
}

export interface Quotation {
  quotation_id: number
  booking_id: number
  transport_id: number
  customer_name: string
  pickup_address: string
  delivery_address: string
  pickup_date: string
  estimated_price: number
  status:
    | "PENDING"
    | "ACCEPTED"
    | "REJECTED"
    | "EXPIRED"
    | "NEGOTIATING"
    | "COUNTERED"
    | "WITHDRAWN"
  created_at: string
}

export interface TransportQuotationSummary {
  quotation_id: number
  booking_id: number
  pickup_location: string
  delivery_location: string
  preferred_date: string
  my_quote_price: number
  status: string
  competitor_quotes_count: number
  lowest_competitor_price: number | null
  my_rank: number
  expires_at: string
  submitted_at: string
}

// Rate cards for transport pricing (Stage 3.2)
export interface RateCardAdditionalRules {
  fragile_multiplier?: number
  disassembly_multiplier?: number
  heavy_item_multiplier?: number
}

export interface RateCard {
  rate_card_id: number
  transport_id: number
  category_id: number
  category_name?: string | null
  base_price: number | null
  price_per_km: number | null
  price_per_hour: number | null
  minimum_charge: number | null
  valid_from: string | null
  valid_until: string | null
  is_active: boolean
  additional_rules: RateCardAdditionalRules | null
  created_at: string
  updated_at: string
}

export interface RateCardRequest {
  category_id: number
  base_price: number
  price_per_km: number
  price_per_hour: number
  minimum_charge: number
  valid_from: string
  valid_until: string
  additional_rules?: RateCardAdditionalRules | null
}

export interface SuggestedPriceResponse {
  suggested_price: number
  price_breakdown: {
    base_price: number
    distance_price: number
    time_price: number
    multipliers: Record<string, number>
    minimum_charge_applied: boolean
  }
  rate_card_id: number
  category_id: number
  calculation_timestamp: string
}


export interface ReadyToQuoteStatus {
  ready_to_quote: boolean
  reason: string | null
  rate_cards_count: number
  expired_cards_count: number
  next_expiry_at: string | null
}



// Address structure (Vietnamese hierarchical)
export interface Address {
  addressLine: string
  provinceCode: string | null
  districtCode: string | null
  wardCode: string | null
  latitude: number | null
  longitude: number | null
  contactName: string
  contactPhone: string
  floor: number | null
  hasElevator: boolean
}

// Vietnamese administrative divisions (options for selects/lookups)
export interface ProvinceOption {
  code: string
  name: string
}

export interface DistrictOption {
  code: string
  name: string
  provinceCode: string
}

export interface WardOption {
  code: string
  name: string
  districtCode: string
}

// Full booking with all details
export interface BookingDetail {
  booking_id: number
  customer_id: number
  transport_id: number | null

  // Pickup Address
  pickup_address_line: string
  pickup_province_code: string | null
  pickup_district_code: string | null
  pickup_ward_code: string | null
  pickup_latitude: number | null
  pickup_longitude: number | null
  pickup_contact_name: string
  pickup_contact_phone: string
  pickup_floor: number | null
  pickup_has_elevator: boolean

  // Delivery Address
  delivery_address_line: string
  delivery_province_code: string | null
  delivery_district_code: string | null
  delivery_ward_code: string | null
  delivery_latitude: number | null
  delivery_longitude: number | null
  delivery_contact_name: string
  delivery_contact_phone: string
  delivery_floor: number | null
  delivery_has_elevator: boolean

  // Distance & Pricing
  distance_km: number | null
  distance_source: string | null
  estimated_price: number | null
  final_price: number | null

  // Scheduling
  preferred_date: string
  preferred_time_slot: string
  scheduled_datetime: string | null
  completed_datetime: string | null

  // Status
  status: string

  // Cancellation
  cancelled_by: number | null
  cancelled_at: string | null
  cancellation_reason: string | null

  // Additional
  notes: string | null
  special_requirements: string | null
  metadata: any | null

  created_at: string
  updated_at: string
}

// Booking item
export interface BookingItem {
  item_id: number
  booking_id: number
  category_id: number
  category_name?: string

  name: string
  quantity: number
  weight: number | null
  volume: number | null

  is_fragile: boolean
  requires_disassembly: boolean
  requires_packaging: boolean

  image_urls: string[] | null
  notes: string | null
  created_at: string
}

// Item category
export interface Category {
  category_id: number
  name: string
  name_en: string
  icon: string
  base_weight: number
  base_volume: number
  requires_special_handling: boolean
  created_at: string
}

// Transport list (notified transports)
export interface TransportList {
  id: number
  booking_id: number
  transport_id: number
  notified_at: string
  notification_method: "email" | "sms" | "push"
  has_viewed: boolean
  viewed_at: string | null
  has_responded: boolean
  responded_at: string | null
}

// Quotation detail
export interface QuotationDetail {
  quotation_id: number
  booking_id: number
  transport_id: number

  // Pricing breakdown
  base_price: number
  distance_price: number
  item_handling_price: number
  additional_services_price: number
  total_price: number

  // Services
  includes_packaging: boolean
  includes_disassembly: boolean
  includes_insurance: boolean
  insurance_value: number | null

  // Transport info (denormalized)
  transporter_name: string
  transporter_avatar: string | null
  transporter_rating: number
  transporter_completed_jobs: number

  // Timing
  estimated_duration_hours: number | null
  estimated_start_time: string | null

  // Status
  status: string
  is_selected: boolean
  expires_at: string | null

  notes: string | null
  metadata: any | null

  created_at: string
  updated_at: string
  accepted_at: string | null
  rejected_at: string | null
}

// Contract
export interface Contract {
  contract_id: number
  booking_id: number
  quotation_id: number
  customer_id: number
  transport_id: number

  contract_number: string
  total_amount: number

  terms_and_conditions: string | null
  payment_terms: string | null
  cancellation_policy: string | null

  contract_pdf_url: string | null

  // Digital signatures
  customer_signed: boolean
  customer_signed_at: string | null
  customer_signature_url: string | null

  transport_signed: boolean
  transport_signed_at: string | null
  transport_signature_url: string | null

  status: string
  effective_date: string | null
  completion_date: string | null

  created_at: string
  updated_at: string
}

// Payment
export interface Payment {
  payment_id: number
  booking_id: number
  amount: number
  payment_method: PaymentMethod
  payment_type: PaymentType
  bank_code: string | null
  status: PaymentStatus
  transaction_id: string | null
  confirmed_by: number | null
  confirmed_at: string | null
  paid_at: string | null
  created_at: string
  updated_at: string
}


// Status history
export interface BookingStatusHistory {
  history_id: number
  booking_id: number
  old_status: string | null
  new_status: string
  changed_by: number
  changed_at: string
  notes: string | null
}

export interface QuotationStatusHistory {
  history_id: number
  quotation_id: number
  old_status: string | null
  new_status: string
  changed_by: number
  changed_at: string
  reason: string | null
}

// API Request/Response types
export interface CreateBookingRequest {
  pickupAddress: {
    address: string
    province: string
    district: string
    ward: string
    contactName: string
    contactPhone: string
    floor: number | null
    hasElevator: boolean
  }
  deliveryAddress: {
    address: string
    province: string
    district: string
    ward: string
    contactName: string
    contactPhone: string
    floor: number | null
    hasElevator: boolean
  }
  items: Array<{
    categoryId: number
    name: string
    quantity: number
    weight?: number
    isFragile?: boolean
    requiresDisassembly?: boolean
    requiresPackaging?: boolean
    imageUrls?: string[]
  }>
  preferredDate: string
  preferredTimeSlot: string
  specialRequirements?: string
  notes?: string
  transportId?: number
  estimatedPrice?: number
}

export interface CreateBookingResponse {
  bookingId: number
  status: string
  estimatedPrice: number
  distanceKm: number
  message: string
  notifiedTransports: number
}

export interface BookingListItem {
  bookingId: number
  pickupLocation: string
  deliveryLocation: string
  distanceKm: number
  preferredDate: string
  status: BookingStatus
  estimatedPrice: number
  finalPrice: number | null
  quotationsCount: number
  itemsCount: number
  createdAt: string
}

export interface BookingDetailResponse {
  booking: BookingDetail & {
    items: BookingItem[]
    quotationsCount: number
    hasAcceptedQuotation: boolean
    transport: {
      transportId: number
      companyName: string
      phone: string
      rating: number
    } | null
    contractId: number | null
  }
  statusHistory: BookingStatusHistory[]
}

export interface QuotationSummary {
  totalQuotations: number
  lowestPrice: number
  highestPrice: number
  averagePrice: number
}

export interface SubmitQuotationRequest {
  bookingId: number
  vehicleId: number
  basePrice: number
  distancePrice: number
  itemHandlingPrice: number
  additionalServicesPrice: number
  includesPackaging: boolean
  includesDisassembly: boolean
  includesInsurance: boolean
  insuranceValue?: number
  estimatedDurationHours: number
  estimatedStartTime: string
  notes?: string
}

export interface AvailableBooking {
  bookingId: number
  pickupLocation: string
  deliveryLocation: string
  distanceKm: number
  distanceFromMe: number
  itemsCount: number
  totalWeight: number
  hasFragileItems: boolean
  preferredDate: string
  preferredTimeSlot: string
  estimatedPrice: number
  quotationsCount: number
  hasQuoted: boolean
  expiresAt: string
  notifiedAt: string
}

// ============================================================================
// MEMBER 3: VEHICLE, PRICING & ESTIMATION TYPES
// ============================================================================

export type TimeSlot = "MORNING" | "AFTERNOON" | "EVENING" | "FLEXIBLE"
export type VehicleType = "motorcycle" | "van" | "truck_small" | "truck_large" | "other"
export type VehicleStatus = "available" | "in_use" | "maintenance" | "inactive"

// Vehicle
export interface Vehicle {
  vehicle_id: number
  transport_id: number

  // Vehicle info
  type: VehicleType
  model: string
  license_plate: string // "51F-12345" UNIQUE

  // Capacity
  capacity_kg: number
  capacity_m3: number | null

  // Status
  status: VehicleStatus

  // Features
  year: number | null
  color: string | null
  has_tail_lift: boolean
  has_tools: boolean
  description: string | null
  image_url: string | null

  created_at: string
  updated_at: string
}

// Size for categories
export interface Size {
  size_id: number
  category_id: number

  name: string // "Small", "Medium", "Large"
  weight_kg: number
  height_cm: number | null
  width_cm: number | null
  depth_cm: number | null

  price_multiplier: number // 0.8, 1.0, 1.3

  created_at: string
}

// Extended Category with sizes
export interface CategoryWithSizes {
  category_id: number
  name: string
  name_en: string | null
  description: string | null
  icon: string | null
  base_weight: number
  base_volume: number
  requires_special_handling: boolean
  created_at: string

  default_weight_kg: number | null
  default_volume_m3: number | null
  default_length_cm: number | null
  default_width_cm: number | null
  default_height_cm: number | null

  is_fragile_default: boolean
  requires_disassembly_default: boolean

  display_order: number
  is_active: boolean

  sizes?: Size[]
}

// Vehicle Pricing
export interface VehiclePricing {
  pricing_id: number
  transport_id: number
  vehicle_id: number

  base_price: number // 300000 VND

  // Tiered distance pricing
  per_km_first_4km: number // 10000
  per_km_5_to_40km: number // 8000
  per_km_after_40km: number // 6000

  // Multipliers
  peak_hour_multiplier: number // 1.2
  weekend_multiplier: number // 1.1
  holiday_multiplier: number // 1.3

  // Floor fees
  no_elevator_fee: number // 50000
  elevator_discount: number // -25000

  is_active: boolean
  created_at: string
  updated_at: string
}

// Category Pricing
export interface CategoryPricing {
  pricing_id: number
  transport_id: number
  category_id: number

  price_per_unit: number // 80000 VND

  fragile_multiplier: number // 1.2
  disassembly_multiplier: number // 1.3
  heavy_multiplier: number // 1.5 (>100kg)

  min_price: number | null
  max_price: number | null

  is_active: boolean
  created_at: string
  updated_at: string
}

// Distance Cache
export interface DistanceCache {
  cache_id: number

  origin_hash: string // SHA-256
  destination_hash: string

  origin_address: string
  destination_address: string

  distance_km: number
  duration_minutes: number

  api_response: any // JSON

  created_at: string
  expires_at: string // +30 days
}

// Price History
export interface PriceHistory {
  history_id: number
  booking_id: number | null
  transport_id: number

  base_price: number
  distance_price: number
  category_price: number
  additional_fees: number
  total_price: number

  calculation_details: any // JSON

  created_at: string
}

// API Request/Response types for Member 3

export interface CreateVehicleRequest {
  type: VehicleType
  model: string
  licensePlate: string
  capacityKg: number
  capacityM3?: number
  year?: number
  color?: string
  hasTailLift?: boolean
  hasTools?: boolean
  description?: string
  imageUrl?: string
}

export interface UpdateVehicleRequest extends Partial<CreateVehicleRequest> { }

export interface VehicleListResponse {
  vehicles: Array<
    Vehicle & {
      hasPricing: boolean
    }
  >
  pagination: {
    currentPage: number
    totalPages: number
    totalItems: number
    itemsPerPage: number
  }
}

export interface SetVehiclePricingRequest {
  vehicleId: number
  basePrice: number
  perKmFirst4km: number
  perKm5To40km: number
  perKmAfter40km: number
  peakHourMultiplier: number
  weekendMultiplier: number
  holidayMultiplier: number
  noElevatorFee: number
  elevatorDiscount: number
}

export interface SetCategoryPricingRequest {
  categoryId: number
  pricePerUnit: number
  fragileMultiplier: number
  disassemblyMultiplier: number
  heavyMultiplier: number
  minPrice?: number
  maxPrice?: number
}

export interface CreateCategoryRequest {
  name: string
  nameEn?: string
  description?: string
  icon?: string
  defaultWeightKg?: number
  defaultVolumeM3?: number
  isFragileDefault?: boolean
  requiresDisassemblyDefault?: boolean
  displayOrder: number
}

export interface AddSizeRequest {
  name: string
  weightKg: number
  heightCm?: number
  widthCm?: number
  depthCm?: number
  priceMultiplier: number
}

export interface CalculateDistanceRequest {
  originAddress: string
  destinationAddress: string
}

export interface CalculateDistanceResponse {
  distanceKm: number
  durationMinutes: number
  originAddress: string
  destinationAddress: string
  cached: boolean
  apiProvider: string
}

export interface PriceBreakdown {
  basePrice: number
  distancePrice: number
  categoryPrice: number
  additionalFees: number
  subtotal: number
  multipliers: {
    peakHour?: number
    weekend?: number
    holiday?: number
  }
  total: number
}

export interface CalculatePriceRequest {
  transportId: number
  vehicleId: number
  distanceKm: number
  items: Array<{
    categoryId: number
    quantity: number
    isFragile: boolean
    requiresDisassembly: boolean
    weight: number
  }>
  pickupFloor: number
  pickupHasElevator: boolean
  deliveryFloor: number
  deliveryHasElevator: boolean
  scheduledDateTime: string
}

export interface CalculatePriceResponse {
  transportId: number
  vehicleId: number
  priceBreakdown: PriceBreakdown
  calculationDetails: {
    distanceBreakdown: {
      first4km: number
      next5to40km?: number
      after40km?: number
    }
    categoryBreakdown: Array<{
      category: string
      basePrice: number
      modifiers: string[]
      finalPrice: number
    }>
    additionalFeesBreakdown: {
      noElevatorPickup?: number
      elevatorDelivery?: number
    }
  }
}

export interface TransportEstimate {
  transportId: number
  transportName: string
  rating: number
  completedJobs: number
  vehicleId: number
  vehicleType: VehicleType
  vehicleModel: string
  estimatedPrice: number
  rank: number
  badges: string[]
  availableNow: boolean
}

export interface AutoEstimationRequest {
  pickupAddress: string
  deliveryAddress: string
  items: Array<{
    categoryId: number
    quantity: number
    isFragile: boolean
    requiresDisassembly: boolean
    weight: number
  }>
  pickupFloor: number
  pickupHasElevator: boolean
  deliveryFloor: number
  deliveryHasElevator: boolean
  scheduledDateTime: string
}

export interface AutoEstimationResponse {
  distanceKm: number
  recommendedVehicleType: VehicleType
  estimatedWeight: number
  transportEstimates: TransportEstimate[]
  priceRange: {
    lowest: number
    highest: number
    average: number
  }
}

// ============================================================================
// MEMBER 4: REVIEW & NOTIFICATION SYSTEM TYPES
// ============================================================================

export type ReviewRating = 1 | 2 | 3 | 4 | 5

export interface Review {
  review_id: number
  booking_id: number
  reviewer_id: number
  reviewee_id: number
  reviewer_type: "CUSTOMER" | "TRANSPORT"

  // Rating (1-5 stars)
  rating: ReviewRating

  // Review content
  title: string | null
  comment: string | null

  // Photos
  photo_urls: string[] | null

  // Response from reviewee
  response: string | null
  responded_at: string | null

  // Moderation
  is_verified: boolean
  is_flagged: boolean
  flag_reason: string | null
  moderated_by: number | null
  moderated_at: string | null

  created_at: string
  updated_at: string
}

export interface ReviewWithDetails extends Review {
  reviewer_name: string
  reviewer_avatar: string | null
  reviewee_name: string
  booking_pickup_location: string
  booking_delivery_location: string
  booking_completed_date: string
}

export interface ReviewStats {
  total_reviews: number
  average_rating: number
  rating_distribution: {
    five_star: number
    four_star: number
    three_star: number
    two_star: number
    one_star: number
  }
  verified_reviews: number
  with_photos: number
  with_response: number
}

export interface SubmitReviewRequest {
  bookingId: number
  rating: ReviewRating
  title?: string
  comment?: string
  photoUrls?: string[]
}

export interface RespondToReviewRequest {
  reviewId: number
  response: string
}

export interface ReportReviewRequest {
  reviewId: number
  reason: string
  details?: string
}

export type NotificationType =
  | "BOOKING_CREATED"
  | "BOOKING_UPDATED"
  | "BOOKING_CANCELLED"
  | "QUOTATION_RECEIVED"
  | "QUOTATION_ACCEPTED"
  | "QUOTATION_REJECTED"
  | "CONTRACT_SIGNED"
  | "PAYMENT_RECEIVED"
  | "JOB_STARTED"
  | "JOB_COMPLETED"
  | "REVIEW_RECEIVED"
  | "REVIEW_RESPONSE"
  | "SYSTEM_ANNOUNCEMENT"

export interface Notification {
  notification_id: number
  user_id: number
  type: NotificationType

  // Content
  title: string
  message: string

  // Related entities
  booking_id: number | null
  quotation_id: number | null
  review_id: number | null

  // Metadata
  data: any | null

  // Status
  is_read: boolean
  read_at: string | null

  // Action URL
  action_url: string | null

  created_at: string
  expires_at: string | null
}

export interface NotificationPreferences {
  user_id: number

  // Email notifications
  email_booking_updates: boolean
  email_quotations: boolean
  email_payments: boolean
  email_reviews: boolean
  email_marketing: boolean

  // Push notifications
  push_booking_updates: boolean
  push_quotations: boolean
  push_payments: boolean
  push_reviews: boolean

  // Quiet hours
  quiet_hours_enabled: boolean
  quiet_hours_start: string | null // "22:00"
  quiet_hours_end: string | null // "08:00"

  updated_at: string
}

export interface NotificationSummary {
  total_unread: number
  by_type: Record<NotificationType, number>
  latest_notifications: Notification[]
}

// ============================================================================
// INCIDENT REPORTING & EVIDENCE SYSTEM TYPES
// ============================================================================

export type IncidentType =
  | "DAMAGE" // Hàng hóa bị hư hỏng
  | "ACCIDENT" // Tai nạn giao thông
  | "DELAY" // Chậm trễ
  | "WEATHER" // Thời tiết xấu
  | "VEHICLE_BREAKDOWN" // Xe hỏng
  | "CUSTOMER_ISSUE" // Vấn đề từ khách hàng
  | "ADDRESS_ISSUE" // Vấn đề địa chỉ
  | "OTHER" // Khác

export type IncidentSeverity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL"

export type IncidentStatus = "REPORTED" | "INVESTIGATING" | "RESOLVED" | "CLOSED"

export interface Incident {
  incident_id: number
  booking_id: number
  reported_by: number // user_id
  reporter_type: "CUSTOMER" | "TRANSPORT" | "MANAGER"

  // Incident details
  type: IncidentType
  severity: IncidentSeverity
  title: string
  description: string

  // Location & time
  occurred_at: string
  location: string | null

  // Evidence
  photo_urls: string[]
  video_urls: string[] | null

  // Resolution
  status: IncidentStatus
  resolution_notes: string | null
  resolved_by: number | null
  resolved_at: string | null

  // Manager involvement
  assigned_to: number | null // manager_id
  requires_manager_action: boolean

  created_at: string
  updated_at: string
}

export interface IncidentWithDetails extends Incident {
  reporter_name: string
  reporter_avatar: string | null
  booking_pickup_location: string
  booking_delivery_location: string
  assigned_to_name: string | null
}

export interface CreateIncidentRequest {
  bookingId: number
  type: IncidentType
  severity: IncidentSeverity
  title: string
  description: string
  occurredAt: string
  location?: string
  photoUrls: string[]
  videoUrls?: string[]
}

export interface UpdateIncidentRequest {
  status?: IncidentStatus
  resolutionNotes?: string
  assignedTo?: number
}


// ============================================================================
// EXCEPTION HANDLING & MANAGER ALERTS
// ============================================================================

export type ExceptionType =
  | "NO_QUOTES_24H" // Không có báo giá sau 24h
  | "NO_QUOTE_SELECTED_3D" // Khách không chọn báo giá sau 3 ngày
  | "PAYMENT_FAILED_3X" // Thanh toán thất bại 3 lần
  | "SERIOUS_INCIDENT" // Sự cố nghiêm trọng
  | "DISPUTE" // Tranh chấp
  | "DELAYED_DELIVERY" // Giao hàng chậm
  | "CUSTOMER_COMPLAINT" // Khiếu nại từ khách hàng

export type ExceptionPriority = "LOW" | "MEDIUM" | "HIGH" | "URGENT"

export type ExceptionStatus = "PENDING" | "IN_PROGRESS" | "RESOLVED" | "ESCALATED"

export interface Exception {
  exception_id: number
  booking_id: number | null
  incident_id: number | null

  // Exception details
  type: ExceptionType
  priority: ExceptionPriority
  title: string
  description: string

  // Assignment
  assigned_to: number | null // manager_id
  status: ExceptionStatus

  // Resolution
  resolution_notes: string | null
  resolved_by: number | null
  resolved_at: string | null

  // Metadata
  metadata: any | null

  created_at: string
  updated_at: string
}

export interface ExceptionWithDetails extends Exception {
  booking_info: {
    booking_id: number
    customer_name: string
    transport_name: string | null
    pickup_location: string
    delivery_location: string
  } | null
  assigned_to_name: string | null
  resolved_by_name: string | null
}

export interface CreateExceptionRequest {
  bookingId?: number
  incidentId?: number
  type: ExceptionType
  priority: ExceptionPriority
  title: string
  description: string
  metadata?: any
}

// ============================================================================
// PERFORMANCE ANALYTICS TYPES
// ============================================================================

export interface PerformanceMetrics {
  acceptance_rate: number
  acceptance_rate_change: number
  avg_response_time_minutes: number
  response_time_change: number
  customer_satisfaction: number
  satisfaction_change: number
  on_time_delivery_rate: number
  on_time_change: number
  completion_rate: number
  completion_change: number
  revenue_per_job: number
  revenue_change: number
  total_jobs: number
  jobs_change: number
  active_vehicles: number
}

export interface PerformanceTrend {
  date: string
  jobs_completed: number
  acceptance_rate: number
  avg_response_time: number
  revenue: number
  satisfaction_score: number
}

export interface CategoryPerformance {
  category: string
  jobs: number
  revenue: number
  avg_rating: number
}

export interface VehicleUtilization {
  vehicle_name: string
  utilization_rate: number
  jobs_completed: number
  revenue: number
}

// ============================================================================
// SUPPORT/HELP SYSTEM TYPES
// ============================================================================

export type FAQCategory =
  | "getting_started"
  | "bookings"
  | "payments"
  | "account"
  | "vehicles"
  | "pricing"
  | "technical"
  | "other"

export interface FAQ {
  faq_id: number
  category: FAQCategory
  question: string
  answer: string
  order: number
  is_popular: boolean
  view_count: number
  helpful_count: number
  created_at: string
  updated_at: string
}

export type SupportTicketStatus = "OPEN" | "IN_PROGRESS" | "RESOLVED" | "CLOSED"
export type SupportTicketPriority = "LOW" | "MEDIUM" | "HIGH" | "URGENT"

export interface SupportTicket {
  ticket_id: number
  user_id: number
  subject: string
  description: string
  category: FAQCategory
  priority: SupportTicketPriority
  status: SupportTicketStatus
  assigned_to: number | null
  resolved_at: string | null
  created_at: string
  updated_at: string
}

export interface SupportTicketWithDetails extends SupportTicket {
  user_name: string
  user_email: string
  user_role: "CUSTOMER" | "TRANSPORT" | "MANAGER"
  assigned_to_name: string | null
  messages_count: number
  last_message_at: string | null
}

export interface SupportMessage {
  message_id: number
  ticket_id: number
  sender_id: number
  sender_name: string
  sender_role: "CUSTOMER" | "TRANSPORT" | "MANAGER" | "SUPPORT"
  message: string
  attachments: string[] | null
  created_at: string
}

export interface CreateSupportTicketRequest {
  subject: string
  description: string
  category: FAQCategory
  priority: SupportTicketPriority
  attachments?: string[]
}

export interface HelpArticle {
  article_id: number
  title: string
  content: string
  category: FAQCategory
  tags: string[]
  view_count: number
  is_featured: boolean
  created_at: string
  updated_at: string
}

export interface ContactInfo {
  email: string
  phone: string
  address: string
  business_hours: string
  response_time: string
}

// ============================================================================
// INTAKE & ITEM CANDIDATE TYPES
// ============================================================================

export interface ItemCandidate {
  id: string // temporary ID for UI
  name: string
  category_id: number | null
  category_name: string | null

  // Size
  size: "S" | "M" | "L" | null
  weight_kg: number | null
  dimensions: {
    width_cm: number | null
    height_cm: number | null
    depth_cm: number | null
  } | null

  // Quantity
  quantity: number

  // Attributes
  is_fragile: boolean
  requires_disassembly: boolean
  requires_packaging: boolean

  // Source & confidence
  source: "image" | "model" | "text" | "ocr" | "document" | "manual"
  confidence: number | null // 0-1 for AI sources

  // Additional data
  image_url: string | null
  notes: string | null
  metadata: any | null
}

export interface ModelSearchResult {
  name: string
  brand: string
  model: string
  dimensions_mm: {
    width: number
    height: number
    depth: number
  } | null
  weight_kg: number | null
  category: string | null
  confidence: number
  source: string
  source_url: string | null
}

export interface TextParseResult {
  items: Array<{
    name: string
    quantity: number
    category: string | null
    size: "S" | "M" | "L" | null
    confidence: number
  }>
}

export interface OCRResult {
  extracted_text: string
  items: Array<{
    name: string
    quantity: number
    category: string | null
    confidence: number
  }>
}

// ============================================================================
// DISPUTE SYSTEM TYPES
// ============================================================================

export type DisputeType =
  | "PRICING_DISPUTE"
  | "DAMAGE_CLAIM"
  | "SERVICE_QUALITY"
  | "DELIVERY_ISSUE"
  | "PAYMENT_ISSUE"
  | "OTHER"

export type DisputeStatus =
  | "PENDING"
  | "UNDER_REVIEW"
  | "RESOLVED"
  | "REJECTED"
  | "ESCALATED"

export interface Dispute {
  disputeId: number
  bookingId: number
  bookingStatus?: string

  // Filed by information
  filedByUserId: number
  filedByUserName: string
  filedByUserRole: "CUSTOMER" | "TRANSPORT" | "MANAGER"

  // Dispute details
  disputeType: DisputeType
  status: DisputeStatus
  title: string
  description: string
  requestedResolution?: string

  // Resolution information
  resolutionNotes?: string
  resolvedByUserId?: number
  resolvedByUserName?: string
  resolvedAt?: string

  // Counts
  messageCount: number
  evidenceCount: number

  // Timestamps
  createdAt: string
  updatedAt: string
}

export interface DisputeMessage {
  messageId: number
  disputeId: number

  // Sender information
  senderUserId: number
  senderName: string
  senderRole: "CUSTOMER" | "TRANSPORT" | "MANAGER"

  // Message content
  messageText: string

  // Timestamp
  createdAt: string
}

export interface CreateDisputeRequest {
  disputeType: DisputeType
  title: string
  description: string
  requestedResolution?: string
  evidenceIds?: number[]
}

export interface AddDisputeMessageRequest {
  messageText: string
}

export interface UpdateDisputeStatusRequest {
  status: DisputeStatus
  resolutionNotes?: string
}

// ============================================================================
// COUNTER-OFFER SYSTEM TYPES
// ============================================================================

export type CounterOfferStatus =
  | "PENDING"
  | "ACCEPTED"
  | "REJECTED"
  | "EXPIRED"
  | "SUPERSEDED"

export type CounterOfferRole = "CUSTOMER" | "TRANSPORT"

export interface CounterOffer {
  counterOfferId: number
  quotationId: number
  bookingId: number

  // Offer details
  offeredByUserId: number
  offeredByUserName: string
  offeredByRole: CounterOfferRole

  // Pricing
  offeredPrice: number
  originalPrice: number
  priceDifference: number
  percentageChange: number

  // Status
  status: CounterOfferStatus
  isExpired: boolean
  canRespond: boolean

  // Negotiation details
  message?: string
  reason?: string

  // Response tracking
  respondedByUserId?: number
  respondedByUserName?: string
  respondedAt?: string
  responseMessage?: string

  // Expiration
  expiresAt: string
  hoursUntilExpiration?: number

  // Timestamps
  createdAt: string
  updatedAt: string
}

export interface CreateCounterOfferRequest {
  quotationId: number
  offeredPrice: number
  message?: string
  reason?: string
  expirationHours?: number
}

export interface RespondToCounterOfferRequest {
  accept: boolean
  responseMessage?: string
}

// ============================================================================
// SCAN SESSION & AI PROCESSING TYPES (Member 2)
// ============================================================================

export type ScanStatus =
  | "PENDING" // Đang chờ xử lý
  | "PROCESSING" // AI đang xử lý
  | "NEEDS_REVIEW" // Cần admin review
  | "REVIEWED" // Admin đã review
  | "QUOTED" // Đã có báo giá hệ thống
  | "PUBLISHED" // Đã publish để nhà xe đấu giá
  | "FAILED" // Xử lý thất bại

export interface ScanSession {
  session_id: number
  customer_id: number
  status: ScanStatus

  // Images
  image_urls: string[]
  image_count: number

  // AI Detection results
  detection_results: any | null // JSON with bbox, confidence, labels
  average_confidence: number | null

  // Extracted items
  items: ItemCandidate[]

  // System estimation
  estimated_price: number | null
  estimated_weight_kg: number | null
  estimated_volume_m3: number | null

  // Admin review
  reviewed_by: number | null
  reviewed_at: string | null
  review_notes: string | null

  // Force quote
  forced_quote_price: number | null
  forced_quote_by: number | null
  forced_quote_at: string | null

  // Publishing
  published_at: string | null
  bidding_expires_at: string | null

  created_at: string
  updated_at: string
}

export interface ScanSessionWithCustomer extends ScanSession {
  customer_name: string
  customer_email: string
  customer_avatar: string | null
}

export interface RerunAIRequest {
  sessionId: number
  forceReprocess: boolean
}

export interface ForceQuoteRequest {
  sessionId: number
  price: number
  notes?: string
}

export interface PublishBiddingRequest {
  sessionId: number
  expiresInHours: number
}
