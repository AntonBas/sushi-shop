import { useEffect } from 'react'
import { useApi } from '@/hooks/common/useApi'
import * as promotionsApi from '@/features/promotion/api/promotions'
import type { PromotionResponse } from '@/features/promotion/types/promotion'

export function usePromotions() {
  const { data, loading, error, run } = useApi<PromotionResponse[]>()

  useEffect(() => {
    void run(() => promotionsApi.getActivePromotions())
  }, [run])

  return { promotions: data || [], loading, error }
}