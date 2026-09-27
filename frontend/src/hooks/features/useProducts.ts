import { useState, useCallback, useRef } from 'react'
import { useApi } from '../common/useApi'
import * as productsApi from '../../api/products'
import type { ProductListResponse, ProductResponse } from '../../types'
import type { ProductFilters } from '../../types/product'
import type { Page } from '../../types/common'

export function useProducts() {
  const { data: listData, loading: listLoading, error: listError, run: listRun } = useApi<Page<ProductListResponse>>()
  const { data: itemData, loading: itemLoading, run: itemRun } = useApi<ProductResponse>()
  const { run: popularRun } = useApi<ProductListResponse[]>()
  const { run: relatedRun } = useApi<ProductListResponse[]>()
  const [products, setProducts] = useState<ProductListResponse[]>([])
  const [popular, setPopular] = useState<ProductListResponse[]>([])
  const [related, setRelated] = useState<ProductListResponse[]>([])
  const latestListRequestId = useRef(0)

  const loadProducts = useCallback(async (page = 0, filters?: ProductFilters) => {
    const requestId = ++latestListRequestId.current
    const data = await listRun(() => productsApi.getProducts(page, 12, filters))
    if (!data || requestId !== latestListRequestId.current) return
    setProducts(data.content)
  }, [listRun])

  const loadMoreProducts = useCallback(async (page = 0, filters?: ProductFilters) => {
    const requestId = ++latestListRequestId.current
    const data = await listRun(() => productsApi.getProducts(page, 12, filters))
    if (!data || requestId !== latestListRequestId.current) return
    setProducts(prev => page === 0 ? data.content : [...prev, ...data.content])
  }, [listRun])

  const loadPopular = useCallback(async () => {
    const data = await popularRun(() => productsApi.getPopularProducts())
    if (data) setPopular(data)
  }, [popularRun])

  const loadRelated = useCallback(async (id: number) => {
    const data = await relatedRun(() => productsApi.getRelatedProducts(id))
    if (data) setRelated(data)
  }, [relatedRun])

  const getProduct = useCallback((id: number) => itemRun(() => productsApi.getProduct(id)), [itemRun])

  const getProductBySlug = useCallback((slug: string) => itemRun(() => productsApi.getProductBySlug(slug)), [itemRun])

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