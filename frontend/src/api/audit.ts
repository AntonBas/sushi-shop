import api from './client'
import type { Page } from '../types/common'
import type { AuditLogResponse, AuditLogFilters } from '../types/audit'

export const getAuditLogs = async (
  page = 0,
  size = 20,
  sort = 'performedAt,desc',
  filters?: AuditLogFilters
): Promise<Page<AuditLogResponse>> => {
  const { data } = await api.get('/admin/audit', {
    params: {
      page,
      size,
      sort,
      ...filters,
    },
  })
  return data
}