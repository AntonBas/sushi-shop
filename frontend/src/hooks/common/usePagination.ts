import { useState, useCallback } from 'react'

interface UsePaginationParams {
  page: number
  size: number
  sort?: string
  search?: string
}

export const usePagination = (defaultSize = 12) => {
  const [params, setParams] = useState<UsePaginationParams>({ page: 0, size: defaultSize })

  const setPage = useCallback((page: number) => setParams(p => ({ ...p, page: Math.max(0, page) })), [])
  const setSize = useCallback((size: number) => setParams(p => ({ ...p, size, page: 0 })), [])
  const setSearch = useCallback((search: string) => setParams(p => ({ ...p, search: search || undefined, page: 0 })), [])
  const setSort = useCallback((sort: string) => setParams(p => ({ ...p, sort })), [])
  const nextPage = useCallback(() => setParams(p => ({ ...p, page: p.page + 1 })), [])
  const prevPage = useCallback(() => setParams(p => ({ ...p, page: Math.max(0, p.page - 1) })), [])

  return { ...params, setPage, setSize, setSearch, setSort, nextPage, prevPage }
}