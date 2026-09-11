import { useState, useCallback } from 'react'
import { useApi } from '.././common/useApi'
import * as productsApi from '../../api/products'
import type { ProductListResponse, ProductResponse } from '../../types'
import type { ProductFilters } from '../../types/product'
import type { Page } from '../../types/common'

export function useProducts() {
  const { data: listData, loading: listLoading, error: listError, execute: listExecute } = useApi<Page<ProductListResponse>>()
  const { data: itemData, loading: itemLoading, execute: itemExecute } = useApi<ProductResponse>()
  const { execute: popularExecute } = useApi<ProductListResponse[]>()
  const { execute: relatedExecute } = useApi<ProductListResponse[]>()
  const [products, setProducts] = useState<ProductListResponse[]>([])
  const [popular, setPopular] = useState<ProductListResponse[]>([])
  const [related, setRelated] = useState<ProductListResponse[]>([])

  const loadProducts = useCallback(async (page = 0, filters?: ProductFilters) => {
    const data = await listExecute(() => productsApi.getProducts(page, 12, filters))
    setProducts(data.content)
  }, [listExecute])

  const loadMoreProducts = useCallback(async (page = 0, filters?: ProductFilters) => {
    const data = await listExecute(() => productsApi.getProducts(page, 12, filters))
    setProducts(prev => page === 0 ? data.content : [...prev, ...data.content])
  }, [listExecute])

  const loadPopular = useCallback(async () => {
    const data = await popularExecute(() => productsApi.getPopularProducts())
    if (data) setPopular(data)
  }, [popularExecute])

  const loadRelated = useCallback(async (id: number) => {
    const data = await relatedExecute(() => productsApi.getRelatedProducts(id))
    if (data) setRelated(data)
  }, [relatedExecute])

  const getProduct = useCallback((id: number) => itemExecute(() => productsApi.getProduct(id)), [itemExecute])

  const getProductBySlug = useCallback((slug: string) => itemExecute(() => productsApi.getProductBySlug(slug)), [itemExecute])

  return {
    products,
    popular,
    related,
    totalPages: listData?.page.totalPages || 0,
    loading: listLoading,
    error: listError,
    loadProducts,
    loadMoreProducts,
    loadPopular,
    loadRelated,
    getProduct,
    getProductBySlug,
    product: itemData,
    productLoading: itemLoading
  }
}