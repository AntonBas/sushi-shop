import api from './client'
import type { LoginRequest, RegisterRequest, ForgotPasswordRequest, ResetPasswordRequest, AuthResponse } from '../types'

export const login = async (data: LoginRequest): Promise<AuthResponse> => {
  const { data: res } = await api.post('/auth/login', data)
  return res
}

export const register = async (data: RegisterRequest): Promise<AuthResponse> => {
  const { data: res } = await api.post('/auth/register', data)
  return res
}

export const verifyEmail = async (token: string): Promise<void> => {
  await api.get('/auth/verify', { params: { token } })
}

export const forgotPassword = async (data: ForgotPasswordRequest): Promise<void> => {
  await api.post('/auth/password/forgot', data)
}

export const resetPassword = async (data: ResetPasswordRequest): Promise<void> => {
  await api.post('/auth/password/reset', data)
}