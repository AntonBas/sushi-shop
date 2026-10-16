import { useState, useCallback } from 'react'
import { useApi } from '../../../hooks/common/useApi'
import * as adminUsersApi from '../api/adminUsers'
import type { AdminUserFilters, AdminUserResponse } from '../../../types'
import type { Page } from '../../../types/common'

export function useAdminUsers() {
  const { data, loading, run } = useApi<Page<AdminUserResponse>>()
  const [users, setUsers] = useState<AdminUserResponse[]>([])

  const loadUsers = useCallback(async (page: number, sort: string, filters?: AdminUserFilters) => {
    const result = await run(() => adminUsersApi.getAdminUsers(page, 20, sort, filters))
    if (result) setUsers(result.content)
  }, [run])

  return {
    users,
    totalPages: data?.page.totalPages || 0,
    loading,
    loadUsers,
  }
}
