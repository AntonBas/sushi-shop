import { useApi } from '../common/useApi'
import * as ordersApi from '../../api/orders'
import type { OrderResponse } from '../../types'
import type { Page } from '../../types/common'

export function useAdminOrders() {
  const { data, loading, error, execute } = useApi<Page<OrderResponse>>()

  const loadOrders = (page = 0, size = 12) => execute(() => ordersApi.getAllOrders(page, size))

  return { orders: data?.content || [], totalPages: data?.totalPages || 0, loading, error, loadOrders }
}