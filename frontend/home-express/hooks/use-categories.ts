import useSWR from "swr"
import type { CategoryWithSizes } from "@/types"
import { apiClient } from "@/lib/api-client"

type CategoriesScope = "admin" | "transport"

/**
 * SWR hook for fetching categories
 */
export function useCategories(
  params?: { isActive?: boolean; page?: number; size?: number },
  options?: { scope?: CategoriesScope },
) {
  const scope = options?.scope ?? "admin"
  const key = scope === "admin" ? ["/admin/categories", params] : ["/transport/categories", params]
  const fetcher =
    scope === "admin"
      ? () => apiClient.getAllCategories(params)
      : () => apiClient.getTransportCategories({ isActive: params?.isActive })

  const { data, error, isLoading, mutate } = useSWR(key, fetcher, {
    refreshInterval: 0,
    dedupingInterval: 60000, // Cache for 1 minute
  })

  return {
    categories: (data?.data?.categories as CategoryWithSizes[] | undefined) || [],
    isLoading,
    isError: error,
    mutate,
  }
}

