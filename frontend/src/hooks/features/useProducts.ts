import { useState, useCallback } from 'react'
import { useApi } from './common/useApi'
import * as productsApi from '../api/products'
import type { ProductListResponse, ProductResponse, Category } from '../types'
import type { Page } from '../types/common'

export function useProducts() {
  const listApi = useApi<Page<ProductListResponse>>()
  const itemApi = useApi<ProductResponse>()
  const [products, setProducts] = useState<ProductListResponse[]>([])

  const loadProducts = useCallback(async (params?: {
    page?: number; search?: string; category?: Category; available?: boolean
  }) => {
    const data = await listApi.execute(() => productsApi.getProducts(params || {}))
    setProducts(data.content)
  }, [])

  const getProduct = (id: number) => itemApi.execute(() => productsApi.getProduct(id))

  return {
    products,
    totalPages: listApi.data?.totalPages || 0,
    loading: listApi.loading,
    error: listApi.error,
    loadProducts,
    getProduct,
    product: itemApi.data,
    productLoading: itemApi.loading
  }
}