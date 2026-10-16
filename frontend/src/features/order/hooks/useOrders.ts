import { useCallback } from 'react'
import { useApi } from '@/hooks/common/useApi'
import * as ordersApi from '@/features/order/api/orders'
import type { CreateOrderRequest, OrderResponse } from '@/features/order/types/order'

export function useOrders() {
  const { data, loading, error, execute } = useApi<OrderResponse>()

  const createOrder = useCallback((data: CreateOrderRequest) => {
    return execute(() => ordersApi.createOrder(data))
  }, [execute])

  return {
    createOrder,
    order: data,
    loading,
    error
  }
}