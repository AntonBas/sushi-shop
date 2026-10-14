import type { UserRole } from './enums'

export interface AdminUserResponse {
  id: number
  name: string
  email: string
  phone: string | null
  userRole: UserRole
  emailVerified: boolean
  hasPassword: boolean
  createdAt: string
  ordersCount: number
  totalSpent: number
}

export interface AdminUserFilters {
  search?: string
  role?: UserRole
  emailVerified?: boolean
}
