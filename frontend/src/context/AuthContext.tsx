import { createContext, useContext, useEffect, useRef, useState } from 'react'
import * as authApi from '../api/auth'
import * as usersApi from '../api/user'
import type { UserResponse, LoginRequest, RegisterRequest } from '../types'

interface AuthContextType {
  user: UserResponse | null
  loading: boolean
  isAuthenticated: boolean
  isAdmin: boolean
  login: (credentials: LoginRequest) => Promise<void>
  register: (userData: RegisterRequest) => Promise<UserResponse>
  logout: () => void
  refreshUser: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const fetchedRef = useRef(false)

  const token = localStorage.getItem('token')
  const isAuthenticated = !!user
  const isAdmin = user?.userRole === 'ADMIN'

  useEffect(() => {
    if (token && !fetchedRef.current) {
      fetchedRef.current = true
      usersApi.getMe()
        .then(setUser)
        .catch(() => {
          localStorage.removeItem('token')
          setUser(null)
        })
        .finally(() => setLoading(false))
    } else {
      setLoading(false)
    }
  }, [])

  const login = async (credentials: LoginRequest) => {
    const response = await authApi.login(credentials)
    localStorage.setItem('token', response.token)
    fetchedRef.current = true
    setUser(response.user)
  }

  const register = async (userData: RegisterRequest) => {
    const response = await authApi.register(userData)
    localStorage.setItem('token', response.token)
    fetchedRef.current = true
    setUser(response.user)
    return response.user
  }

  const logout = () => {
    localStorage.removeItem('token')
    setUser(null)
    fetchedRef.current = false
    window.location.href = '/login'
  }

  const refreshUser = async () => {
    try {
      const data = await usersApi.getMe()
      setUser(data)
    } catch {
      logout()
    }
  }

  return (
    <AuthContext.Provider value={{ user, loading, isAuthenticated, isAdmin, login, register, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within AuthProvider')
  return context
}