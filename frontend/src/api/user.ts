import api from './client'
import type { UserResponse, UpdateUserRequest, ChangePasswordRequest, ChangeEmailRequest } from '../types'

export const getMe = async (): Promise<UserResponse> => {
  const { data } = await api.get('/users/me', { skipAuthRedirect: true })
  return data
}

export const updateProfile = async (data: UpdateUserRequest): Promise<UserResponse> => {
  const { data: res } = await api.put('/users/me', data)
  return res
}

export const changePassword = async (data: ChangePasswordRequest): Promise<void> => {
  await api.put('/users/me/password', data)
}

export const requestEmailChange = async (data: ChangeEmailRequest): Promise<void> => {
  await api.post('/auth/email-change', data)
}

export const confirmEmailChange = async (token: string): Promise<UserResponse> => {
  const { data } = await api.get('/auth/email-change/confirm', { params: { token } })
  return data
}

export const disableGoogleSignIn = async (): Promise<void> => {
  await api.post('/users/me/google-unlink')
}