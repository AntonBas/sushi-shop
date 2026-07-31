import { useState, useCallback } from 'react'
import { useApi } from '.././common/useApi'
import * as productsApi from '../../api/products'
import type { ProductListResponse, ProductResponse } from '../../types'
import type { ProductFilters } from '../../types/product'
import type { Page } from '../../types/common'

export function useProducts() {
  const listApi = useApi<Page<ProductListResponse>>()
  const itemApi = useApi<ProductResponse>()
  const popularApi = useApi<ProductListResponse[]>()
  const relatedApi = useApi<ProductListResponse[]>()
  const [products, setProducts] = useState<ProductListResponse[]>([])
  const [popular, setPopular] = useState<ProductListResponse[]>([])
  const [related, setRelated] = useState<ProductListResponse[]>([])

  const loadProducts = useCallback(async (page = 0, filters?: ProductFilters) => {
    const data = await listApi.execute(() => productsApi.getProducts(page, 12, filters))
    setProducts(data.content)
  }, [])

  const loadPopular = useCallback(async () => {
    const data = await popularApi.execute(() => productsApi.getPopularProducts())
    if (data) setPopular(data)
  }, [])

  const loadRelated = useCallback(async (id: number) => {
    const data = await relatedApi.execute(() => productsApi.getRelatedProducts(id))
    if (data) setRelated(data)
  }, [])

  const getProduct = (id: number) => itemApi.execute(() => productsApi.getProduct(id))

  const getProductBySlug = (slug: string) => itemApi.execute(() => productsApi.getProductBySlug(slug))

  return {
    products,
    popular,
    related,
    totalPages: listApi.data?.totalPages || 0,
    loading: listApi.loading,
    error: listApi.error,
    loadProducts,
    loadPopular,
    loadRelated,
    getProduct,
    getProductBySlug,
    product: itemApi.data,
    productLoading: itemApi.loading
  }
}