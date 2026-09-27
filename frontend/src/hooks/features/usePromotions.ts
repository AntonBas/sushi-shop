import { useEffect } from 'react'
import { useApi } from '../common/useApi'
import * as promotionsApi from '../../api/promotions'
import type { PromotionResponse } from '../../types'

export function usePromotions() {
  const { data, loading, error, run } = useApi<PromotionResponse[]>()

  useEffect(() => {
    void run(() => promotionsApi.getActivePromotions())
  }, [run])

  return { promotions: data || [], loading, error }
}