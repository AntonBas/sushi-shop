import api from './client'
import type { UserResponse, UpdateUserRequest, ChangePasswordRequest } from '../types'

export const getMe = async (): Promise<UserResponse> => {
  const { data } = await api.get('/users/me')
  return data
}

export const updateProfile = async (data: UpdateUserRequest): Promise<UserResponse> => {
  const { data: res } = await api.put('/users/me', data)
  return res
}

export const changePassword = async (data: ChangePasswordRequest): Promise<void> => {
  await api.put('/users/me/password', data)
}