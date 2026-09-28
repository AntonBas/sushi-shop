import { useState, useCallback } from 'react'
import { useApi } from '../common/useApi'
import * as auditApi from '../../api/audit'
import type { AuditLogResponse, AuditLogFilters } from '../../types/audit'
import type { Page } from '../../types/common'

export function useAuditLogs() {
  const { data, loading, error, run } = useApi<Page<AuditLogResponse>>()
  const [logs, setLogs] = useState<AuditLogResponse[]>([])

  const loadLogs = useCallback(async (page = 0, filters?: AuditLogFilters) => {
    const result = await run(() => auditApi.getAuditLogs(page, 20, 'performedAt,desc', filters))
    if (result) setLogs(result.content)
  }, [run])

  return {
    logs,
    totalPages: data?.page.totalPages || 0,
    loading,
    error,
    loadLogs,
  }
}