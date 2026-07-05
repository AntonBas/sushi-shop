import { useApi } from './useApi'
import * as authApi from '../api/auth'
import type { AuthResponse, LoginRequest, RegisterRequest } from '../types'

export function useAuth() {
  const { data, loading, error, execute } = useApi<AuthResponse>()

  const login = (req: LoginRequest) => execute(() => authApi.login(req))
  const register = (req: RegisterRequest) => execute(() => authApi.register(req))

  return { user: data?.user, token: data?.token, loading, error, login, register }
}