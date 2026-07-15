import { useState, useCallback } from 'react'
import { useApi } from '../common/useApi'
import * as auditApi from '../../api/audit'
import type { AuditLogResponse, AuditLogFilters } from '../../types/audit'
import type { Page } from '../../types/common'

export function useAuditLogs() {
  const api = useApi<Page<AuditLogResponse>>()
  const [logs, setLogs] = useState<AuditLogResponse[]>([])

  const loadLogs = useCallback(async (page = 0, filters?: AuditLogFilters) => {
    const data = await api.execute(() => auditApi.getAuditLogs(page, 20, filters))
    setLogs(data.content)
  }, [])

  return {
    logs,
    totalPages: api.data?.totalPages || 0,
    loading: api.loading,
    error: api.error,
    loadLogs,
  }
}