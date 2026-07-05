import { useCallback } from 'react'
import { useApi } from './useApi'
import * as ordersApi from '../api/orders'
import type { CreateOrderRequest, OrderResponse } from '../types'

export function useOrders() {
  const createApi = useApi<OrderResponse>()

  const createOrder = useCallback((data: CreateOrderRequest) => {
    return createApi.execute(() => ordersApi.createOrder(data))
  }, [])

  return {
    createOrder,
    order: createApi.data,
    loading: createApi.loading,
    error: createApi.error
  }
}