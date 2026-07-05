import api from './client'
import type { Page } from '../types/common'
import type { AuditLogResponse } from '../types/audit'

export const getAuditLogs = async (page = 0, size = 20): Promise<Page<AuditLogResponse>> => {
  const { data } = await api.get('/admin/audit', { params: { page, size, sort: 'performedAt,desc' } })
  return data
}