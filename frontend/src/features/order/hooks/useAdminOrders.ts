import { useCallback } from 'react'
import { useApi } from '@/hooks/common/useApi'
import * as ordersApi from '@/features/order/api/orders'
import type { OrderResponse, OrderFilters } from '@/features/order/types/order'
import type { Page } from '@/types/common'

export function useAdminOrders() {
  const { data, loading, error, run } = useApi<Page<OrderResponse>>()

  const loadOrders = useCallback((page = 0, size = 12, filters?: OrderFilters) =>
    run(() => ordersApi.getAllOrders(page, size, filters)), [run])

  return { orders: data?.content || [], totalPages: data?.page.totalPages || 0, loading, error, loadOrders }
}