import type { AddressRequest } from "@/types/address"
import type { UserResponse } from "@/features/user/types/user"

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
  confirmPassword: string
}

export interface AuthResponse {
  user: UserResponse
}

export interface ResendVerificationResponse {
  cooldownSeconds: number
}