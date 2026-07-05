import type { AddressRequest } from "./address"
import type { UserResponse } from "./user"

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  name: string
  email: string
  password: string
  confirmPassword: string
  phone: string
  address?: AddressRequest
}

export interface ForgotPasswordRequest {
  email: string
}

export interface ResetPasswordRequest {
  token: string
  newPassword: string
}

export interface AuthResponse {
  token: string
  tokenType: string
  user: UserResponse
}