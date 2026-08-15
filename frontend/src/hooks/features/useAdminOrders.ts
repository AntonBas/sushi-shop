import { useApi } from '../common/useApi'
import * as ordersApi from '../../api/orders'
import type { OrderResponse, OrderFilters } from '../../types'
import type { Page } from '../../types/common'

export function useAdminOrders() {
  const { data, loading, error, execute } = useApi<Page<OrderResponse>>()

  const loadOrders = (page = 0, size = 12, filters?: OrderFilters) => 
    execute(() => ordersApi.getAllOrders(page, size, filters))

  return { orders: data?.content || [], totalPages: data?.page.totalPages || 0, loading, error, loadOrders }
}