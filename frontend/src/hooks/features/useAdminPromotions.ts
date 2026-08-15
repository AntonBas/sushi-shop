import { useApi } from '../common/useApi'
import * as promotionsApi from '../../api/promotions'
import type { PromotionResponse } from '../../types'
import type { Page } from '../../types/common'

export function useAdminPromotions() {
  const { data, loading, error, execute } = useApi<Page<PromotionResponse>>()

  const loadPromotions = (page = 0, size = 12, search?: string) =>
    execute(() => promotionsApi.getPromotions(page, size, search))

  return { promotions: data?.content || [], totalPages: data?.page.totalPages || 0, loading, error, loadPromotions }
}