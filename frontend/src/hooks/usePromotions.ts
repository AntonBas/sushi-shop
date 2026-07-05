import { useEffect } from 'react'
import { useApi } from './useApi'
import * as promotionsApi from '../api/promotions'
import type { PromotionResponse } from '../types'

export function usePromotions() {
  const { data, loading, error, execute } = useApi<PromotionResponse[]>()

  useEffect(() => {
    execute(() => promotionsApi.getActivePromotions())
  }, [])

  return { promotions: data || [], loading, error }
}