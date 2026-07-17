import type { AddressRequest, AddressResponse } from "./address"
import type { UserRole } from "./enums"

export interface UpdateUserRequest {
  name?: string
  phone?: string
  address?: AddressRequest
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

export interface UserResponse {
  id: number
  name: string
  email: string
  phone: string
  userRole: UserRole
  address?: AddressResponse
}