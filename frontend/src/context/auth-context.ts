import { createContext } from "react";
import type { LoginRequest, RegisterRequest } from '@/features/auth/types/auth'
import type { UserResponse } from '@/features/user/types/user';

export interface AuthContextType {
  user: UserResponse | null;
  loading: boolean;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isCourier: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (userData: RegisterRequest) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);
