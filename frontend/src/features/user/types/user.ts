import type { AddressRequest, AddressResponse } from "../../../types/address"
import type { UserRole } from "../../../types/enums"

export interface UpdateUserRequest {
  name?: string
  phone?: string
  address?: AddressRequest
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

export interface ChangeEmailRequest {
  newEmail: string
  currentPassword?: string
}

export interface UserResponse {
  id: number
  name: string
  email: string
  pendingEmail?: string
  phone: string
  userRole: UserRole
  address?: AddressResponse
  hasPassword: boolean
}