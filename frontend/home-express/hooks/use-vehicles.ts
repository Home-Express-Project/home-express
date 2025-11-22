import useSWR from "swr"
import { apiClient } from "@/lib/api-client"

/**
 * SWR hook for fetching transport's vehicles
 */
export function useVehicles(params?: { status?: string; page?: number; size?: number }) {
  const { data, error, isLoading, mutate } = useSWR(
    ["/transport/vehicles", params],
    () => apiClient.getVehicles(params),
    {
      refreshInterval: 0,
      revalidateOnFocus: true,
    },
  )

  return {
    vehicles: data?.data?.vehicles || [],
    pagination: data?.data?.pagination,
    isLoading,
    isError: error,
    mutate,
  }
}

/**
 * SWR hook for fetching single vehicle
 */
export function useVehicle(vehicleId: number | null) {
  const { data, error, isLoading, mutate } = useSWR(
    vehicleId ? `/transport/vehicles/${vehicleId}` : null,
    () => (vehicleId ? apiClient.getVehicle(vehicleId) : null),
    {
      refreshInterval: 0,
      revalidateOnFocus: true,
    },
  )

  return {
    vehicle: data?.data,
    isLoading,
    isError: error,
    mutate,
  }
}

/**
 * SWR hook for fetching vehicle pricing
 */
export function useVehiclePricing() {
  const { data, error, isLoading, mutate } = useSWR("/transport/pricing/vehicles", () => apiClient.getVehiclePricing())

  return {
    pricingRules: data?.data?.pricingRules || [],
    isLoading,
    isError: error,
    mutate,
  }
}

/**
 * SWR hook for fetching transport rate cards (Stage 3.2)
 */
export function useCategoryPricing() {
  const { data, error, isLoading, mutate } = useSWR("/transport/rate-cards", () =>
    apiClient.getTransportRateCards(),
  )

  return {
    rateCards: data || [],
    isLoading,
    isError: error,
    mutate,
  }
}


/**
 * SWR hook for READY_TO_QUOTE status (Stage 3.2)
 */
export function useReadyToQuoteStatus() {
  const { data, error, isLoading, mutate } = useSWR("/transport/ready-status", () =>
    apiClient.getReadyToQuoteStatus(),
  )

  return {
    status: data || null,
    isLoading,
    isError: error,
    mutate,
  }
}
