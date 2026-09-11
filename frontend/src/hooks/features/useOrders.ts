import { useCallback } from 'react'
import { useApi } from '.././common/useApi'
import * as ordersApi from '../../api/orders'
import type { CreateOrderRequest, OrderResponse } from '../../types'

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