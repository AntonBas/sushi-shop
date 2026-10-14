import api from './client'
import type { Page } from '../types/common'
import type { AdminUserFilters, AdminUserResponse, UserRole } from '../types'

export const getAdminUsers = async (
  page = 0,
  size = 20,
  sort = 'createdAt,desc',
  filters?: AdminUserFilters,
): Promise<Page<AdminUserResponse>> => {
  const { data } = await api.get<Page<AdminUserResponse>>('/admin/users', {
    params: { page, size, sort, ...filters },
  })
  return data
}

export const changeUserRole = async (id: number, role: UserRole): Promise<void> => {
  await api.patch(`/admin/users/${id}/role`, { role })
}
